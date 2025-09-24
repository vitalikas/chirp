package lt.vitalijus.chirp.api.dto

import lt.vitalijus.chirp.domain.model.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean
)
