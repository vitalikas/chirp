package lt.vitalijus.chirp.service

import lt.vitalijus.chirp.api.dto.ChatMessageDto
import lt.vitalijus.chirp.api.mappers.toChatMessageDto
import lt.vitalijus.chirp.domain.events.ChatParticipantJoinedEvent
import lt.vitalijus.chirp.domain.events.ChatParticipantLeftEvent
import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.UserId
import lt.vitalijus.chirp.domain.exception.ChatNotFoundException
import lt.vitalijus.chirp.domain.exception.ChatParticipantNotFoundException
import lt.vitalijus.chirp.domain.exception.ForbiddenException
import lt.vitalijus.chirp.domain.exception.InvalidChatSizeException
import lt.vitalijus.chirp.domain.models.Chat
import lt.vitalijus.chirp.domain.models.ChatMessage
import lt.vitalijus.chirp.infra.database.entities.ChatEntity
import lt.vitalijus.chirp.infra.database.mappers.toChat
import lt.vitalijus.chirp.infra.database.mappers.toChatMessage
import lt.vitalijus.chirp.infra.database.repositories.ChatMessageRepository
import lt.vitalijus.chirp.infra.database.repositories.ChatParticipantRepository
import lt.vitalijus.chirp.infra.database.repositories.ChatRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>
    ): Chat {
        val otherParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds
        )

        val allParticipants = (otherParticipants + creatorId)
        if (allParticipants.size < 2) {
            throw InvalidChatSizeException()
        }

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(id = creatorId)

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = setOf(creator) + otherParticipants,
            )
        ).toChat(lastMessage = null)
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId) ?: throw ChatNotFoundException(id = chatId)

        val isRequestingUserInChat = chat.participants.any {
            it.userId == requestUserId
        }
        if (!isRequestingUserInChat) {
            throw ForbiddenException()
        }

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId) ?: throw ChatParticipantNotFoundException(id = userId)
        }

        val lastMessage = lastMessageForChat(chatId = chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toChat(lastMessage = lastMessage)

        applicationEventPublisher.publishEvent(
            ChatParticipantJoinedEvent(
                chatId = chatId,
                userIds = userIds
            )
        )

        return updatedChat
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId
    ) {
        val chat = chatRepository.findByIdOrNull(chatId) ?: throw ChatNotFoundException(id = chatId)
        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(id = userId)

        val newParticipantsSize = chat.participants.size - 1
        if (newParticipantsSize == 0) {
            chatRepository.deleteById(chatId)
            return
        }

        val updatedChat = chat.apply {
            this.participants = chat.participants - participant
        }
        chatRepository.save(updatedChat)

        applicationEventPublisher.publishEvent(
            ChatParticipantLeftEvent(
                chatId = chatId,
                userId = userId
            )
        )
    }

    fun getChatMessages(
        chatId: ChatId,
        before: Instant?,
        pageSize: Int
    ): List<ChatMessageDto> {
        return chatMessageRepository
            .findByChatIdBefore(
                chatId = chatId,
                before = before ?: Instant.now(),
                pageable = PageRequest.of(0, pageSize)
            )
            .content
            .asReversed()
            .map { it.toChatMessage().toChatMessageDto() }
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository.findLatestMessagesByChatIds(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}
