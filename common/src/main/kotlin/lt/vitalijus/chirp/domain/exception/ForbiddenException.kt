package lt.vitalijus.chirp.domain.exception

import java.lang.RuntimeException

class ForbiddenException : RuntimeException("You are not allowed to access this resource.")
