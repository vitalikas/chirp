package lt.vitalijus.chirp.api.dto

import jakarta.validation.constraints.Email
import lt.vitalijus.chirp.api.util.Password
import org.hibernate.validator.constraints.Length

data class RegisterRequest(
    @field:Email(message = "Must be a valid email address")
    val email: String,
    @field:Length(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    val username: String,
    @field:Password
    val password: String
)
