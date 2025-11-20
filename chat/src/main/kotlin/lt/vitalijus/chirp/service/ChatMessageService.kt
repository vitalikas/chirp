package lt.vitalijus.chirp.service

import lt.vitalijus.chirp.domain.events.MessageDeletedEvent
import lt.vitalijus.chirp.domain.events.chat.ChatEvent
import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.ChatMessageId
import lt.vitalijus.chirp.domain.events.type.UserId
import lt.vitalijus.chirp.domain.exception.ChatMessageNotFoundException
import lt.vitalijus.chirp.domain.exception.ChatNotFoundException
import lt.vitalijus.chirp.domain.exception.ChatParticipantNotFoundException
import lt.vitalijus.chirp.domain.exception.ForbiddenException
import lt.vitalijus.chirp.domain.models.ChatMessage
import lt.vitalijus.chirp.infra.database.entities.ChatMessageEntity
import lt.vitalijus.chirp.infra.database.mappers.toChatMessage
import lt.vitalijus.chirp.infra.database.repositories.ChatMessageRepository
import lt.vitalijus.chirp.infra.database.repositories.ChatParticipantRepository
import lt.vitalijus.chirp.infra.database.repositories.ChatRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Transactional
    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null
    ): ChatMessage {
        val chat = chatRepository.findChatById(
            id = chatId,
            userId = senderId
        ) ?: throw ChatNotFoundException(id = chatId)

        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(id = senderId)

        val savedMessage = chatMessageRepository.saveAndFlush(
            ChatMessageEntity(
                id = messageId,
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender
            )
        )

        applicationEventPublisher.publishEvent(
            ChatEvent.NewMessage(
                senderId = sender.userId,
                senderUsername = sender.username,
                recipientIds = chat.participants.map { it.userId }.toSet(),
                chatId = chatId,
                message = savedMessage.content
            )
        )

        return savedMessage.toChatMessage()
    }

    @CacheEvict(
        value = ["messages"],
        key = "#result.chatId"
    )
    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw ChatMessageNotFoundException(id = messageId)

        if (message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)

        applicationEventPublisher.publishEvent(
            MessageDeletedEvent(
                messageId = messageId,
                chatId = message.chatId
            )
        )

        evictMessagesCache(chatId = message.chatId)
    }

    @CacheEvict(
        value = ["messages"],
        key = "#chatId"
    )
    fun evictMessagesCache(chatId: ChatId) {
        // NO-OP: Let Spring handle the cache evict
    }
}
