package lt.vitalijus.chirp.infra.database.mappers

import lt.vitalijus.chirp.domain.model.User
import lt.vitalijus.chirp.infra.database.entities.UserEntity

fun UserEntity.toUser(): User = User(
    id = this.id!!,
    username = this.username,
    email = email,
    hasEmailVerified = this.hasVerifiedEmail
)