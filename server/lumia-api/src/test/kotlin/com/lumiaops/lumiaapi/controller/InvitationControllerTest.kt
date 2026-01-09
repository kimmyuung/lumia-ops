package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.dto.CreateInvitationRequest
import com.lumiaops.lumiacore.domain.*
import com.lumiaops.lumiacore.service.InvitationService
import com.lumiaops.lumiacore.service.TeamService
import com.lumiaops.lumiacore.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("InvitationController 통합 테스트")
class InvitationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var invitationService: InvitationService

    @MockBean
    private lateinit var teamService: TeamService

    @MockBean
    private lateinit var userService: UserService

    private lateinit var testUser: User
    private lateinit var testTeam: Team
    private lateinit var testInvitation: TeamInvitation

    @BeforeEach
    fun setUp() {
        testUser = User(
            email = "test@example.com",
            password = "password123"
        ).apply {
            val idField = User::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }

        testTeam = Team(
            name = "Test Team",
            description = "Test Description"
        ).apply {
            val idField = Team::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }

        testInvitation = TeamInvitation(
            team = testTeam,
            invitedEmail = "invitee@example.com",
            invitedBy = testUser,
            role = TeamRole.MEMBER
        ).apply {
            val idField = TeamInvitation::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
    }

    @Nested
    @DisplayName("POST /teams/{teamId}/invitations")
    inner class CreateInvitation {

        @Test
        @WithMockUser(username = "test@example.com")
        fun `팀 초대 생성 성공`() {
            val request = CreateInvitationRequest(
                email = "invitee@example.com",
                role = TeamRole.MEMBER.name,
                message = "팀에 합류해주세요"
            )

            `when`(teamService.findById(1L)).thenReturn(testTeam)
            `when`(invitationService.createInvitation(any(), any(), any(), any(), any()))
                .thenReturn(Pair(testInvitation, true))

            mockMvc.perform(post("/api/teams/1/invitations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated)
        }
    }

    @Nested
    @DisplayName("GET /teams/{teamId}/invitations")
    inner class GetTeamInvitations {

        @Test
        @WithMockUser
        fun `팀 초대 목록 조회 성공`() {
            `when`(invitationService.getTeamInvitations(1L)).thenReturn(listOf(testInvitation))

            mockMvc.perform(get("/teams/1/invitations"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].invitedEmail").value("invitee@example.com"))
        }
    }

    @Nested
    @DisplayName("DELETE /teams/{teamId}/invitations/{invitationId}")
    inner class CancelInvitation {

        @Test
        @WithMockUser
        fun `초대 취소 성공`() {
            `when`(invitationService.cancelInvitation(1L)).thenReturn(true)

            mockMvc.perform(delete("/teams/1/invitations/1"))
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    @DisplayName("GET /invitations/{token}")
    inner class GetInvitationByToken {

        @Test
        fun `토큰으로 초대 조회 성공`() {
            val token = "test-token-123"
            `when`(invitationService.getInvitationByToken(token)).thenReturn(testInvitation)

            mockMvc.perform(get("/invitations/$token"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.invitedEmail").value("invitee@example.com"))
        }

        @Test
        fun `존재하지 않는 토큰 조회시 404 반환`() {
            `when`(invitationService.getInvitationByToken("invalid-token")).thenReturn(null)

            mockMvc.perform(get("/invitations/invalid-token"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("POST /invitations/{token}/accept")
    inner class AcceptInvitation {

        @Test
        @WithMockUser(username = "invitee@example.com")
        fun `초대 수락 성공`() {
            val token = "test-token-123"
            val member = TeamMember(team = testTeam, user = testUser, role = TeamRole.MEMBER)
            `when`(invitationService.acceptInvitation(token, testUser)).thenReturn(member)

            mockMvc.perform(post("/api/invitations/$token/accept"))
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("POST /invitations/{token}/decline")
    inner class DeclineInvitation {

        @Test
        fun `초대 거절 성공`() {
            val token = "test-token-123"
            `when`(invitationService.declineInvitation(token)).thenReturn(true)

            mockMvc.perform(post("/invitations/$token/decline"))
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("GET /invitations/pending")
    inner class GetMyPendingInvitations {

        @Test
        @WithMockUser(username = "invitee@example.com")
        fun `대기 중인 초대 목록 조회 성공`() {
            `when`(invitationService.getMyPendingInvitations("invitee@example.com"))
                .thenReturn(listOf(testInvitation))

            mockMvc.perform(get("/api/invitations/pending"))
                .andExpect(status().isOk)
        }
    }
}
