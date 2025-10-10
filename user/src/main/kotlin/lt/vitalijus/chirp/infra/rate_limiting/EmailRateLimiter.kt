package lt.vitalijus.chirp.infra.rate_limiting

import lt.vitalijus.chirp.domain.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class EmailRateLimiter(
    private val redisTemplate: StringRedisTemplate
) : BaseRateLimiter() {

    companion object {
        private const val EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"
        private const val EMAIL_ATTEMPT_COUNT_PREFIX = "attempt_count:email"
    }

    @Value("classpath:email_rate_limit.lua")
    lateinit var emailRateLimitScriptResource: Resource

    private val emailRateLimitScript by lazy {
        loadScript(scriptResource = emailRateLimitScriptResource)
    }

    fun withRateLimit(
        email: String,
        action: () -> Unit
    ) {
        val normalizedEmail = email.lowercase().trim()

        val rateLimitKey = "$EMAIL_RATE_LIMIT_PREFIX:$normalizedEmail"
        val attemptCountKey = "$EMAIL_ATTEMPT_COUNT_PREFIX:$normalizedEmail"

        val result = redisTemplate.execute(
            emailRateLimitScript,
            listOf(rateLimitKey, attemptCountKey)
        )

        val attemptCount = result[0]
        val ttl = result[1]

        if (attemptCount == -1L) {
            throw RateLimitException(ttl)
        }

        action()
    }
}
