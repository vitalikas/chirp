package lt.vitalijus.chirp.chat.api.dto

import lt.vitalijus.chirp.domain.events.type.UserId

data class ChatParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
