package lt.vitalijus.chirp.api.exception_handlers

import lt.vitalijus.chirp.domain.exception.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AuthExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun onUserAlreadyExistsException(
        e: UserAlreadyExistsException
    ) = mapOf(
        "code" to "USER_ALREADY_EXISTS",
        "error" to e.message
    )

    @ExceptionHandler(PasswordEncodingException::class)
    fun onPasswordEncodingException(
        e: PasswordEncodingException
    ): ResponseEntity<Map<String, Any>> {
        val error = e.message ?: "Password encoding failed"
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                mapOf(
                    "code" to "PASSWORD_ENCODING_FAILED",
                    "error" to error
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun onValidationException(
        e: MethodArgumentNotValidException
    ): ResponseEntity<Map<String, Any>> {
        val errors = e.bindingResult.allErrors.map {
            it.defaultMessage ?: "Invalid value"
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                mapOf(
                    "code" to "VALIDATION_ERROR",
                    "errors" to errors
                )
            )
    }

    @ExceptionHandler(InvalidTokenException::class)
    fun onInvalidTokenException(
        e: InvalidTokenException
    ): ResponseEntity<Map<String, Any>> {
        val error = e.message ?: "Invalid token"
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                mapOf(
                    "code" to "INVALID_TOKEN",
                    "error" to error
                )
            )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun onUserNotFoundException(
        e: UserNotFoundException
    ): ResponseEntity<Map<String, Any>> {
        val error = e.message ?: "User not found"
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                mapOf(
                    "code" to "USER_NOT_FOUND",
                    "error" to error
                )
            )
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun onInvalidCredentialsException(
        e: InvalidCredentialsException
    ): ResponseEntity<Map<String, Any>> {
        val error = e.message ?: "User not found"
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                mapOf(
                    "code" to "INVALID_CREDENTIALS",
                    "error" to error
                )
            )
    }

    @ExceptionHandler(EmailNotVerifiedException::class)
    fun onEmailNotVerifiedException(
        e: EmailNotVerifiedException
    ): ResponseEntity<Map<String, Any>> {
        val error = e.message ?: "Email not verified"
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                mapOf(
                    "code" to "EMAIL_NOT_VERIFIED",
                    "error" to error
                )
            )
    }

    @ExceptionHandler(SamePasswordException::class)
    fun onSamePasswordException(
        e: SamePasswordException
    ): ResponseEntity<Map<String, Any>> {
        val error = e.message ?: "New password must be different from the current password"
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                mapOf(
                    "code" to "SAME_PASSWORD",
                    "error" to error
                )
            )
    }
}
