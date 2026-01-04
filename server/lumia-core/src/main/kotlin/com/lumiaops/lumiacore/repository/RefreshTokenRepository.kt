package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    
    /**
     * 토큰 문자열로 Refresh Token 조회
     */
    fun findByToken(token: String): RefreshToken?
    
    /**
     * 사용자 ID로 유효한 Refresh Token 조회
     */
    fun findByUserIdAndRevokedFalseAndExpiresAtAfter(userId: Long, now: LocalDateTime): List<RefreshToken>
    
    /**
     * 사용자의 모든 Refresh Token 폐기
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId")
    fun revokeAllByUserId(userId: Long): Int
    
    /**
     * 만료된 토큰 삭제
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    fun deleteExpiredTokens(now: LocalDateTime): Int
}
