package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.CreateStrategyRequest
import com.lumiaops.lumiaapi.dto.UpdateStrategyRequest
import com.lumiaops.lumiacore.domain.User
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
    @DisplayName("전략 조회 API")
    inner class GetStrategies {

        @Test
        @DisplayName("모든 전략 조회 성공")
        fun getStrategies_Success() {
            mockMvc.perform(
                get("/api/strategies")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
        }

        @Test
        @DisplayName("인증 없이 전략 조회 시 401")
        fun getStrategies_WithoutAuth_Returns401() {
            mockMvc.perform(get("/api/strategies"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("전략 생성 API")
    inner class CreateStrategy {

        @Test
        @DisplayName("전략 생성 성공")
        fun createStrategy_Success() {
            val request = CreateStrategyRequest(
                title = "Test Strategy",
                mapData = "{\"markers\": []}"
            )

            mockMvc.perform(
                post("/api/strategies")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("Test Strategy"))
        }
    }

    @Nested
    @DisplayName("전략 상세 조회 API")
    inner class GetStrategyById {

        @Test
        @DisplayName("전략 상세 조회 성공")
        fun getStrategy_Success() {
            // given
            val strategy = strategyService.createStrategy(
                "Test Strategy",
                "{\"markers\": []}",
                null,
                testUser.id!!
            )

            // when & then
            mockMvc.perform(
                get("/api/strategies/${strategy.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.title").value("Test Strategy"))
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
    @DisplayName("전략 수정 API")
    inner class UpdateStrategy {

        @Test
        @DisplayName("전략 수정 성공")
        fun updateStrategy_Success() {
            // given
            val strategy = strategyService.createStrategy(
                "Original Title",
                "{\"markers\": []}",
                null,
                testUser.id!!
            )

            val request = UpdateStrategyRequest(
                title = "Updated Title",
                mapData = "{\"markers\": [{\"x\": 100}]}"
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
        }
    }

    @Nested
    @DisplayName("전략 삭제 API")
    inner class DeleteStrategy {

        @Test
        @DisplayName("전략 삭제 성공")
        fun deleteStrategy_Success() {
            // given
            val strategy = strategyService.createStrategy(
                "Strategy to Delete",
                "{\"markers\": []}",
                null,
                testUser.id!!
            )

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
