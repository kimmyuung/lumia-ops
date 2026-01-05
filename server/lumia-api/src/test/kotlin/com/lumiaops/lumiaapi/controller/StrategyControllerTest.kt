package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.CreateStrategyRequest
import com.lumiaops.lumiaapi.dto.UpdateStrategyRequest
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.domain.strategy.Strategy
import com.lumiaops.lumiacore.repository.StrategyRepository
import com.lumiaops.lumiacore.repository.UserRepository
import com.lumiaops.lumiacore.security.JwtTokenProvider
import com.lumiaops.lumiacore.service.StrategyService
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
@DisplayName("StrategyController 통합 테스트")
class StrategyControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var strategyRepository: StrategyRepository

    @Autowired
    private lateinit var strategyService: StrategyService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var testUser: User
    private lateinit var authToken: String

    @BeforeEach
    fun setUp() {
        strategyRepository.deleteAll()
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
    @DisplayName("전략 목록 조회")
    inner class GetStrategies {

        @Test
        @DisplayName("인증된 사용자가 전략 목록을 조회할 수 있다")
        fun getStrategies_Authenticated_Success() {
            // given
            strategyService.createStrategy("Test Strategy", "{}", null, testUser.id!!)

            // when & then
            mockMvc.perform(
                get("/api/strategies")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
                .andExpect(jsonPath("$[0].title").value("Test Strategy"))
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        fun getStrategies_Unauthenticated_Returns401() {
            mockMvc.perform(get("/api/strategies"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("전략 상세 조회")
    inner class GetStrategy {

        @Test
        @DisplayName("전략 상세 조회 성공")
        fun getStrategy_Success() {
            // given
            val strategy = strategyService.createStrategy("Detail Strategy", "{\"test\": true}", null, testUser.id!!)

            // when & then
            mockMvc.perform(
                get("/api/strategies/${strategy.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("Detail Strategy"))
                .andExpect(jsonPath("$.mapData").value("{\"test\": true}"))
        }

        @Test
        @DisplayName("존재하지 않는 전략 조회 시 404")
        fun getStrategy_NotFound_Returns404() {
            mockMvc.perform(
                get("/api/strategies/99999")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("전략 생성")
    inner class CreateStrategy {

        @Test
        @DisplayName("전략 생성 성공")
        fun createStrategy_Success() {
            val request = CreateStrategyRequest(
                title = "New Strategy",
                mapData = "{\"markers\": []}"
            )

            mockMvc.perform(
                post("/api/strategies")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("New Strategy"))
                .andExpect(jsonPath("$.mapData").value("{\"markers\": []}"))
        }

        @Test
        @DisplayName("제목이 비어있으면 실패")
        fun createStrategy_EmptyTitle_Fails() {
            val request = mapOf(
                "title" to "",
                "mapData" to "{}"
            )

            mockMvc.perform(
                post("/api/strategies")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("인증 없이 전략 생성 시 401")
        fun createStrategy_Unauthenticated_Returns401() {
            val request = CreateStrategyRequest(title = "Strategy", mapData = "{}")

            mockMvc.perform(
                post("/api/strategies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("전략 수정")
    inner class UpdateStrategy {

        @Test
        @DisplayName("전략 수정 성공")
        fun updateStrategy_Success() {
            // given
            val strategy = strategyService.createStrategy("Old Title", "{}", null, testUser.id!!)

            val request = UpdateStrategyRequest(
                title = "Updated Title",
                mapData = "{\"updated\": true}"
            )

            // when & then
            mockMvc.perform(
                patch("/api/strategies/${strategy.id}")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.mapData").value("{\"updated\": true}"))
        }
    }

    @Nested
    @DisplayName("전략 삭제")
    inner class DeleteStrategy {

        @Test
        @DisplayName("전략 삭제 성공")
        fun deleteStrategy_Success() {
            // given
            val strategy = strategyService.createStrategy("To Delete", "{}", null, testUser.id!!)

            // when & then
            mockMvc.perform(
                delete("/api/strategies/${strategy.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)

            // verify deletion
            mockMvc.perform(
                get("/api/strategies/${strategy.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }
    }
}
