package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.CreateTeamRequest
import com.lumiaops.lumiaapi.dto.UpdateTeamRequest
import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.TeamRepository
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
@DisplayName("TeamController 통합 테스트")
class TeamControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var teamRepository: TeamRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var testUser: User
    private lateinit var authToken: String

    @BeforeEach
    fun setUp() {
        teamRepository.deleteAll()
        userRepository.deleteAll()

        // 테스트 사용자 생성
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
    @DisplayName("팀 조회 API")
    inner class GetTeams {

        @Test
        @DisplayName("모든 팀 조회 성공")
        fun getTeams_Success() {
            mockMvc.perform(
                get("/api/teams")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
        }

        @Test
        @DisplayName("인증 없이 팀 조회 시 401")
        fun getTeams_WithoutAuth_Returns401() {
            mockMvc.perform(get("/api/teams"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("팀 생성 API")
    inner class CreateTeam {

        @Test
        @DisplayName("팀 생성 성공")
        fun createTeam_Success() {
            val request = CreateTeamRequest(
                name = "Test Team",
                description = "A test team description"
            )

            mockMvc.perform(
                post("/api/teams")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Test Team"))
                .andExpect(jsonPath("$.description").value("A test team description"))
        }

        @Test
        @DisplayName("인증 없이 팀 생성 시 401")
        fun createTeam_WithoutAuth_Returns401() {
            val request = CreateTeamRequest(
                name = "Test Team",
                description = "A test team description"
            )

            mockMvc.perform(
                post("/api/teams")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("팀 상세 조회 API")
    inner class GetTeamById {

        @Test
        @DisplayName("팀 상세 조회 성공")
        fun getTeam_Success() {
            // given
            val team = teamRepository.save(
                Team(name = "Test Team", description = "Description", ownerId = testUser.id!!)
            )

            // when & then
            mockMvc.perform(
                get("/api/teams/${team.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Test Team"))
        }

        @Test
        @DisplayName("존재하지 않는 팀 조회 시 404")
        fun getTeam_NotFound_Returns404() {
            mockMvc.perform(
                get("/api/teams/99999")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("팀 수정 API")
    inner class UpdateTeam {

        @Test
        @DisplayName("팀 정보 수정 성공")
        fun updateTeam_Success() {
            // given
            val team = teamRepository.save(
                Team(name = "Original Name", description = "Original Description", ownerId = testUser.id!!)
            )

            val request = UpdateTeamRequest(
                name = "Updated Name",
                description = "Updated Description"
            )

            // when & then
            mockMvc.perform(
                patch("/api/teams/${team.id}")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
        }
    }

    @Nested
    @DisplayName("팀 삭제 API")
    inner class DeleteTeam {

        @Test
        @DisplayName("팀 삭제 성공")
        fun deleteTeam_Success() {
            // given
            val team = teamRepository.save(
                Team(name = "Team to Delete", description = "Description", ownerId = testUser.id!!)
            )

            // when & then
            mockMvc.perform(
                delete("/api/teams/${team.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)

            // verify deletion
            mockMvc.perform(
                get("/api/teams/${team.id}")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isNotFound)
        }
    }
}
