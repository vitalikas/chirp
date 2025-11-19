package lt.vitalijus.chirp.domain.events

import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.UserId

data class ChatParticipantJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)
