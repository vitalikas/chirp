package lt.vitalijus.chirp.domain.exception

import java.lang.RuntimeException

class UnauthorizedException : RuntimeException("Missing auth details")
