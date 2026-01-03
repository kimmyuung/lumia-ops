package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.LoginRequest
import com.lumiaops.lumiaapi.dto.RegisterRequest
import com.lumiaops.lumiacore.config.JwtProperties
import com.lumiaops.lumiacore.domain.AccountStatus
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.UserRepository
import com.lumiaops.lumiacore.security.JwtTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig::class)
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Nested
    @DisplayName("회원가입 API")
    inner class Register {

        @Test
        @DisplayName("회원가입 성공")
        fun register_Success() {
            val request = RegisterRequest(
                email = "test@example.com",
                password = "password123"
            )

            mockMvc.perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.message").exists())
        }

        @Test
        @DisplayName("유효하지 않은 이메일로 회원가입 실패")
        fun register_InvalidEmail_Fails() {
            val request = RegisterRequest(
                email = "invalid-email",
                password = "password123"
            )

            mockMvc.perform(
                post("/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("로그인 API")
    inner class Login {

        @Test
        @DisplayName("로그인 성공 시 JWT 토큰 반환")
        fun login_Success_ReturnsToken() {
            // given - 활성 상태의 사용자 생성
            val user = User(
                email = "test@example.com",
                password = passwordEncoder.encode("password123")
            )
            // 이메일 인증 및 닉네임 설정 시뮬레이션
            user.verifyEmail()
            user.setInitialNickname("TestUser")
            userRepository.save(user)

            val request = LoginRequest(
                email = "test@example.com",
                password = "password123"
            )

            // when & then
            mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("TestUser"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        fun login_WrongPassword_Fails() {
            // given
            val user = User(
                email = "test@example.com",
                password = passwordEncoder.encode("password123")
            )
            user.verifyEmail()
            user.setInitialNickname("TestUser")
            userRepository.save(user)

            val request = LoginRequest(
                email = "test@example.com",
                password = "wrongpassword"
            )

            // when & then
            mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 실패")
        fun login_UserNotFound_Fails() {
            val request = LoginRequest(
                email = "nonexistent@example.com",
                password = "password123"
            )

            mockMvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("보호된 엔드포인트 접근")
    inner class ProtectedEndpoints {

        @Test
        @DisplayName("토큰 없이 보호된 엔드포인트 접근 시 401")
        fun accessProtectedEndpoint_WithoutToken_Returns401() {
            mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("유효한 토큰으로 보호된 엔드포인트 접근 성공")
        fun accessProtectedEndpoint_WithValidToken_Succeeds() {
            // given - 사용자 생성 및 토큰 발급
            val user = User(
                email = "test@example.com",
                password = passwordEncoder.encode("password123")
            )
            user.verifyEmail()
            user.setInitialNickname("TestUser")
            val savedUser = userRepository.save(user)

            val token = jwtTokenProvider.generateAccessToken(savedUser.id!!, savedUser.email)

            // when & then
            mockMvc.perform(
                get("/users/me")
                    .header("Authorization", "Bearer $token")
            )
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("잘못된 토큰으로 보호된 엔드포인트 접근 시 401")
        fun accessProtectedEndpoint_WithInvalidToken_Returns401() {
            mockMvc.perform(
                get("/users/me")
                    .header("Authorization", "Bearer invalid.token.here")
            )
                .andExpect(status().isUnauthorized)
        }
    }
}
