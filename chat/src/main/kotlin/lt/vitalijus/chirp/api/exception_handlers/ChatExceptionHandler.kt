package lt.vitalijus.chirp.api.exception_handlers

import lt.vitalijus.chirp.domain.exception.ChatMessageNotFoundException
import lt.vitalijus.chirp.domain.exception.ChatNotFoundException
import lt.vitalijus.chirp.domain.exception.ChatParticipantNotFoundException
import lt.vitalijus.chirp.domain.exception.InvalidChatSizeException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        ChatParticipantNotFoundException::class,
        ChatMessageNotFoundException::class
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onChatNotFoundException(e: Exception) = mapOf(
        "code" to "NOT_FOUND",
        "message" to e.message
    )

    @ExceptionHandler(InvalidChatSizeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidChatSizeException(e: InvalidChatSizeException) = mapOf(
        "code" to "INVALID_CHAT_SIZE",
        "message" to e.message
    )
}
