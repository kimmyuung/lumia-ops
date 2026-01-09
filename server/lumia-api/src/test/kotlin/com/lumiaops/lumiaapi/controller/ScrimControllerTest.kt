package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.CreateScrimRequest
import com.lumiaops.lumiaapi.dto.UpdateScrimRequest
import com.lumiaops.lumiaapi.dto.UpdateScrimStatusRequest
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.domain.scrim.ScrimStatus
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
    private val futureTime: LocalDateTime = LocalDateTime.now().plusDays(1)

    @BeforeEach
    fun setUp() {
        scrimRepository.deleteAll()
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
    @DisplayName("스크림 목록 조회")
    inner class GetScrims {

        @Test
        @DisplayName("전체 스크림 목록 조회")
        fun getScrims_All_Success() {
            // given
            scrimService.createScrim("Scrim 1", futureTime)
            scrimService.createScrim("Scrim 2", futureTime.plusHours(2))

            // when & then
            mockMvc.perform(
                get("/api/scrims")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$.length()").value(2))
        }

        @Test
        @DisplayName("상태별 스크림 필터링")
        fun getScrims_ByStatus_Success() {
            // given
            val scrim = scrimService.createScrim("Active Scrim", futureTime)
            scrimService.startScrim(scrim.id!!)

            // when & then
            mockMvc.perform(
                get("/api/scrims")
                    .param("status", "IN_PROGRESS")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"))
        }

        @Test
        @DisplayName("인증 없이 조회 시 401")
        fun getScrims_Unauthenticated_Returns401() {
            mockMvc.perform(get("/api/scrims"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("스크림 생성")
    inner class CreateScrim {

        @Test
        @DisplayName("스크림 생성 성공")
        fun createScrim_Success() {
            val request = CreateScrimRequest(
                title = "New Scrim",
                startTime = futureTime
            )

            mockMvc.perform(
                post("/api/scrims")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.title").value("New Scrim"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
        }

        @Test
        @DisplayName("제목 없이 생성 시 실패")
        fun createScrim_NoTitle_Fails() {
            val request = mapOf(
                "title" to "",
                "startTime" to futureTime.toString()
            )

            mockMvc.perform(
                post("/api/scrims")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("스크림 상세 조회")
    inner class GetScrim {

        @Test
        @DisplayName("스크림 상세 조회 성공")
        fun getScrim_Success() {
            // given
            val scrim = scrimService.createScrim("Test Scrim", futureTime)

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
    @DisplayName("스크림 수정")
    inner class UpdateScrim {

        @Test
        @DisplayName("스크림 수정 성공")
        fun updateScrim_Success() {
            // given
            val scrim = scrimService.createScrim("Old Title", futureTime)

            val request = UpdateScrimRequest(
                title = "New Title",
                startTime = null,
                opponentTeamName = null,
                mapName = null,
                notes = null,
                status = null
            )

            // when & then
            mockMvc.perform(
                patch("/api/scrims/${scrim.id}")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("New Title"))
        }
    }

    @Nested
    @DisplayName("스크림 삭제")
    inner class DeleteScrim {

        @Test
        @DisplayName("스크림 삭제 성공")
        fun deleteScrim_Success() {
            // given
            val scrim = scrimService.createScrim("To Delete", futureTime)

            // when & then
            mockMvc.perform(
                delete("/api/scrims/${scrim.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNoContent)

            // verify deletion
            mockMvc.perform(
                get("/api/scrims/${scrim.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("스크림 상태 변경")
    inner class UpdateStatus {

        @Test
        @DisplayName("스크림 시작 (SCHEDULED -> IN_PROGRESS)")
        fun startScrim_Success() {
            // given
            val scrim = scrimService.createScrim("Test Scrim", futureTime)

            val request = UpdateScrimStatusRequest(status = ScrimStatus.IN_PROGRESS)

            // when & then
            mockMvc.perform(
                patch("/api/scrims/${scrim.id}/status")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
        }

        @Test
        @DisplayName("스크림 종료 (IN_PROGRESS -> FINISHED)")
        fun finishScrim_Success() {
            // given
            val scrim = scrimService.createScrim("Test Scrim", futureTime)
            scrimService.startScrim(scrim.id!!)

            val request = UpdateScrimStatusRequest(status = ScrimStatus.FINISHED)

            // when & then
            mockMvc.perform(
                patch("/api/scrims/${scrim.id}/status")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("FINISHED"))
        }

        @Test
        @DisplayName("스크림 취소")
        fun cancelScrim_Success() {
            // given
            val scrim = scrimService.createScrim("Test Scrim", futureTime)

            val request = UpdateScrimStatusRequest(status = ScrimStatus.CANCELLED)

            // when & then
            mockMvc.perform(
                patch("/api/scrims/${scrim.id}/status")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.status").value("CANCELLED"))
        }

        @Test
        @DisplayName("SCHEDULED 상태로 되돌리기 불가")
        fun revertToScheduled_Fails() {
            // given
            val scrim = scrimService.createScrim("Test Scrim", futureTime)
            scrimService.startScrim(scrim.id!!)

            val request = UpdateScrimStatusRequest(status = ScrimStatus.SCHEDULED)

            // when & then
            mockMvc.perform(
                patch("/api/scrims/${scrim.id}/status")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("매치 추가")
    inner class AddMatch {

        @Test
        @DisplayName("매치 추가 성공")
        fun addMatch_Success() {
            // given
            val scrim = scrimService.createScrim("Test Scrim", futureTime)

            // when & then
            mockMvc.perform(
                post("/api/scrims/${scrim.id}/matches")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.roundNumber").value(1))
        }
    }
}
