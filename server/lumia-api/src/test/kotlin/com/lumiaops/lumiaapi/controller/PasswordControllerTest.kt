package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.ChangePasswordRequest
import com.lumiaops.lumiaapi.dto.PasswordResetRequest
import com.lumiaops.lumiaapi.dto.ResetPasswordRequest
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.domain.EmailVerification
import com.lumiaops.lumiacore.domain.VerificationType
import com.lumiaops.lumiacore.repository.EmailVerificationRepository
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
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig::class)
@DisplayName("PasswordController 통합 테스트")
class PasswordControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var emailVerificationRepository: EmailVerificationRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var testUser: User
    private lateinit var authToken: String

    @BeforeEach
    fun setUp() {
        emailVerificationRepository.deleteAll()
        userRepository.deleteAll()

        // 테스트 사용자 생성
        testUser = User(
            email = "test@example.com",
            password = passwordEncoder.encode("password123")
        )
        testUser.verifyEmail()
        testUser.setInitialNickname("TestUser")
        testUser = userRepository.save(testUser)

        // JWT 토큰 생성
        authToken = jwtTokenProvider.generateAccessToken(testUser.id!!, testUser.email ?: "")
    }

    @Nested
    @DisplayName("비밀번호 찾기")
    inner class ForgotPassword {

        @Test
        @DisplayName("비밀번호 찾기 요청 성공 (기존 이메일)")
        fun forgotPassword_ExistingEmail_Success() {
            val request = PasswordResetRequest(email = "test@example.com")

            mockMvc.perform(
                post("/auth/password/forgot")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").exists())
        }

        @Test
        @DisplayName("비밀번호 찾기 요청 (존재하지 않는 이메일도 성공 응답)")
        fun forgotPassword_NonExistingEmail_StillReturnsOk() {
            val request = PasswordResetRequest(email = "nonexistent@example.com")

            mockMvc.perform(
                post("/auth/password/forgot")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정")
    inner class ResetPassword {

        @Test
        @DisplayName("잘못된 토큰으로 재설정 시 실패")
        fun resetPassword_InvalidToken_Fails() {
            val request = ResetPasswordRequest(
                token = "invalid-token",
                newPassword = "newPassword123"
            )

            mockMvc.perform(
                post("/auth/password/reset")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    inner class ChangePassword {

        @Test
        @DisplayName("올바른 현재 비밀번호로 변경 성공")
        fun changePassword_CorrectOldPassword_Success() {
            val request = ChangePasswordRequest(
                oldPassword = "password123",
                newPassword = "newPassword456"
            )

            mockMvc.perform(
                put("/auth/password/change")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        @DisplayName("잘못된 현재 비밀번호로 변경 실패")
        fun changePassword_WrongOldPassword_Fails() {
            val request = ChangePasswordRequest(
                oldPassword = "wrongPassword",
                newPassword = "newPassword456"
            )

            mockMvc.perform(
                put("/auth/password/change")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("인증 없이 변경 시 401")
        fun changePassword_Unauthenticated_Returns401() {
            val request = ChangePasswordRequest(
                oldPassword = "password123",
                newPassword = "newPassword456"
            )

            mockMvc.perform(
                put("/auth/password/change")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
        }
    }
}
