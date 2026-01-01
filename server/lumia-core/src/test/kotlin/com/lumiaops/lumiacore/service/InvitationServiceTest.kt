package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.*
import com.lumiaops.lumiacore.repository.TeamInvitationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("InvitationService 테스트")
class InvitationServiceTest {

    @MockK
    private lateinit var invitationRepository: TeamInvitationRepository

    @MockK
    private lateinit var teamService: TeamService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var emailService: EmailService

    private lateinit var invitationService: InvitationService

    private lateinit var testTeam: Team
    private lateinit var testInviter: User
    private lateinit var testUser: User

    @BeforeEach
    fun setUp() {
        invitationService = InvitationService(
            invitationRepository,
            teamService,
            userRepository,
            emailService
        )

        testTeam = mockk<Team> {
            every { id } returns 1L
            every { name } returns "테스트팀"
        }

        testInviter = mockk<User> {
            every { id } returns 1L
            every { email } returns "inviter@example.com"
            every { nickname } returns "초대자"
        }

        testUser = mockk<User> {
            every { id } returns 2L
            every { email } returns "invited@example.com"
            every { nickname } returns "초대받은유저"
        }
    }

    @Nested
    @DisplayName("createInvitation 테스트")
    inner class CreateInvitationTest {

        @Test
        fun `should create invitation and send email`() {
            // given
            val invitedEmail = "invited@example.com"
            val savedInvitation = mockk<TeamInvitation> {
                every { id } returns 1L
                every { token } returns "test-token"
                every { team } returns testTeam
                every { invitedBy } returns testInviter
                every { invitedEmail } returns invitedEmail
                every { message } returns null
            }

            every {
                invitationRepository.existsByTeamAndInvitedEmailAndStatus(
                    testTeam, invitedEmail, InvitationStatus.PENDING
                )
            } returns false

            every { userRepository.findByEmail(invitedEmail) } returns null
            every { invitationRepository.save(any()) } returns savedInvitation
            every { emailService.sendInvitationEmail(savedInvitation) } returns true

            // when
            val (invitation, emailSent) = invitationService.createInvitation(
                team = testTeam,
                invitedEmail = invitedEmail,
                inviter = testInviter
            )

            // then
            assertNotNull(invitation)
            assertTrue(emailSent)
            verify(exactly = 1) { invitationRepository.save(any()) }
            verify(exactly = 1) { emailService.sendInvitationEmail(any()) }
        }

        @Test
        fun `should throw exception when invitation already exists`() {
            // given
            val invitedEmail = "invited@example.com"

            every {
                invitationRepository.existsByTeamAndInvitedEmailAndStatus(
                    testTeam, invitedEmail, InvitationStatus.PENDING
                )
            } returns true

            // when & then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                invitationService.createInvitation(
                    team = testTeam,
                    invitedEmail = invitedEmail,
                    inviter = testInviter
                )
            }

