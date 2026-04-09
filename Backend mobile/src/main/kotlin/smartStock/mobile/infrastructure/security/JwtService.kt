package smartStock.mobile.infrastructure.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.issuer}") private val issuer: String,
    @Value("\${app.jwt.audience}") private val audience: String,
    @Value("\${app.jwt.expiration-minutes}") private val expirationMinutes: Long,
) {
    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    }

    fun generateToken(
        userId: Int,
        email: String,
        roleId: Int,
        roleName: String,
        name: String,
    ): String {
        val now = Instant.now()
        val expiration = now.plusSeconds(expirationMinutes * 60)

        return Jwts
            .builder()
            .subject(userId.toString())
            .issuer(issuer)
            .audience().add(audience).and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .claim("email", email)
            .claim("rolId", roleId)
            .claim("role", roleName.lowercase())
            .claim("nombre", name)
            .signWith(signingKey)
            .compact()
    }

    fun isTokenValid(token: String): Boolean {
        return runCatching {
            val claims = parseClaims(token)
            val validAudience = claims.audience?.contains(audience) ?: false
            claims.issuer == issuer && validAudience && claims.expiration.after(Date())
        }.getOrDefault(false)
    }

    fun extractUserId(token: String): Int? = runCatching { parseClaims(token).subject?.toInt() }.getOrNull()

    fun extractEmail(token: String): String? = runCatching { parseClaims(token).get("email", String::class.java) }.getOrNull()

    fun extractRole(token: String): String? = runCatching { parseClaims(token).get("role", String::class.java) }.getOrNull()

    fun extractRoleId(token: String): Int? = runCatching {
        val claim = parseClaims(token).get("rolId")
        when (claim) {
            is Int -> claim
            is Number -> claim.toInt()
            is String -> claim.toIntOrNull()
            else -> null
        }
    }.getOrNull()

    fun extractName(token: String): String? = runCatching { parseClaims(token).get("nombre", String::class.java) }.getOrNull()

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
