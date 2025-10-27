package lt.vitalijus.chirp.chat.domain.models

import lt.vitalijus.chirp.domain.events.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?
)
