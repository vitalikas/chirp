package lt.vitalijus.chirp.api.dto

import jakarta.validation.constraints.NotBlank
import lt.vitalijus.chirp.api.util.Password

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,
    @field:Password
    val newPassword: String
)
