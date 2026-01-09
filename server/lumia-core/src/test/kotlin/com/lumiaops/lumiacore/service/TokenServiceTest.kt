package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.config.JwtProperties
import com.lumiaops.lumiacore.domain.RefreshToken
import com.lumiaops.lumiacore.domain.TokenBlacklist
import com.lumiaops.lumiacore.repository.RefreshTokenRepository
import com.lumiaops.lumiacore.repository.TokenBlacklistRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("TokenService 테스트")
class TokenServiceTest {

    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var tokenBlacklistRepository: TokenBlacklistRepository
    private lateinit var jwtProperties: JwtProperties
    private lateinit var tokenService: TokenService

    @BeforeEach
    fun setUp() {
        refreshTokenRepository = mockk()
        tokenBlacklistRepository = mockk()
        jwtProperties = mockk()
        
        every { jwtProperties.refreshExpirationMs } returns 604800000L // 7 days
        every { jwtProperties.maxSessions } returns 5
        
        tokenService = TokenService(refreshTokenRepository, tokenBlacklistRepository, jwtProperties)
    }

    @Nested
    @DisplayName("Refresh Token 저장")
    inner class SaveRefreshToken {

        @Test
        @DisplayName("Refresh Token 저장 성공")
        fun `should save refresh token successfully`() {
            // given
            val userId = 1L
            val token = "test-refresh-token"
            
            every { refreshTokenRepository.save(any()) } answers { firstArg() }

            // when
            val result = tokenService.saveRefreshToken(userId, token)

            // then
            assertNotNull(result)
            assertEquals(userId, result.userId)
            assertEquals(token, result.token)
            assertTrue(result.expiresAt.isAfter(LocalDateTime.now()))
            
            verify { refreshTokenRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("Refresh Token 검증")
    inner class ValidateRefreshToken {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        fun `should validate valid refresh token`() {
            // given
            val token = "valid-refresh-token"
            val refreshToken = RefreshToken(
                userId = 1L,
                token = token,
                expiresAt = LocalDateTime.now().plusDays(7)
            )
            
            every { refreshTokenRepository.findByToken(token) } returns refreshToken

            // when
            val result = tokenService.validateRefreshToken(token)

            // then
            assertNotNull(result)
            assertEquals(token, result.token)
        }

        @Test
        @DisplayName("존재하지 않는 토큰 검증 실패")
        fun `should return null for non-existent token`() {
            // given
            val token = "non-existent-token"
            
            every { refreshTokenRepository.findByToken(token) } returns null

            // when
            val result = tokenService.validateRefreshToken(token)

            // then
            assertNull(result)
        }

        @Test
        @DisplayName("만료된 토큰 검증 실패")
        fun `should return null for expired token`() {
            // given
            val token = "expired-token"
            val refreshToken = RefreshToken(
                userId = 1L,
                token = token,
                expiresAt = LocalDateTime.now().minusDays(1)
            )
            
            every { refreshTokenRepository.findByToken(token) } returns refreshToken

            // when
            val result = tokenService.validateRefreshToken(token)

            // then
            assertNull(result)
        }
    }

    @Nested
    @DisplayName("Refresh Token 폐기")
    inner class RevokeRefreshToken {

        @Test
        @DisplayName("단일 토큰 폐기 성공")
        fun `should revoke single refresh token`() {
            // given
            val token = "token-to-revoke"
            val refreshToken = RefreshToken(
                userId = 1L,
                token = token,
                expiresAt = LocalDateTime.now().plusDays(7)
            )
            
            every { refreshTokenRepository.findByToken(token) } returns refreshToken

            // when
            tokenService.revokeRefreshToken(token)

            // then
            assertTrue(refreshToken.revoked)
            verify { refreshTokenRepository.findByToken(token) }
        }

        @Test
        @DisplayName("사용자의 모든 토큰 폐기")
        fun `should revoke all user tokens`() {
            // given
            val userId = 1L
            
            every { refreshTokenRepository.revokeAllByUserId(userId) } returns 3

            // when
            val count = tokenService.revokeAllUserTokens(userId)

            // then
            assertEquals(3, count)
            verify { refreshTokenRepository.revokeAllByUserId(userId) }
        }
    }

    @Nested
    @DisplayName("세션 제한")
    inner class EnforceSessionLimit {

        @Test
        @DisplayName("세션 제한 초과 시 오래된 세션 폐기")
        fun `should revoke oldest sessions when limit exceeded`() {
            // given
            val userId = 1L
            val now = LocalDateTime.now()
            
            every { refreshTokenRepository.countByUserIdAndRevokedFalseAndExpiresAtAfter(userId, any()) } returns 5L
            
            val oldestSessions = listOf(
                RefreshToken(userId = userId, token = "token1", expiresAt = now.plusDays(7)),
                RefreshToken(userId = userId, token = "token2", expiresAt = now.plusDays(7))
            )
            every { refreshTokenRepository.findOldestActiveByUserId(userId, any()) } returns oldestSessions

            // when
            tokenService.enforceSessionLimit(userId)

            // then - 가장 오래된 세션이 폐기되어야 함
            assertTrue(oldestSessions[0].revoked)
        }
    }

    @Nested
    @DisplayName("Access Token 블랙리스트")
    inner class AccessTokenBlacklist {

        @Test
        @DisplayName("블랙리스트에 토큰 추가")
        fun `should add token to blacklist`() {
            // given
            val token = "token-to-blacklist"
            val expiresAt = LocalDateTime.now().plusHours(1)
            
            every { tokenBlacklistRepository.existsByTokenHash(any()) } returns false
            every { tokenBlacklistRepository.save(any()) } answers { firstArg() }

            // when
            tokenService.blacklistAccessToken(token, expiresAt)

            // then
            verify { tokenBlacklistRepository.save(any()) }
        }

        @Test
        @DisplayName("이미 블랙리스트에 있으면 무시")
        fun `should ignore if already blacklisted`() {
            // given
            val token = "already-blacklisted"
            val expiresAt = LocalDateTime.now().plusHours(1)
            
            every { tokenBlacklistRepository.existsByTokenHash(any()) } returns true

            // when
            tokenService.blacklistAccessToken(token, expiresAt)

            // then
            verify(exactly = 0) { tokenBlacklistRepository.save(any()) }
        }

        @Test
        @DisplayName("블랙리스트 확인")
        fun `should check if token is blacklisted`() {
            // given
            val token = "some-token"
            
            every { tokenBlacklistRepository.existsByTokenHash(any()) } returns true

            // when
            val result = tokenService.isBlacklisted(token)

            // then
            assertTrue(result)
        }
    }
}
