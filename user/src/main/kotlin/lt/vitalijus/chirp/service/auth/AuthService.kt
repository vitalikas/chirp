package lt.vitalijus.chirp.service.auth

import lt.vitalijus.chirp.domain.exception.InvalidCredentialsException
import lt.vitalijus.chirp.domain.exception.PasswordEncodingException
import lt.vitalijus.chirp.domain.exception.UserAlreadyExistsException
import lt.vitalijus.chirp.domain.exception.UserNotFoundException
import lt.vitalijus.chirp.domain.model.AuthenticatedUser
import lt.vitalijus.chirp.domain.model.User
import lt.vitalijus.chirp.domain.model.UserId
import lt.vitalijus.chirp.infra.database.entities.RefreshTokenEntity
import lt.vitalijus.chirp.infra.database.entities.UserEntity
import lt.vitalijus.chirp.infra.database.mappers.toUser
import lt.vitalijus.chirp.infra.database.repositories.RefreshTokenRepository
import lt.vitalijus.chirp.infra.database.repositories.UserRepository
import lt.vitalijus.chirp.infra.security.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun register(
        email: String,
        username: String,
        password: String
    ): User {
        val user = userRepository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim()
        )

        if (user != null) {
            throw UserAlreadyExistsException()
        }

        val savedUser = userRepository.save(
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(rawPassword = password) ?: throw PasswordEncodingException()
            )
        ).toUser()

        return savedUser
    }

    fun login(
        email: String,
        password: String
    ): AuthenticatedUser {
        val user = userRepository.findByEmail(email = email.trim()) ?: throw InvalidCredentialsException()

        val matches = passwordEncoder.matches(
            rawPassword = password,
            encodedPassword = user.hashedPassword
        )
        if (!matches) throw InvalidCredentialsException()

        // TODO: Check for verified email

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId = userId)
            val refreshToken = jwtService.generateRefreshToken(userId = userId)
            storeRefreshToken(
                userId = userId,
                token = refreshToken
            )

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        } ?: throw UserNotFoundException()
    }

    private fun storeRefreshToken(
        userId: UserId,
        token: String
    ) {
        val hashedToken = hashToken(token = token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val createdAt = Instant.now()
        val expiresAt = createdAt.plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashedToken
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}
