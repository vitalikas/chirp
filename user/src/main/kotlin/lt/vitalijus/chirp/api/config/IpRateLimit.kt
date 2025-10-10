package lt.vitalijus.chirp.api.config

import java.util.concurrent.TimeUnit

annotation class IpRateLimit(
    val requests: Int = 60,
    val duration: Long = 1L,
    val timeUnit: TimeUnit = TimeUnit.MINUTES
)
