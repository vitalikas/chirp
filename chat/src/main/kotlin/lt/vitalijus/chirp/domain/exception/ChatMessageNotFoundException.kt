package lt.vitalijus.chirp.domain.exception

import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.ChatMessageId
import lt.vitalijus.chirp.domain.events.type.UserId

class ChatMessageNotFoundException(
    private val id: ChatMessageId
) : RuntimeException(
    "The chat message with the ID $id was not found."
)
