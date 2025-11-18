package lt.vitalijus.chirp.api.controllers

import lt.vitalijus.chirp.api.util.requestUserId
import lt.vitalijus.chirp.domain.events.type.ChatMessageId
import lt.vitalijus.chirp.service.ChatMessageService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/messages")
class ChatMessageController(
    private val chatMessageService: ChatMessageService
) {

    @DeleteMapping("/{messageId}")
    fun deleteMessage(
        @PathVariable("messageId") messageId: ChatMessageId
    ) {
        chatMessageService.deleteMessage(
            messageId = messageId,
            requestUserId = requestUserId
        )
    }
}
