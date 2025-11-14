package lt.vitalijus.chirp.infra.database.mappers

import lt.vitalijus.chirp.domain.models.Chat
import lt.vitalijus.chirp.domain.models.ChatMessage
import lt.vitalijus.chirp.domain.models.ChatParticipant
import lt.vitalijus.chirp.infra.database.entities.ChatEntity
import lt.vitalijus.chirp.infra.database.entities.ChatMessageEntity
import lt.vitalijus.chirp.infra.database.entities.ChatParticipantEntity

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

fun ChatParticipant.toChatParticipantEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = this.userId,
        email = this.email,
        username = this.username,
        profilePictureUrl = this.profilePictureUrl
    )
}

fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = this.id!!,
        chatId = this.chatId,
        sender = this.sender.toChatParticipant(),
        content = this.content,
        createdAt = this.createdAt
    )
}
