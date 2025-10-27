package lt.vitalijus.chirp.chat.domain.models

import lt.vitalijus.chirp.domain.events.type.ChatId
import java.time.Instant

data class Chat(
    val chatId: ChatId,
    val chatParticipants: Set<ChatParticipant>,
    val lastMessage: ChatMessage?,
    val creator: ChatParticipant,
    val lastActivityAt: Instant,
    val createdAt: Instant
)
