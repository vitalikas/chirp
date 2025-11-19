package lt.vitalijus.chirp.domain.events

import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.ChatMessageId

data class MessageDeletedEvent(
    val messageId: ChatMessageId,
    val chatId: ChatId
)
