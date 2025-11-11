package lt.vitalijus.chirp.chat.service

import lt.vitalijus.chirp.chat.domain.exception.ChatParticipantNotFoundException
import lt.vitalijus.chirp.chat.domain.exception.InvalidChatSizeException
import lt.vitalijus.chirp.chat.domain.models.Chat
import lt.vitalijus.chirp.chat.infra.database.entities.ChatEntity
import lt.vitalijus.chirp.chat.infra.database.mappers.toChat
import lt.vitalijus.chirp.chat.infra.database.repositories.ChatParticipantRepository
import lt.vitalijus.chirp.chat.infra.database.repositories.ChatRepository
import lt.vitalijus.chirp.domain.events.type.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository
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
}