            assertEquals("이미 해당 이메일로 대기 중인 초대가 있습니다", exception.message)
        }

        @Test
        fun `should throw exception when user is already a member`() {
            // given
            val invitedEmail = "member@example.com"
            val existingMember = mockk<User>()

            every {
                invitationRepository.existsByTeamAndInvitedEmailAndStatus(
                    testTeam, invitedEmail, InvitationStatus.PENDING
                )
            } returns false

            every { userRepository.findByEmail(invitedEmail) } returns existingMember
            every { teamService.isMember(testTeam, existingMember) } returns true

            // when & then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                invitationService.createInvitation(
                    team = testTeam,
                    invitedEmail = invitedEmail,
                    inviter = testInviter
                )
            }

            assertEquals("이미 팀의 멤버입니다", exception.message)
        }
    }

    @Nested
    @DisplayName("getInvitationByToken 테스트")
    inner class GetInvitationByTokenTest {

        @Test
        fun `should return invitation when token is valid`() {
            // given
            val token = "valid-token"
            val invitation = mockk<TeamInvitation> {
                every { isExpired() } returns false
                every { status } returns InvitationStatus.PENDING
            }

            every { invitationRepository.findByToken(token) } returns invitation

            // when
            val result = invitationService.getInvitationByToken(token)

            // then
            assertNotNull(result)
            verify(exactly = 1) { invitationRepository.findByToken(token) }
        }

        @Test
        fun `should throw exception when token is invalid`() {
            // given
            val token = "invalid-token"
            every { invitationRepository.findByToken(token) } returns null

            // when & then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                invitationService.getInvitationByToken(token)
            }

            assertEquals("유효하지 않은 초대입니다", exception.message)
        }
    }

    @Nested
    @DisplayName("acceptInvitation 테스트")
    inner class AcceptInvitationTest {

        @Test
        fun `should accept invitation and add member to team`() {
            // given
            val token = "test-token"
            val invitation = mockk<TeamInvitation>(relaxed = true) {
                every { id } returns 1L
                every { invitedEmail } returns "invited@example.com"
                every { canAccept() } returns true
                every { isExpired() } returns false
                every { status } returns InvitationStatus.PENDING
                every { team } returns testTeam
                every { role } returns TeamRole.MEMBER
            }

            val newMember = mockk<TeamMember> {
                every { team } returns testTeam
            }

            every { invitationRepository.findByToken(token) } returns invitation
            every { teamService.addMember(testTeam, testUser, TeamRole.MEMBER) } returns newMember

            // when
            val result = invitationService.acceptInvitation(token, testUser)

            // then
            assertNotNull(result)
            verify(exactly = 1) { invitation.accept() }
            verify(exactly = 1) { teamService.addMember(testTeam, testUser, TeamRole.MEMBER) }
        }

        @Test
        fun `should throw exception when email does not match`() {
            // given
            val token = "test-token"
            val differentUser = mockk<User> {
                every { email } returns "different@example.com"
            }
            val invitation = mockk<TeamInvitation>(relaxed = true) {
                every { invitedEmail } returns "invited@example.com"
                every { isExpired() } returns false
                every { status } returns InvitationStatus.PENDING
            }

            every { invitationRepository.findByToken(token) } returns invitation

            // when & then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                invitationService.acceptInvitation(token, differentUser)
            }

            assertEquals("초대받은 이메일과 일치하지 않습니다", exception.message)
        }
    }

    @Nested
    @DisplayName("declineInvitation 테스트")
    inner class DeclineInvitationTest {

        @Test
        fun `should decline invitation`() {
            // given
            val token = "test-token"
            val invitation = mockk<TeamInvitation>(relaxed = true) {
                every { id } returns 1L
                every { isExpired() } returns false
                every { status } returns InvitationStatus.PENDING
            }

            every { invitationRepository.findByToken(token) } returns invitation

            // when
            invitationService.declineInvitation(token)

            // then
            verify(exactly = 1) { invitation.decline() }
        }
    }

    @Nested
    @DisplayName("cancelInvitation 테스트")
    inner class CancelInvitationTest {

        @Test
        fun `should cancel invitation`() {
            // given
            val invitationId = 1L
            val invitation = mockk<TeamInvitation>(relaxed = true) {
                every { id } returns invitationId
            }

            every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

            // when
            invitationService.cancelInvitation(invitationId)

            // then
            verify(exactly = 1) { invitation.cancel() }
        }

        @Test
        fun `should throw exception when invitation not found`() {
            // given
            val invitationId = 999L
            every { invitationRepository.findById(invitationId) } returns Optional.empty()

            // when & then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                invitationService.cancelInvitation(invitationId)
            }

            assertEquals("초대를 찾을 수 없습니다", exception.message)
        }
    }

    @Nested
    @DisplayName("resendInvitation 테스트")
    inner class ResendInvitationTest {

        @Test
        fun `should resend invitation email`() {
            // given
            val invitationId = 1L
            val invitation = mockk<TeamInvitation> {
                every { id } returns invitationId
                every { status } returns InvitationStatus.PENDING
                every { isExpired() } returns false
            }

            every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)
            every { emailService.sendInvitationEmail(invitation) } returns true

            // when
            val result = invitationService.resendInvitation(invitationId)

            // then
            assertTrue(result)
            verify(exactly = 1) { emailService.sendInvitationEmail(invitation) }
        }

        @Test
        fun `should throw exception when invitation is not pending`() {
            // given
            val invitationId = 1L
            val invitation = mockk<TeamInvitation> {
                every { status } returns InvitationStatus.ACCEPTED
            }

            every { invitationRepository.findById(invitationId) } returns Optional.of(invitation)

            // when & then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                invitationService.resendInvitation(invitationId)
            }

            assertEquals("대기 중인 초대만 재발송할 수 있습니다", exception.message)
        }
    }
}
