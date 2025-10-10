package lt.vitalijus.chirp.infra.rate_limiting

import lt.vitalijus.chirp.domain.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class IpRateLimiter(
    private val redisTemplate: StringRedisTemplate
) : BaseRateLimiter() {

    companion object {
        private const val IP_RATE_LIMIT_PREFIX = "rate_limit:ip"
    }

    @Value("classpath:ip_rate_limit.lua")
    lateinit var ipRateLimitScriptResource: Resource

    private val ipRateLimitScript by lazy {
        loadScript(scriptResource = ipRateLimitScriptResource)
    }

    fun <T> withIpRateLimit(
        ipAddress: String,
        resetsIn: Duration,
        maxRequestsPerIp: Int,
        action: () -> T
    ): T {
        val key = "$IP_RATE_LIMIT_PREFIX:$ipAddress"

        val result = redisTemplate.execute(
            ipRateLimitScript,
            listOf(key),
            maxRequestsPerIp.toString(),
            resetsIn.seconds.toString()
        )

        val currentCount = result[0]

        return if (currentCount <= maxRequestsPerIp) {
            action()
        } else {
            val ttl = result[1]
            throw RateLimitException(resetInSeconds = ttl)
        }
    }
}
