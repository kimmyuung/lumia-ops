package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.UpdateNicknameRequest
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
@DisplayName("UserController 통합 테스트")
class UserControllerTest {

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

    private lateinit var testUser: User
    private lateinit var authToken: String

    @BeforeEach
    fun setUp() {
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
        authToken = jwtTokenProvider.generateAccessToken(testUser.id!!, testUser.email)
    }

    @Nested
    @DisplayName("내 정보 조회")
    inner class GetMyInfo {

        @Test
        @DisplayName("인증된 사용자가 내 정보를 조회할 수 있다")
        fun getMyInfo_Authenticated_Success() {
            mockMvc.perform(
                get("/users/me")
                    .header("Authorization", "Bearer $authToken")
                    .header("X-User-Id", testUser.id!!)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("TestUser"))
        }

        @Test
        @DisplayName("인증 없이 조회 시 401")
        fun getMyInfo_Unauthenticated_Returns401() {
            mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("닉네임 설정")
    inner class SetNickname {

        @Test
        @DisplayName("닉네임 설정 성공")
        fun setNickname_Success() {
            // given - 닉네임이 없는 사용자
            val newUser = User(
                email = "newuser@example.com",
                password = passwordEncoder.encode("password123")
            )
            newUser.verifyEmail()
            val savedUser = userRepository.save(newUser)
            val token = jwtTokenProvider.generateAccessToken(savedUser.id!!, savedUser.email)

            val request = UpdateNicknameRequest(nickname = "NewNickname")

            // when & then
            mockMvc.perform(
                post("/users/me/nickname")
                    .header("Authorization", "Bearer $token")
                    .header("X-User-Id", savedUser.id!!)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.nickname").value("NewNickname"))
        }
    }

    @Nested
    @DisplayName("닉네임 변경")
    inner class UpdateNickname {

        @Test
        @DisplayName("닉네임 변경 시도 (30일 제한)")
        fun updateNickname_WithinLimit_Fails() {
            val request = UpdateNicknameRequest(nickname = "NewNickname")

            // 이미 닉네임이 설정된 사용자가 변경 시도
            // 30일 제한에 걸릴 수 있음
            mockMvc.perform(
                put("/users/me/nickname")
                    .header("Authorization", "Bearer $authToken")
                    .header("X-User-Id", testUser.id!!)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("닉네임 변경 남은 일수")
    inner class GetRemainingDays {

        @Test
        @DisplayName("남은 일수 조회 성공")
        fun getRemainingDays_Success() {
            mockMvc.perform(
                get("/users/me/nickname/remaining-days")
                    .header("Authorization", "Bearer $authToken")
                    .header("X-User-Id", testUser.id!!)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.daysRemaining").exists())
        }
    }
}
