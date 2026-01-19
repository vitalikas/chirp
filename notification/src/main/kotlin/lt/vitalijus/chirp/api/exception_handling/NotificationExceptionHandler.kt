package lt.vitalijus.chirp.api.exception_handling

import lt.vitalijus.chirp.domain.exception.InvalidDeviceTokenException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class NotificationExceptionHandler {

    @ExceptionHandler(InvalidDeviceTokenException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidDeviceTokenException(
        e: InvalidDeviceTokenException
    ) = mapOf(
        "code" to "INVALID_DEVICE_TOKEN",
        "error" to e.message
    )
}
