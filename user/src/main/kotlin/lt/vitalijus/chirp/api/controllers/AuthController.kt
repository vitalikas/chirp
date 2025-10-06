package lt.vitalijus.chirp.api.controllers

import jakarta.validation.Valid
import lt.vitalijus.chirp.api.dto.*
import lt.vitalijus.chirp.api.mappers.toAuthenticatedUserDto
import lt.vitalijus.chirp.api.mappers.toUserDto
import lt.vitalijus.chirp.service.AuthService
import lt.vitalijus.chirp.service.EmailVerificationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        val user = authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()

        val verificationToken = emailVerificationService.createVerificationToken(email = body.email)
        return user
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return authService.refresh(
            refreshToken = body.refreshToken
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(
        @RequestBody body: LogoutRequest
    ) {
        authService.logout(refreshToken = body.refreshToken)
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token = token)
    }
}
