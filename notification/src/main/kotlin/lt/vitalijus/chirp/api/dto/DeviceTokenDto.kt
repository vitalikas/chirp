package lt.vitalijus.chirp.api.dto

import lt.vitalijus.chirp.domain.events.type.UserId
import java.time.Instant

data class DeviceTokenDto(
    val userId: UserId,
    val token: String,
    val createdAt: Instant
)
