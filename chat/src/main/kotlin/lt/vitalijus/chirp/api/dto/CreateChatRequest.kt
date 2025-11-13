package lt.vitalijus.chirp.api.dto

import jakarta.validation.constraints.Size
import lt.vitalijus.chirp.domain.events.type.UserId

data class CreateChatRequest(
    @field:Size(
        min = 1,
        message = "Chats must have at least 2 unique participants"
    )
    val otherUserIds: List<UserId>
)
