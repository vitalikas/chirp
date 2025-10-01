package lt.vitalijus.chirp.infra.database.repositories

import lt.vitalijus.chirp.domain.model.UserId
import lt.vitalijus.chirp.infra.database.entities.RefreshTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, Long> {

    fun findByUserIdAndHashedToken(
        userId: UserId,
        hashedToken: String
    ): RefreshTokenEntity?

    fun deleteByUserIdAndHashedToken(
        userId: UserId,
        hashedToken: String
    )

    fun deleteByUserId(userId: UserId)
}