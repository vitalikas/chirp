package lt.vitalijus.chirp.domain.models

import lt.vitalijus.chirp.domain.events.type.ChatId
import java.time.Instant

data class Chat(
    val id: ChatId,
    val chatParticipants: Set<ChatParticipant>,
    val lastMessage: ChatMessage?,
    val creator: ChatParticipant,
    val lastActivityAt: Instant,
    val createdAt: Instant
)
