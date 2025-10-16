package lt.vitalijus.chirp.api.util

import lt.vitalijus.chirp.domain.exception.UnauthorizedException
import lt.vitalijus.chirp.domain.events.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId ?: throw UnauthorizedException()
