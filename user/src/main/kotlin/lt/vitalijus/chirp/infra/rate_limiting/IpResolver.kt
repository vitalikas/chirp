package lt.vitalijus.chirp.infra.rate_limiting

import jakarta.servlet.http.HttpServletRequest
import lt.vitalijus.chirp.infra.config.NginxConfig
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginxConfig: NginxConfig
) {

    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8", // RFC 1918
            "172.16.0.0/12", // RFC 1918
            "192.168.0.0/16", // RFC 1918
            "127.0.0.0/8", // Loopback
            "::1/128", // Single IPv6 address localhost
            "fc00::/7", // IPv6 private network
            "fe80::/10" // IPv6 link-local
        ).map { IpAddressMatcher(it) }

        private val INVALID_IPS = listOf(
            "unknown",
            "unavailable",
            "0.0.0.0",
            "::"
        )
    }

    private enum class IpVersion {
        IPV4, IPV6, INVALID
    }

    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val trustedMatchers = nginxConfig
        .trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            val cidr = when {
                proxy.contains("/") -> proxy // Already has CIDR: "192.168.1.0/24"
                proxy.count { it == ':' } >= 2 -> "$proxy/128" // IPv6: "2001:db8::1" → "2001:db8::1/128"
                else -> "$proxy/32" // IPv4: "192.168.1.1" → "192.168.1.1/32"
            }
            IpAddressMatcher(cidr)
        }

    private val IPV4_PATTERN = Regex(
        "^(?:(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$"
    )

    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr

        logger.warn(
            "IP DEBUG → remoteAddr={}, X-Real-IP={}, X-Forwarded-For={}",
            remoteAddr,
            request.getHeader("X-Real-IP"),
            request.getHeader("X-Forwarded-For")
        )

        if (!isFromTrustedProxy(ip = remoteAddr)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct connection attempt from $remoteAddr")
                throw SecurityException("No valid client IP in proxy header")
            }

            return remoteAddr
        }

        val clientIp = extractFromXRealIp(request = request)

        if (clientIp == null) {
            logger.warn("No valid client IP in proxy header")
            if (nginxConfig.requireProxy) {
                throw SecurityException("No valid client IP in proxy header")
            }
        }

        return clientIp ?: remoteAddr
    }

    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { matcher ->
            matcher.matches(ip)
        }
    }

    private fun extractFromXRealIp(request: HttpServletRequest): String? {
        val headerName = "X-Real-IP"
        return request.getHeader(headerName)?.let { realIp ->
            validateAndNormalizeIp(
                realIp = realIp,
                headerName = headerName,
                proxyIp = request.remoteAddr
            )
        }
    }

    private fun validateAndNormalizeIp(
        realIp: String,
        headerName: String,
        proxyIp: String
    ): String? {
        val trimmedRealIp = realIp.trim()

        if (trimmedRealIp.isBlank() || INVALID_IPS.contains(trimmedRealIp)) {
            logger.debug("Invalid IP in $headerName: $trimmedRealIp from proxy $proxyIp")
            return null
        }

        val ipVersion = determineIpVersion(ip = trimmedRealIp)
        if (ipVersion == IpVersion.INVALID) {
            logger.warn("Invalid IP format in $headerName: $trimmedRealIp from proxy $proxyIp")
            return null
        }

        return try {
            val inetAddr = when (ipVersion) {
                IpVersion.IPV4 -> Inet4Address.getByName(trimmedRealIp)
                IpVersion.IPV6 -> Inet6Address.getByName(trimmedRealIp)
                IpVersion.INVALID -> throw Exception("Invalid IP format") // Should never reach here
            }

            val ipAddr = inetAddr.hostAddress

            if (isPrivateIp(ip = ipAddr)) {
                logger.debug("Private ${ipVersion.name} IP in $headerName: $trimmedRealIp from proxy $proxyIp")
            } else {
                logger.debug("Public ${ipVersion.name} IP in $headerName: $trimmedRealIp from proxy $proxyIp")
            }

            ipAddr
        } catch (e: Exception) {
            logger.warn("Failed to parse ${ipVersion.name} IP in $headerName: $trimmedRealIp from proxy $proxyIp", e)
            null
        }
    }

    private fun determineIpVersion(ip: String): IpVersion {
        return when {
            ip.matches(IPV4_PATTERN) -> IpVersion.IPV4

            ip.contains(":") -> {
                // Check if it's IPv4 with port (not IPv6)
                if (ip.count { it == ':' } == 1) {
                    val parts = ip.split(":")
                    if (parts.size == 2 && parts[0].matches(IPV4_PATTERN)) {
                        return IpVersion.INVALID // IPv4:port is invalid
                    }
                }
                IpVersion.IPV6
            }

            else -> IpVersion.INVALID
        }
    }

    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { it.matches(ip) }
    }
}
