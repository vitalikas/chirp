package lt.vitalijus.chirp.api.mappers

import lt.vitalijus.chirp.api.dto.ChatDto
import lt.vitalijus.chirp.api.dto.ChatMessageDto
import lt.vitalijus.chirp.api.dto.ChatParticipantDto
import lt.vitalijus.chirp.domain.models.Chat
import lt.vitalijus.chirp.domain.models.ChatMessage
import lt.vitalijus.chirp.domain.models.ChatParticipant

fun Chat.toChatDto(): ChatDto {
    return ChatDto(
        id = id,
        participants = chatParticipants.map { chatParticipant ->
            chatParticipant.toChatParticipantDto()
        },
        lastActivityAt = lastActivityAt,
        lastMessage = lastMessage?.toChatMessageDto(),
        creator = creator.toChatParticipantDto()
    )
}

fun ChatMessage.toChatMessageDto(): ChatMessageDto {
    return ChatMessageDto(
        id = id,
        chatId = chatId,
        content = content,
        createdAt = createdAt,
        senderId = sender.userId
    )
}

fun ChatParticipant.toChatParticipantDto(): ChatParticipantDto {
    return ChatParticipantDto(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl
    )
}
