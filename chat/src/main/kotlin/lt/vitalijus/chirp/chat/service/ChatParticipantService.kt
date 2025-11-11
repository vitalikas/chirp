package lt.vitalijus.chirp.chat.service

import lt.vitalijus.chirp.chat.domain.models.ChatParticipant
import lt.vitalijus.chirp.chat.infra.database.mappers.toChatParticipant
import lt.vitalijus.chirp.chat.infra.database.mappers.toChatParticipantEntity
import lt.vitalijus.chirp.chat.infra.database.repositories.ChatParticipantRepository
import lt.vitalijus.chirp.domain.events.type.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChatParticipantService(
    private val chatParticipantRepository: ChatParticipantRepository
) {

    fun createChatParticipant(
        chatParticipant: ChatParticipant
    ) {
        chatParticipantRepository.save(
            chatParticipant.toChatParticipantEntity()
        )
    }

    fun findChatParticipantById(userId: UserId): ChatParticipant? {
        return chatParticipantRepository.findByIdOrNull(userId)?.toChatParticipant()
    }

    fun findChatParticipantByEmailOrUsername(
        query: String
    ): ChatParticipant? {
        val normalizedQuery = query.lowercase().trim()
        return chatParticipantRepository.findByEmailOrUsername(query = normalizedQuery)?.toChatParticipant()
    }
}
