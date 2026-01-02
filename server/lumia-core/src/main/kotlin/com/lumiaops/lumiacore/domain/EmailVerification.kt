package com.lumiaops.lumiacore.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * 이메일 인증 토큰 엔티티
 * 회원가입, 비밀번호 재설정, 휴면 계정 재활성화, 계정 잠금 해제에 사용
 */
@Entity
@Table(
    name = "email_verifications",
    indexes = [
        Index(name = "idx_email_verification_token", columnList = "token", unique = true),
        Index(name = "idx_email_verification_email", columnList = "email")
    ]
)
class EmailVerification(
    @Column(nullable = false)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: VerificationType,

    @Column(nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now().plusMinutes(15)
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true)
    val token: String = UUID.randomUUID().toString()

    @Column(nullable = false)
    var verified: Boolean = false
        protected set

    val createdAt: LocalDateTime = LocalDateTime.now()

    var verifiedAt: LocalDateTime? = null
        protected set

    /**
     * 토큰이 만료되었는지 확인
     */
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    /**
     * 인증 가능 여부 확인
     */
    fun canVerify(): Boolean = !verified && !isExpired()

    /**
     * 인증 완료 처리
     */
    fun verify() {
        require(canVerify()) { "인증할 수 없습니다. 만료: ${isExpired()}, 이미 인증됨: $verified" }
        verified = true
        verifiedAt = LocalDateTime.now()
    }
}

/**
 * 이메일 인증 유형
 */
enum class VerificationType {
    SIGNUP,              // 회원가입
    PASSWORD_RESET,      // 비밀번호 재설정
    DORMANT_REACTIVATION,// 휴면 계정 재활성화
    UNLOCK_ACCOUNT       // 계정 잠금 해제
}
