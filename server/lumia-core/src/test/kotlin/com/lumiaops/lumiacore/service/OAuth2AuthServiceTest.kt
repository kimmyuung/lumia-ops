package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.AccountStatus
import com.lumiaops.lumiacore.domain.AuthProvider
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DisplayName("OAuth2AuthService 테스트")
class OAuth2AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var oAuth2AuthService: OAuth2AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        oAuth2AuthService = OAuth2AuthService(userRepository)
    }

    @Nested
    @DisplayName("Steam 로그인")
    inner class SteamLogin {

        @Test
        @DisplayName("신규 Steam 사용자 - 새 계정 생성")
        fun `should create new user for new Steam login`() {
            // given
            val steamId = "76561198123456789"
            val steamNickname = "TestPlayer"

            every { userRepository.findBySteamId(steamId) } returns null
            every { userRepository.save(any()) } answers {
                firstArg<User>().apply {
                    // ID 시뮬레이션
                }
            }

            // when
            val result = oAuth2AuthService.processingSteamLogin(steamId, steamNickname)

            // then
            assertNotNull(result)
            assertEquals(steamNickname, result.nickname)
            assertEquals(AuthProvider.STEAM, result.authProvider)
            assertEquals(AccountStatus.PENDING_NICKNAME, result.status)
            
            verify { userRepository.findBySteamId(steamId) }
            verify { userRepository.save(any()) }
        }

        @Test
        @DisplayName("기존 Steam 사용자 - 로그인 처리")
        fun `should login existing Steam user`() {
            // given
            val steamId = "76561198123456789"
            val steamNickname = "TestPlayer"
            
            val existingUser = User.createSteamUser(steamId, "ExistingPlayer")

            every { userRepository.findBySteamId(steamId) } returns existingUser

            // when
            val result = oAuth2AuthService.processingSteamLogin(steamId, steamNickname)

            // then
            assertNotNull(result)
            assertEquals("ExistingPlayer", result.nickname) // 기존 닉네임 유지
            
            verify { userRepository.findBySteamId(steamId) }
            verify(exactly = 0) { userRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("Kakao 로그인")
    inner class KakaoLogin {

        @Test
        @DisplayName("신규 Kakao 사용자 - 새 계정 생성")
        fun `should create new user for new Kakao login`() {
            // given
            val kakaoId = 1234567890L
            val kakaoNickname = "KakaoPlayer"
            val kakaoEmail = "test@kakao.com"

            every { userRepository.findByKakaoId(kakaoId) } returns null
            every { userRepository.save(any()) } answers { firstArg() }

            // when
            val result = oAuth2AuthService.processingKakaoLogin(kakaoId, kakaoNickname, kakaoEmail)

            // then
            assertNotNull(result)
            assertEquals(kakaoNickname, result.nickname)
            assertEquals(kakaoEmail, result.email)
            assertEquals(AuthProvider.KAKAO, result.authProvider)
            
            verify { userRepository.findByKakaoId(kakaoId) }
            verify { userRepository.save(any()) }
        }

        @Test
        @DisplayName("기존 Kakao 사용자 - 이메일 없으면 업데이트")
        fun `should update email for existing Kakao user if missing`() {
            // given
            val kakaoId = 1234567890L
            val kakaoNickname = "KakaoPlayer"
            val newEmail = "new@kakao.com"
            
            val existingUser = User.createKakaoUser(kakaoId, "ExistingKakao", null)

            every { userRepository.findByKakaoId(kakaoId) } returns existingUser

            // when
            val result = oAuth2AuthService.processingKakaoLogin(kakaoId, kakaoNickname, newEmail)

            // then
            assertNotNull(result)
            assertEquals(newEmail, result.email)
            
            verify { userRepository.findByKakaoId(kakaoId) }
        }
    }

    @Nested
    @DisplayName("게임 닉네임 설정")
    inner class SetupGameNickname {

        @Test
        @DisplayName("OAuth 사용자 게임 닉네임 설정 성공")
        fun `should complete OAuth setup with game nickname`() {
            // given
            val userId = 1L
            val gameNickname = "EternalReturn_Nick"
            
            val user = User.createSteamUser("76561198123456789", "SteamPlayer")
            
            every { userRepository.findById(userId) } returns Optional.of(user)

            // when
            val result = oAuth2AuthService.completeOAuthSetup(userId, gameNickname)

            // then
            assertNotNull(result)
            assertEquals(gameNickname, result.gameNickname)
            assertEquals(AccountStatus.ACTIVE, result.status)
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - 예외 발생")
        fun `should throw exception for non-existent user`() {
            // given
            val userId = 999L
            val gameNickname = "EternalReturn_Nick"

            every { userRepository.findById(userId) } returns Optional.empty()

            // when & then
            try {
                oAuth2AuthService.completeOAuthSetup(userId, gameNickname)
                assert(false) { "예외가 발생해야 합니다" }
            } catch (e: IllegalArgumentException) {
                assertEquals("사용자를 찾을 수 없습니다: $userId", e.message)
            }
        }
    }

    @Nested
    @DisplayName("OAuth 사용자 확인")
    inner class IsOAuthUser {

        @Test
        @DisplayName("Steam 사용자는 OAuth 사용자")
        fun `should return true for Steam user`() {
            // given
            val userId = 1L
            val user = User.createSteamUser("76561198123456789", "SteamPlayer")

            every { userRepository.findById(userId) } returns Optional.of(user)

            // when
            val result = oAuth2AuthService.isOAuthUser(userId)

            // then
            assertEquals(true, result)
        }

        @Test
        @DisplayName("이메일 사용자는 OAuth 사용자가 아님")
        fun `should return false for email user`() {
            // given
            val userId = 1L
            val user = User(
                email = "test@test.com",
                password = "password123",
                nickname = "EmailUser"
            )

            every { userRepository.findById(userId) } returns Optional.of(user)

            // when
            val result = oAuth2AuthService.isOAuthUser(userId)

            // then
            assertEquals(false, result)
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - false 반환")
        fun `should return false for non-existent user`() {
            // given
            val userId = 999L

            every { userRepository.findById(userId) } returns Optional.empty()

            // when
            val result = oAuth2AuthService.isOAuthUser(userId)

            // then
            assertEquals(false, result)
        }
    }
}
