package lt.vitalijus.chirp.api.dto

import jakarta.validation.constraints.Size
import lt.vitalijus.chirp.domain.events.type.UserId

data class AddParticipantToChatDto(
    @field:Size(min = 1)
    val userIds: List<UserId>
)
