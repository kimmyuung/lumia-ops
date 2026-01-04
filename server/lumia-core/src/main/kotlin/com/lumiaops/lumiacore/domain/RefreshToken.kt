package com.lumiaops.lumiacore.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * Refresh Token 엔티티
 * 서버 측에서 발급된 refresh token을 관리
 */
@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val userId: Long,
    
    @Column(nullable = false, unique = true, length = 500)
    val token: String,
    
    @Column(nullable = false)
    val expiresAt: LocalDateTime,
    
    @Column(nullable = false)
    var revoked: Boolean = false,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    /**
     * 토큰 폐기
     */
    fun revoke() {
        this.revoked = true
    }
    
    /**
     * 토큰 유효성 검사
     */
    fun isValid(): Boolean {
        return !revoked && expiresAt.isAfter(LocalDateTime.now())
    }
}
