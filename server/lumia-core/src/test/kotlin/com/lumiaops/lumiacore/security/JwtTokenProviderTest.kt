package com.lumiaops.lumiacore.security

import com.lumiaops.lumiacore.config.JwtProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Base64

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var jwtProperties: JwtProperties

    @BeforeEach
    fun setUp() {
        // 테스트용 256비트 이상의 비밀키 생성
        val secretKey = "lumia-ops-jwt-secret-key-for-test-256-bits-or-more"
        val encodedSecret = Base64.getEncoder().encodeToString(secretKey.toByteArray())
        
        jwtProperties = JwtProperties(
            secret = encodedSecret,
            expirationMs = 3600000, // 1시간
            refreshExpirationMs = 604800000 // 7일
        )
        jwtTokenProvider = JwtTokenProvider(jwtProperties)
    }

    @Nested
    @DisplayName("토큰 생성")
    inner class TokenGeneration {

        @Test
        @DisplayName("액세스 토큰 생성 성공")
        fun generateAccessToken_Success() {
            // given
            val userId = 1L
            val email = "test@example.com"

            // when
            val token = jwtTokenProvider.generateAccessToken(userId, email)

            // then
            assertNotNull(token)
            assertTrue(token.isNotBlank())
            assertTrue(token.split(".").size == 3) // JWT 구조: header.payload.signature
        }

        @Test
        @DisplayName("리프레시 토큰 생성 성공")
        fun generateRefreshToken_Success() {
            // given
            val userId = 1L

            // when
            val token = jwtTokenProvider.generateRefreshToken(userId)

            // then
            assertNotNull(token)
            assertTrue(token.isNotBlank())
            assertTrue(token.split(".").size == 3)
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    inner class TokenValidation {

        @Test
        @DisplayName("유효한 토큰 검증 성공")
        fun validateToken_ValidToken_ReturnsTrue() {
            // given
            val token = jwtTokenProvider.generateAccessToken(1L, "test@example.com")

            // when
            val isValid = jwtTokenProvider.validateToken(token)

            // then
            assertTrue(isValid)
        }

        @Test
        @DisplayName("잘못된 형식의 토큰 검증 실패")
        fun validateToken_MalformedToken_ReturnsFalse() {
            // given
            val invalidToken = "invalid.token.format"

            // when
            val isValid = jwtTokenProvider.validateToken(invalidToken)

            // then
            assertFalse(isValid)
        }

        @Test
        @DisplayName("빈 토큰 검증 실패")
        fun validateToken_EmptyToken_ReturnsFalse() {
            // when
            val isValid = jwtTokenProvider.validateToken("")

            // then
            assertFalse(isValid)
        }

        @Test
        @DisplayName("만료된 토큰 검증 실패")
        fun validateToken_ExpiredToken_ReturnsFalse() {
            // given - 만료 시간이 0인 provider 생성
            val expiredProperties = JwtProperties(
                secret = jwtProperties.secret,
                expirationMs = 0, // 즉시 만료
                refreshExpirationMs = 0
            )
            val expiredTokenProvider = JwtTokenProvider(expiredProperties)
            val token = expiredTokenProvider.generateAccessToken(1L, "test@example.com")

            // when - 약간 대기 후 검증
            Thread.sleep(100)
            val isValid = expiredTokenProvider.validateToken(token)

            // then
            assertFalse(isValid)
        }
    }

    @Nested
    @DisplayName("토큰 파싱")
    inner class TokenParsing {

        @Test
        @DisplayName("토큰에서 userId 추출 성공")
        fun getUserIdFromToken_Success() {
            // given
            val userId = 123L
            val token = jwtTokenProvider.generateAccessToken(userId, "test@example.com")

            // when
            val extractedUserId = jwtTokenProvider.getUserIdFromToken(token)

            // then
            assertEquals(userId, extractedUserId)
        }

        @Test
        @DisplayName("토큰에서 email 추출 성공")
        fun getEmailFromToken_Success() {
            // given
            val email = "test@example.com"
            val token = jwtTokenProvider.generateAccessToken(1L, email)

            // when
            val extractedEmail = jwtTokenProvider.getEmailFromToken(token)

            // then
            assertEquals(email, extractedEmail)
        }
    }

    @Nested
    @DisplayName("토큰 타입 확인")
    inner class TokenType {

        @Test
        @DisplayName("액세스 토큰 타입 확인")
        fun isAccessToken_AccessToken_ReturnsTrue() {
            // given
            val token = jwtTokenProvider.generateAccessToken(1L, "test@example.com")

            // when
            val isAccess = jwtTokenProvider.isAccessToken(token)
            val isRefresh = jwtTokenProvider.isRefreshToken(token)

            // then
            assertTrue(isAccess)
            assertFalse(isRefresh)
        }

        @Test
        @DisplayName("리프레시 토큰 타입 확인")
        fun isRefreshToken_RefreshToken_ReturnsTrue() {
            // given
            val token = jwtTokenProvider.generateRefreshToken(1L)

            // when
            val isAccess = jwtTokenProvider.isAccessToken(token)
            val isRefresh = jwtTokenProvider.isRefreshToken(token)

            // then
            assertFalse(isAccess)
            assertTrue(isRefresh)
        }
    }
}
