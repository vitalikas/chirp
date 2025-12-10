package lt.vitalijus.chirp.domain.events

import lt.vitalijus.chirp.domain.events.type.UserId

data class ProfilePictureUpdatedEvent(
    val userId: UserId,
    val newUrl: String?
)
