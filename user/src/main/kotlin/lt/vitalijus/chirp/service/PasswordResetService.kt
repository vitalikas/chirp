package lt.vitalijus.chirp.service

import lt.vitalijus.chirp.domain.events.type.UserId
import lt.vitalijus.chirp.domain.exception.*
import lt.vitalijus.chirp.infra.database.entities.PasswordResetTokenEntity
import lt.vitalijus.chirp.infra.database.repositories.PasswordResetTokenRepository
import lt.vitalijus.chirp.infra.database.repositories.RefreshTokenRepository
import lt.vitalijus.chirp.infra.database.repositories.UserRepository
import lt.vitalijus.chirp.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${chirp.email.reset-password.expiry-minutes}")
    private val expiryMinutes: Long
) {

    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email = email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user = user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(token)

        // TODO: Inform notification service about password reset trigger to send email
    }

    @Transactional
    fun resetPassword(
        token: String,
        newPassword: String
    ) {
        val resetToken = passwordResetTokenRepository.findByToken(token = token)
            ?: throw InvalidTokenException("Invalid password reset token")

        if (resetToken.isUsed) {
            throw InvalidTokenException("Password reset token is already used.")
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException("Password reset token is expired.")
        }

        val user = resetToken.user

        if (passwordEncoder.matches(rawPassword = newPassword, encodedPassword = user.hashedPassword)) {
            throw SamePasswordException()
        }

        val hashedNewPassword = passwordEncoder.encode(newPassword) ?: throw PasswordEncodingException()
        userRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword
            }
        )

        passwordResetTokenRepository.invalidateActiveTokensForUser(user = user)

        refreshTokenRepository.deleteByUserId(userId = user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String
    ) {
        val user = userRepository.findByIdOrNull(id = userId) ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(rawPassword = oldPassword, encodedPassword = user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(userId = user.id!!)

        val hashedNewPassword = passwordEncoder.encode(newPassword) ?: throw PasswordEncodingException()
        userRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword
            }
        )
    }

    @Scheduled(cron = "\${chirp.email.reset-password.cleanup-cron}", zone = "\${chirp.email.reset-password.cleanup-zone}")
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}
