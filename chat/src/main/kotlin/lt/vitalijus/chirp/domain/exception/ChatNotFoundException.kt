package lt.vitalijus.chirp.domain.exception

import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.UserId

class ChatNotFoundException(
    private val id: ChatId
) : RuntimeException(
    "The chat with the ID $id was not found."
)
