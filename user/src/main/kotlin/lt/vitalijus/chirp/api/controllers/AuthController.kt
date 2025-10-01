package lt.vitalijus.chirp.api.controllers

import jakarta.validation.Valid
import lt.vitalijus.chirp.api.dto.AuthenticatedUserDto
import lt.vitalijus.chirp.api.dto.LoginRequest
import lt.vitalijus.chirp.api.dto.RegisterRequest
import lt.vitalijus.chirp.api.dto.UserDto
import lt.vitalijus.chirp.api.mappers.toAuthenticatedUserDto
import lt.vitalijus.chirp.api.mappers.toUserDto
import lt.vitalijus.chirp.domain.model.AuthenticatedUser
import lt.vitalijus.chirp.service.auth.AuthService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()
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
}