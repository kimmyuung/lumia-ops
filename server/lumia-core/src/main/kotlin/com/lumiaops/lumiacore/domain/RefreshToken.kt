package com.lumiaops.lumiacore.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Refresh Token 엔티티
 * 서버 측에서 Refresh Token을 관리하여 토큰 탈취 시 무효화 가능
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = [
        Index(name = "idx_refresh_token", columnList = "token", unique = true),
        Index(name = "idx_refresh_token_user_id", columnList = "userId")
    ]
)
class RefreshToken(
    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, unique = true)
    val token: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false)
    var revoked: Boolean = false
        private set

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    /**
     * 토큰이 유효한지 확인
     */
    fun isValid(): Boolean = !revoked && !isExpired()

    /**
     * 토큰 만료 여부
     */
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    /**
     * 토큰 폐기
     */
    fun revoke() {
        revoked = true
    }
}
