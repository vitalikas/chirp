package lt.vitalijus.chirp.infra.database.mappers

import lt.vitalijus.chirp.domain.model.EmailVerificationToken
import lt.vitalijus.chirp.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser()
    )
}
