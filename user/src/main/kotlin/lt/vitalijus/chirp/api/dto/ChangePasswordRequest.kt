package lt.vitalijus.chirp.api.dto

import jakarta.validation.constraints.NotBlank
import lt.vitalijus.chirp.api.util.Password

data class ChangePasswordRequest(
    @field:NotBlank
    val oldPassword: String,
    @field:Password
    val newPassword: String
)
