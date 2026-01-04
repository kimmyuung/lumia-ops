package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.CreateScrimRequest
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.ScrimRepository
import com.lumiaops.lumiacore.repository.UserRepository
import com.lumiaops.lumiacore.security.JwtTokenProvider
import com.lumiaops.lumiacore.service.ScrimService
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
@DisplayName("ScrimController 통합 테스트")
class ScrimControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var scrimRepository: ScrimRepository

    @Autowired
    private lateinit var scrimService: ScrimService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var testUser: User
    private lateinit var authToken: String

    @BeforeEach
    fun setUp() {
        scrimRepository.deleteAll()
        userRepository.deleteAll()

        testUser = User(
            email = "test@example.com",
            password = passwordEncoder.encode("password123")
        )
        testUser.verifyEmail()
        testUser.setInitialNickname("TestUser")
        testUser = userRepository.save(testUser)

        authToken = jwtTokenProvider.generateAccessToken(testUser.id!!, testUser.email)
    }

    @Nested
    @DisplayName("스크림 조회 API")
    inner class GetScrims {

        @Test
        @DisplayName("모든 스크림 조회 성공")
        fun getScrims_Success() {
            mockMvc.perform(
                get("/api/scrims")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
        }

        @Test
        @DisplayName("인증 없이 스크림 조회 시 401")
        fun getScrims_WithoutAuth_Returns401() {
            mockMvc.perform(get("/api/scrims"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("스크림 생성 API")
    inner class CreateScrim {

        @Test
        @DisplayName("스크림 생성 성공")
        fun createScrim_Success() {
            val request = CreateScrimRequest(
                title = "Test Scrim",
                startTime = LocalDateTime.now().plusDays(1)
            )

            mockMvc.perform(
                post("/api/scrims")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("Test Scrim"))
        }
    }

    @Nested
    @DisplayName("스크림 상세 조회 API")
    inner class GetScrimById {

        @Test
        @DisplayName("스크림 상세 조회 성공")
        fun getScrim_Success() {
            // given
            val scrim = scrimService.createScrim("Test Scrim", LocalDateTime.now().plusDays(1))

            // when & then
            mockMvc.perform(
                get("/api/scrims/${scrim.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("Test Scrim"))
        }

        @Test
        @DisplayName("존재하지 않는 스크림 조회 시 404")
        fun getScrim_NotFound_Returns404() {
            mockMvc.perform(
                get("/api/scrims/99999")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("스크림 삭제 API")
    inner class DeleteScrim {

        @Test
        @DisplayName("스크림 삭제 성공")
        fun deleteScrim_Success() {
            // given
            val scrim = scrimService.createScrim("Scrim to Delete", LocalDateTime.now().plusDays(1))

            // when & then
            mockMvc.perform(
                delete("/api/scrims/${scrim.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)

            // verify deletion
            mockMvc.perform(
                get("/api/scrims/${scrim.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }
    }
}
