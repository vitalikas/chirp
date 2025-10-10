package lt.vitalijus.chirp.infra.rate_limiting

import org.springframework.core.io.Resource
import org.springframework.data.redis.core.script.DefaultRedisScript

abstract class BaseRateLimiter {

    protected fun loadScript(scriptResource: Resource): DefaultRedisScript<List<Long>> {
        val script = scriptResource.inputStream.use {
            it.readBytes().decodeToString()
        }
        @Suppress("UNCHECKED_CAST")
        return DefaultRedisScript(
            script,
            List::class.java as Class<List<Long>>
        )
    }
}
