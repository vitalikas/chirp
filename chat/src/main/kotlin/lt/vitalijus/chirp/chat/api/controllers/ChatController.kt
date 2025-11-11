package lt.vitalijus.chirp.chat.api.controllers

import jakarta.validation.Valid
import lt.vitalijus.chirp.api.util.requestUserId
import lt.vitalijus.chirp.chat.api.dto.ChatDto
import lt.vitalijus.chirp.chat.api.dto.CreateChatRequest
import lt.vitalijus.chirp.chat.api.mappers.toChatDto
import lt.vitalijus.chirp.chat.service.ChatService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    @PostMapping
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet()
        ).toChatDto()
    }
}
