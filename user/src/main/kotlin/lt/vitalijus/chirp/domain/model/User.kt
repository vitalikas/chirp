package lt.vitalijus.chirp.domain.model

import lt.vitalijus.chirp.domain.events.type.UserId

data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean
)
