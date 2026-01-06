package com.lumiaops.lumiacore.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 토큰 블랙리스트 엔티티
 * 로그아웃 시 Access Token을 블랙리스트에 추가하여 재사용 방지
 */
@Entity
@Table(
    name = "token_blacklist",
    indexes = [
        Index(name = "idx_token_blacklist_hash", columnList = "tokenHash", unique = true),
        Index(name = "idx_token_blacklist_expires", columnList = "expiresAt")
    ]
)
class TokenBlacklist(
    /**
     * 토큰 해시 (SHA-256)
     * 전체 토큰 저장 대신 해시로 저장하여 보안 및 저장 공간 최적화
     */
    @Column(nullable = false, unique = true, length = 64)
    val tokenHash: String,

    /**
     * 토큰 만료 시간
     * 이 시간 이후에는 DB에서 삭제 가능 (정리 스케줄러용)
     */
    @Column(nullable = false)
    val expiresAt: LocalDateTime
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    /**
     * 만료 여부 확인 (정리 대상)
     */
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
}
