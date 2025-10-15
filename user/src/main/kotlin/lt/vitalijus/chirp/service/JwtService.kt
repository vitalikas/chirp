package lt.vitalijus.chirp.service

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import lt.vitalijus.chirp.domain.exception.InvalidTokenException
import lt.vitalijus.chirp.domain.model.UserId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import kotlin.io.encoding.Base64

@Service
class JwtService(
    @param:Value("\${jwt.secret}") private val secretBase64: String,
    @param:Value("\${jwt.expiration-minutes}") private val expirationMinutes: Int
) {

    private val secretKey = Keys.hmacShaKeyFor(
        Base64.decode(secretBase64)
    )

    private val accessTokenValidityMs = expirationMinutes * 60 * 1000L
    val refreshTokenValidityMs = 30 * 24 * 60 * 60 * 1000L

    fun generateAccessToken(userId: UserId): String {
        return generateToken(
            userId = userId,
            type = "access",
            expiry = accessTokenValidityMs
        )
    }

    fun generateRefreshToken(userId: UserId): String {
        return generateToken(
            userId = userId,
            type = "refresh",
            expiry = refreshTokenValidityMs
        )
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "access"
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == "refresh"
    }

    fun getUserIdFromToken(token: String): UserId {
        val claims = parseAllClaims(token) ?: throw InvalidTokenException(
            "The attached JWT token is invalid or expired."
        )
        val userId = claims.subject
        return UUID.fromString(userId)
    }

    private fun generateToken(
        userId: UserId,
        type: String,
        expiry: Long,
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    private fun parseAllClaims(token: String): Claims? {
        val rawToken = if (token.startsWith("Bearer ")) {
            token.removePrefix("Bearer ")
        } else {
            token
        }
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (_: Exception) {
            null
        }
    }
}
