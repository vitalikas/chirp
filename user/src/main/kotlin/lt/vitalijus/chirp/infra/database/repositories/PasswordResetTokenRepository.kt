package lt.vitalijus.chirp.infra.database.repositories

import lt.vitalijus.chirp.infra.database.entities.EmailVerificationTokenEntity
import lt.vitalijus.chirp.infra.database.entities.PasswordResetTokenEntity
import lt.vitalijus.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface PasswordResetTokenRepository : JpaRepository<PasswordResetTokenEntity, Long> {

    fun findByToken(token: String): PasswordResetTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)

    @Modifying
    @Query("""
        UPDATE PasswordResetTokenEntity prt
        SET prt.usedAt = CURRENT_TIMESTAMP
        WHERE prt.user = :user
    """)
    fun invalidateActiveTokensForUser(user: UserEntity)
}
