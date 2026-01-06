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
     * 토큰 값으로 조회
     */
    fun findByToken(token: String): RefreshToken?
    
    /**
     * 사용자 ID로 모든 토큰 조회
     */
    fun findByUserId(userId: Long): List<RefreshToken>
    
    /**
     * 사용자 ID로 유효한 토큰만 조회
     */
    fun findByUserIdAndRevokedFalse(userId: Long): List<RefreshToken>
    
    /**
     * 토큰 삭제
     */
    fun deleteByToken(token: String)
    
    /**
     * 사용자의 모든 토큰 삭제
     */
    fun deleteByUserId(userId: Long)
    
    /**
     * 만료된 토큰 삭제 (정리용)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    fun deleteExpiredTokens(now: LocalDateTime): Int
    
    /**
     * 사용자의 모든 토큰 폐기 (revoked = true)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId")
    fun revokeAllByUserId(userId: Long): Int

    /**
     * 사용자의 유효한 세션 수 카운트
     */
    fun countByUserIdAndRevokedFalseAndExpiresAtAfter(userId: Long, now: LocalDateTime): Long

    /**
     * 사용자의 가장 오래된 활성 세션 조회 (세션 제한 시 폐기 대상)
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.revoked = false AND rt.expiresAt > :now ORDER BY rt.createdAt ASC")
    fun findOldestActiveByUserId(userId: Long, now: LocalDateTime): List<RefreshToken>
}
