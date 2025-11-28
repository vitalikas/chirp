package lt.vitalijus.chirp.api.dto.ws

import lt.vitalijus.chirp.domain.events.type.ChatId
import lt.vitalijus.chirp.domain.events.type.UserId

data class ChatParticipantsChangedDto(
    val chatId: ChatId,
    val userIds: Set<UserId>
)
