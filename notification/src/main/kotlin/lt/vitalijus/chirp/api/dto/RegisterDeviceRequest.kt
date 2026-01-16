package lt.vitalijus.chirp.api.dto

import jakarta.validation.constraints.NotBlank

data class RegisterDeviceRequest(
    @field:NotBlank
    val token: String,
    val platformDto: PlatformDto
)

enum class PlatformDto {
    ANDROID, IOS
}
