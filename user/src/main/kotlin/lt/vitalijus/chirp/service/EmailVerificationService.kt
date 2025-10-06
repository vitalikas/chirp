package lt.vitalijus.chirp.service

import lt.vitalijus.chirp.domain.exception.InvalidTokenException
import lt.vitalijus.chirp.domain.exception.UserNotFoundException
import lt.vitalijus.chirp.domain.model.EmailVerificationToken
import lt.vitalijus.chirp.infra.database.entities.EmailVerificationTokenEntity
import lt.vitalijus.chirp.infra.database.mappers.toEmailVerificationToken
import lt.vitalijus.chirp.infra.database.repositories.EmailVerificationTokenRepository
import lt.vitalijus.chirp.infra.database.repositories.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class EmailVerificationService(
    private val emailVerificationRepository: EmailVerificationTokenRepository,
    private val userRepository: UserRepository,
    @param:Value("\${chirp.email.verification.expiry-hours}") private val expiryHours: Long
) {

    @Transactional
    fun createVerificationToken(email: String): EmailVerificationToken {
        val userEntity = userRepository.findByEmail(email = email) ?: throw UserNotFoundException()

        emailVerificationRepository.invalidateActiveTokensForUser(user = userEntity)

        val token = EmailVerificationTokenEntity(
            expiresAt = Instant.now().plus(expiryHours, ChronoUnit.HOURS),
            user = userEntity,
        )
        emailVerificationRepository.save(token)

        return token.toEmailVerificationToken()
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verificationToken = emailVerificationRepository.findByToken(token)
            ?: throw InvalidTokenException("Email verification token not found.")

        if (verificationToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used.")
        }

        if (verificationToken.isExpired) {
            throw InvalidTokenException("Email verification token is expired.")
        }

        emailVerificationRepository.save(
            verificationToken.apply {
                this.usedAt = Instant.now()
            }
        )

        userRepository.save(
            verificationToken.user.apply {
                this.hasVerifiedEmail = true
            }
        )
    }

    @Scheduled(cron = "\${chirp.email.verification.cleanup-cron}", zone = "\${chirp.email.verification.cleanup-zone}")
    fun cleanupExpiredTokens() {
        emailVerificationRepository.deleteByExpiresAtLessThan(
            now = Instant.now()
        )
    }
}
