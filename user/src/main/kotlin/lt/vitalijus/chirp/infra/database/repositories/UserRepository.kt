package lt.vitalijus.chirp.infra.database.repositories

import lt.vitalijus.chirp.domain.events.type.UserId
import lt.vitalijus.chirp.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, UserId> {

    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String, username: String): UserEntity?
}
