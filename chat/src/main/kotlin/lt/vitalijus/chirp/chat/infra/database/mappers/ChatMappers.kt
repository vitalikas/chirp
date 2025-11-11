package lt.vitalijus.chirp.chat.infra.database.mappers

import lt.vitalijus.chirp.chat.domain.models.Chat
import lt.vitalijus.chirp.chat.domain.models.ChatMessage
import lt.vitalijus.chirp.chat.domain.models.ChatParticipant
import lt.vitalijus.chirp.chat.infra.database.entities.ChatEntity
import lt.vitalijus.chirp.chat.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = this.id!!,
        chatParticipants = this.participants.map { entity ->
            entity.toChatParticipant()
        }.toSet(),
        creator = this.creator.toChatParticipant(),
        lastMessage = lastMessage,
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        createdAt = this.createdAt
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = this.userId,
        email = this.email,
        username = this.username,
        profilePictureUrl = this.profilePictureUrl
    )
}
