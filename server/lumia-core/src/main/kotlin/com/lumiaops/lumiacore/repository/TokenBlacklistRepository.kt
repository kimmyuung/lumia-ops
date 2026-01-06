package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.TokenBlacklist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TokenBlacklistRepository : JpaRepository<TokenBlacklist, Long> {
    
    /**
     * 토큰 해시가 블랙리스트에 있는지 확인
     */
    fun existsByTokenHash(tokenHash: String): Boolean
    
    /**
     * 토큰 해시로 조회
     */
    fun findByTokenHash(tokenHash: String): TokenBlacklist?
    
    /**
     * 만료된 블랙리스트 항목 삭제 (정리용)
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist tb WHERE tb.expiresAt < :now")
    fun deleteExpiredTokens(now: LocalDateTime): Int
}
