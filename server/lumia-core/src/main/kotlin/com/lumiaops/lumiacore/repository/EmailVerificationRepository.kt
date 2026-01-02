package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.EmailVerification
import com.lumiaops.lumiacore.domain.VerificationType
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface EmailVerificationRepository : JpaRepository<EmailVerification, Long> {

    /**
     * 토큰으로 인증 조회
     */
    fun findByToken(token: String): EmailVerification?

    /**
     * 이메일과 타입으로 유효한 인증 조회
     */
    fun findByEmailAndTypeAndVerifiedFalseAndExpiresAtAfter(
        email: String,
        type: VerificationType,
        now: LocalDateTime = LocalDateTime.now()
    ): EmailVerification?

    /**
     * 이메일의 모든 미인증 토큰 삭제 (새 토큰 발급 시)
     */
    fun deleteByEmailAndTypeAndVerifiedFalse(email: String, type: VerificationType)
}
