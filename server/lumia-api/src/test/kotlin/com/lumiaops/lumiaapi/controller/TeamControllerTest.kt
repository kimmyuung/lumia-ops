package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.TestConfig
import com.lumiaops.lumiaapi.dto.CreateTeamRequest
import com.lumiaops.lumiaapi.dto.UpdateTeamRequest
import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.TeamRole
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.TeamMemberRepository
import com.lumiaops.lumiacore.repository.TeamRepository
import com.lumiaops.lumiacore.repository.UserRepository
import com.lumiaops.lumiacore.security.JwtTokenProvider
import com.lumiaops.lumiacore.service.TeamService
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
    private lateinit var teamMemberRepository: TeamMemberRepository

    @Autowired
    private lateinit var teamService: TeamService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var testUser: User
    private lateinit var authToken: String

    @BeforeEach
    fun setUp() {
        teamMemberRepository.deleteAll()
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

        // JWT 토큰 생성
        authToken = jwtTokenProvider.generateAccessToken(testUser.id!!, testUser.email ?: "")
    }

    @Nested
    @DisplayName("팀 목록 조회")
    inner class GetTeams {

        @Test
        @DisplayName("인증된 사용자가 팀 목록을 조회할 수 있다")
        fun getTeams_Authenticated_Success() {
            // given
            val team = teamService.createTeam("Test Team", "Description", testUser.id!!)
            teamService.addMember(team, testUser, TeamRole.OWNER)

            // when & then
            mockMvc.perform(
                get("/api/teams")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$").isArray)
        }

        @Test
        @DisplayName("인증되지 않은 사용자는 401을 받는다")
        fun getTeams_Unauthenticated_Returns401() {
            mockMvc.perform(get("/api/teams"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("팀 생성")
    inner class CreateTeam {

        @Test
        @DisplayName("팀 생성 성공")
        fun createTeam_Success() {
            val request = CreateTeamRequest(
                name = "New Team",
                description = "Team description"
            )

            mockMvc.perform(
                post("/api/teams")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("New Team"))
                .andExpect(jsonPath("$.description").value("Team description"))
        }

        @Test
        @DisplayName("팀 이름이 비어있으면 실패")
        fun createTeam_EmptyName_Fails() {
            val request = mapOf(
                "name" to "",
                "description" to "Description"
            )

            mockMvc.perform(
                post("/api/teams")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("인증 없이 팀 생성 시 401")
        fun createTeam_Unauthenticated_Returns401() {
            val request = CreateTeamRequest(name = "Team", description = null)

            mockMvc.perform(
                post("/api/teams")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("팀 상세 조회")
    inner class GetTeam {

        @Test
        @DisplayName("팀 상세 조회 성공")
        fun getTeam_Success() {
            // given
            val team = teamService.createTeam("Test Team", "Description", testUser.id!!)
            teamService.addMember(team, testUser, TeamRole.OWNER)

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
    @DisplayName("팀 수정")
    inner class UpdateTeam {

        @Test
        @DisplayName("팀 수정 성공")
        fun updateTeam_Success() {
            // given
            val team = teamService.createTeam("Old Name", "Old Description", testUser.id!!)
            teamService.addMember(team, testUser, TeamRole.OWNER)

            val request = UpdateTeamRequest(
                name = "New Name",
                description = "New Description"
            )

            // when & then
            mockMvc.perform(
                patch("/api/teams/${team.id}")
                    .header("Authorization", "Bearer $authToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.description").value("New Description"))
        }
    }

    @Nested
    @DisplayName("팀 삭제")
    inner class DeleteTeam {

        @Test
        @DisplayName("팀 삭제 성공")
        fun deleteTeam_Success() {
            // given
            val team = teamService.createTeam("To Delete", null, testUser.id!!)
            teamService.addMember(team, testUser, TeamRole.OWNER)

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

    @Nested
    @DisplayName("내 팀 조회")
    inner class GetMyTeam {

        @Test
        @DisplayName("내 팀 조회 성공")
        fun getMyTeam_Success() {
            // given
            val team = teamService.createTeam("My Team", "Description", testUser.id!!)
            teamService.addMember(team, testUser, TeamRole.OWNER)

            // when & then
            mockMvc.perform(
                get("/api/teams/me")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.name").value("My Team"))
        }

        @Test
        @DisplayName("팀이 없으면 null 반환")
        fun getMyTeam_NoTeam_ReturnsNull() {
            mockMvc.perform(
                get("/api/teams/me")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
                .andExpect(content().string(""))
        }
    }

    @Nested
    @DisplayName("팀 탈퇴")
    inner class LeaveTeam {

        @Test
        @DisplayName("팀 탈퇴 성공")
        fun leaveTeam_Success() {
            // given - 팀 생성 및 다른 멤버 추가
            val team = teamService.createTeam("Team", null, testUser.id!!)
            teamService.addMember(team, testUser, TeamRole.MEMBER) // MEMBER로 추가

            // when & then
            mockMvc.perform(
                post("/api/teams/${team.id}/leave")
                    .header("Authorization", "Bearer $authToken")
            )
                .andExpect(status().isOk)
        }
    }
}
