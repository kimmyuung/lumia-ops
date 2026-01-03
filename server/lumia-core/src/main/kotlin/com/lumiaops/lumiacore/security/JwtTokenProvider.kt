package com.lumiaops.lumiacore.security

import com.lumiaops.lumiacore.config.JwtProperties
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

/**
 * JWT 토큰 생성 및 검증을 담당하는 서비스
 */
@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val secretKey: SecretKey by lazy {
        val decodedKey = Base64.getDecoder().decode(jwtProperties.secret)
        Keys.hmacShaKeyFor(decodedKey)
    }

    /**
     * 액세스 토큰 생성
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     * @return JWT 액세스 토큰
     */
    fun generateAccessToken(userId: Long, email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.expirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", "access")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 리프레시 토큰 생성
     * @param userId 사용자 ID
     * @return JWT 리프레시 토큰
     */
    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtProperties.refreshExpirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    /**
     * 토큰에서 사용자 ID 추출
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    fun getUserIdFromToken(token: String): Long {
        val claims = getClaims(token)
        return claims.subject.toLong()
    }

    /**
     * 토큰에서 이메일 추출
     * @param token JWT 토큰
     * @return 사용자 이메일
     */
    fun getEmailFromToken(token: String): String {
        val claims = getClaims(token)
        return claims.get("email", String::class.java)
    }

    /**
     * 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효 여부
     */
    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: SignatureException) {
            log.error("잘못된 JWT 서명입니다")
            false
        } catch (e: MalformedJwtException) {
            log.error("잘못된 JWT 형식입니다")
            false
        } catch (e: ExpiredJwtException) {
            log.error("만료된 JWT 토큰입니다")
            false
        } catch (e: UnsupportedJwtException) {
            log.error("지원되지 않는 JWT 토큰입니다")
            false
        } catch (e: IllegalArgumentException) {
            log.error("JWT 토큰이 비어있습니다")
            false
        }
    }

    /**
     * 토큰이 액세스 토큰인지 확인
     */
    fun isAccessToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            claims.get("type", String::class.java) == "access"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 토큰이 리프레시 토큰인지 확인
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = getClaims(token)
            claims.get("type", String::class.java) == "refresh"
        } catch (e: Exception) {
            false
        }
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
