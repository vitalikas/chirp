package lt.vitalijus.chirp.infra.database

import lt.vitalijus.chirp.domain.events.type.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeviceTokenRepository : JpaRepository<DeviceTokenEntity, Long> {

    fun findByUserIdIn(userIds: List<UserId>): List<DeviceTokenEntity>
    fun findByToken(token: String): DeviceTokenEntity?
    fun deleteByToken(token: String)
}
