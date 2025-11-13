package lt.vitalijus.chirp.domain.exception

import lt.vitalijus.chirp.domain.events.type.UserId

class ChatParticipantNotFoundException(
    private val id: UserId
) : RuntimeException(
    "The chat participant with the ID $id was not found."
)
