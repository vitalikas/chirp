package lt.vitalijus.chirp.api.websocket

import lt.vitalijus.chirp.api.dto.ws.*
import lt.vitalijus.chirp.api.mappers.toChatMessageDto
import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.UserId
import lt.vitalijus.chirp.service.ChatMessageService
import lt.vitalijus.chirp.service.ChatService
import lt.vitalijus.chirp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

typealias SessionId = String

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jwtService: JwtService
) : TextWebSocketHandler() {


    private val logger = LoggerFactory.getLogger(javaClass)

    private val connectionLock = ReentrantReadWriteLock()

    private val sessions = ConcurrentHashMap<SessionId, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<SessionId>>()
    private val userChats = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<SessionId>>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was closed due to missing auth header")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
                return
            }

        val userId = jwtService.getUserIdFromToken(token = authHeader)

        val userSession = UserSession(
            userId = userId,
            session = session
        )

        connectionLock.write {
            sessions[session.id] = userSession

            userToSessions.compute(userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }

            val chatIds = userChats.computeIfAbsent(userId) {
                val chatIds = chatService.findChatsByUser(userId = userId).map { it.id }
                ConcurrentHashMap.newKeySet<ChatId>().apply {
                    addAll(chatIds)
                }
            }
            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }

        logger.info("Web socket connection established for user $userId with session ${session.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message: ${message.payload}")

        val userSession = connectionLock.read {
            sessions[session.id] ?: return
        }

        try {
            val incomingMessage = objectMapper.readValue(
                message.payload,
                IncomingWebSocketMessage::class.java
            )

            when (incomingMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val dto = objectMapper.readValue(
                        incomingMessage.payload,
                        SendMessageDto::class.java
                    )
                    handleSendMessage(
                        dto = dto,
                        senderId = userSession.userId
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("Error handling message from user ${userSession.userId}", e)
            sendError(
                session = session,
                error = ErrorDto(
                    message = "Incoming JSON or UUID is invalid",
                    code = "INVALID_JSON"
                )
            )
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {

    }

    private fun handleSendMessage(
        dto: SendMessageDto,
        senderId: UserId
    ) {
        val userChatIds = connectionLock.read {
            userChats[senderId] ?: return
        }
        if (dto.chatId !in userChatIds) {
            logger.warn("User $senderId tried to send message to chat ${dto.chatId} which he is not a member of")
            return
        }

        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId
        )

        broadcastToChatSessions(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(savedMessage.toChatMessageDto())
            )
        )
    }

    private fun broadcastToChatSessions(
        chatId: ChatId,
        message: OutgoingWebSocketMessage
    ) {
        val userSessions = connectionLock.read {
            chatToSessions[chatId]
                ?.mapNotNull { sessionId ->
                    sessions[sessionId]
                }
                ?: emptyList()
        }

        userSessions.forEach { userSession ->
            sendToSession(
                userSession = userSession,
                message = message
            )
        }
    }

    private fun sendToSession(
        userSession: UserSession,
        message: OutgoingWebSocketMessage
    ) {
        if (userSession.session.isOpen) {
            try {
                val messageJson = objectMapper.writeValueAsString(message)
                userSession.session.sendMessage(TextMessage(messageJson))
                logger.debug("Sent message to user {}: {}", userSession.userId, messageJson)
            } catch (e: Exception) {
                logger.error("Error sending message to user ${userSession.userId}", e)
            }
        }
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto
    ) {
        val webSocketMessage = objectMapper.writeValueAsString(
            OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.ERROR,
                payload = objectMapper.writeValueAsString(error)
            )
        )

        try {
            session.sendMessage(TextMessage(webSocketMessage))
        } catch (e: Exception) {
            logger.warn("Couldn't send error message", e)
        }
    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession
    )
}
