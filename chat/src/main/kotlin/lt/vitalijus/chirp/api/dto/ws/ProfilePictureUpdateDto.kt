package lt.vitalijus.chirp.api.dto.ws

import lt.vitalijus.chirp.domain.events.type.UserId

data class ProfilePictureUpdateDto(
    val userId: UserId,
    val newUrl: String?
)
