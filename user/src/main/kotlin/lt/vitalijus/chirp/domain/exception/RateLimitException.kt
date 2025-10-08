package lt.vitalijus.chirp.domain.exception

class RateLimitException(
    resetInSeconds: Long
) : RuntimeException(
    "Rate limit exceeded. Try again in $resetInSeconds seconds."
)
