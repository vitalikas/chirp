package lt.vitalijus.chirp.api.controllers

import jakarta.validation.Valid
import lt.vitalijus.chirp.api.config.IpRateLimit
import lt.vitalijus.chirp.api.dto.*
import lt.vitalijus.chirp.api.mappers.toAuthenticatedUserDto
import lt.vitalijus.chirp.api.mappers.toUserDto
import lt.vitalijus.chirp.api.util.requestUserId
import lt.vitalijus.chirp.infra.rate_limiting.EmailRateLimiter
import lt.vitalijus.chirp.service.AuthService
import lt.vitalijus.chirp.service.EmailVerificationService
import lt.vitalijus.chirp.service.PasswordResetService
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val rateLimiter: EmailRateLimiter
) {

    @PostMapping("/register")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        timeUnit = TimeUnit.HOURS
    )
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        val user = authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()

        return user
    }

    @PostMapping("/login")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        timeUnit = TimeUnit.HOURS
    )
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        timeUnit = TimeUnit.HOURS
    )
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

    @PostMapping("/resend-verification")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        timeUnit = TimeUnit.HOURS
    )
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest
    ) {
        rateLimiter.withRateLimit(
            email = body.email,
        ) {
            emailVerificationService.resendVerificationEmail(email = body.email)
        }
    }

    @PostMapping("/forgot-password")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        timeUnit = TimeUnit.HOURS
    )
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest
    ) {
        passwordResetService.requestPasswordReset(
            email = body.email
        )
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest
    ) {
        passwordResetService.changePassword(
            userId = requestUserId,
            oldPassword = body.oldPassword,
            newPassword = body.newPassword
        )
    }
}
