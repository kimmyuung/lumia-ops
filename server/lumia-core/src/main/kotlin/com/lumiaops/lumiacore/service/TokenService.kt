package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.config.JwtProperties
import com.lumiaops.lumiacore.domain.RefreshToken
import com.lumiaops.lumiacore.domain.TokenBlacklist
import com.lumiaops.lumiacore.repository.RefreshTokenRepository
import com.lumiaops.lumiacore.repository.TokenBlacklistRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.LocalDateTime

/**
 * 토큰 관리 서비스
 * Refresh Token 서버 저장 및 Access Token 블랙리스트 관리
 */
@Service
@Transactional(readOnly = true)
class TokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenBlacklistRepository: TokenBlacklistRepository,
    private val jwtProperties: JwtProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // ==================== Refresh Token 관리 ====================

    /**
     * Refresh Token 저장
     * @return 저장된 RefreshToken 엔티티
     */
    @Transactional
    fun saveRefreshToken(userId: Long, token: String): RefreshToken {
        val expiresAt = LocalDateTime.now().plusSeconds(jwtProperties.refreshExpirationMs / 1000)
        val refreshToken = RefreshToken(
            userId = userId,
            token = token,
            expiresAt = expiresAt
        )
        log.debug("Refresh Token 저장: userId=$userId")
        return refreshTokenRepository.save(refreshToken)
    }

    /**
     * Refresh Token 검증
     * @return 유효한 토큰이면 RefreshToken, 아니면 null
     */
    fun validateRefreshToken(token: String): RefreshToken? {
        val refreshToken = refreshTokenRepository.findByToken(token) ?: return null
        
        if (!refreshToken.isValid()) {
            log.warn("Refresh Token 검증 실패 - 폐기됨 또는 만료됨: userId=${refreshToken.userId}")
            return null
        }
        
        return refreshToken
    }

    /**
     * Refresh Token 폐기 (단일)
     */
    @Transactional
    fun revokeRefreshToken(token: String) {
        val refreshToken = refreshTokenRepository.findByToken(token)
        if (refreshToken != null) {
            refreshToken.revoke()
            log.info("Refresh Token 폐기: userId=${refreshToken.userId}")
        }
    }

    /**
     * 사용자의 모든 Refresh Token 폐기
     * 보안 이벤트(비밀번호 변경, 계정 잠금 등) 시 사용
     */
    @Transactional
    fun revokeAllUserTokens(userId: Long): Int {
        val count = refreshTokenRepository.revokeAllByUserId(userId)
        log.info("사용자의 모든 Refresh Token 폐기: userId=$userId, count=$count")
        return count
    }

    // ==================== Access Token 블랙리스트 관리 ====================

    /**
     * Access Token 블랙리스트에 추가
     */
    @Transactional
    fun blacklistAccessToken(token: String, expiresAt: LocalDateTime) {
        val tokenHash = hashToken(token)
        
        // 이미 블랙리스트에 있으면 무시
        if (tokenBlacklistRepository.existsByTokenHash(tokenHash)) {
            return
        }
        
        val blacklisted = TokenBlacklist(
            tokenHash = tokenHash,
            expiresAt = expiresAt
        )
        tokenBlacklistRepository.save(blacklisted)
        log.debug("Access Token 블랙리스트 추가")
    }

    /**
     * Access Token이 블랙리스트에 있는지 확인
     */
    fun isBlacklisted(token: String): Boolean {
        val tokenHash = hashToken(token)
        return tokenBlacklistRepository.existsByTokenHash(tokenHash)
    }

    // ==================== 토큰 정리 ====================

    /**
     * 만료된 토큰 정리 (매시간 실행)
     */
    @Scheduled(cron = "0 0 * * * *") // 매시간 정각
    @Transactional
    fun cleanupExpiredTokens() {
        val now = LocalDateTime.now()
        
        val deletedRefreshTokens = refreshTokenRepository.deleteExpiredTokens(now)
        val deletedBlacklist = tokenBlacklistRepository.deleteExpiredTokens(now)
        
        if (deletedRefreshTokens > 0 || deletedBlacklist > 0) {
            log.info("만료된 토큰 정리: refreshTokens=$deletedRefreshTokens, blacklist=$deletedBlacklist")
        }
    }

    // ==================== 유틸리티 ====================

    /**
     * 토큰을 SHA-256 해시로 변환
     */
    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
