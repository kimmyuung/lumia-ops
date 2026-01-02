package com.lumiaops.lumiacore.domain

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("TeamInvitation 엔티티 테스트")
class TeamInvitationTest {

    private fun createTestUser(id: Long = 1L): User {
        return mockk<User>(relaxed = true) {
            every { this@mockk.id } returns id
            every { email } returns "test$id@example.com"
            every { nickname } returns "테스트유저$id"
        }
    }

    private fun createTestTeam(id: Long = 1L): Team {
        return mockk<Team>(relaxed = true) {
            every { this@mockk.id } returns id
            every { name } returns "테스트팀"
        }
    }

    @Nested
    @DisplayName("초대 생성")
    inner class CreateInvitationTest {

        @Test
        fun `should create invitation with default values`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitedEmail = "invited@example.com"

            // when
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = invitedEmail,
                invitedBy = inviter
            )

            // then
            assertEquals(invitedEmail, invitation.invitedEmail)
            assertEquals(team, invitation.team)
            assertEquals(inviter, invitation.invitedBy)
            assertEquals(TeamRole.MEMBER, invitation.role)
            assertEquals(InvitationStatus.PENDING, invitation.status)
            assertNotNull(invitation.token)
            assertTrue(invitation.token.isNotBlank())
            assertNull(invitation.message)
            assertNull(invitation.respondedAt)
        }

        @Test
        fun `should create invitation with custom role and message`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()

            // when
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter,
                role = TeamRole.LEADER,
                message = "팀에 합류해주세요!"
            )

            // then
            assertEquals(TeamRole.LEADER, invitation.role)
            assertEquals("팀에 합류해주세요!", invitation.message)
        }

        @Test
        fun `should generate unique token for each invitation`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()

            // when
            val invitation1 = TeamInvitation(team = team, invitedEmail = "a@a.com", invitedBy = inviter)
            val invitation2 = TeamInvitation(team = team, invitedEmail = "b@b.com", invitedBy = inviter)

            // then
            assertNotEquals(invitation1.token, invitation2.token)
        }
    }

    @Nested
    @DisplayName("초대 만료 확인")
    inner class ExpirationTest {

        @Test
        fun `should not be expired when within expiry time`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter,
                expiresAt = LocalDateTime.now().plusDays(7)
            )

            // when & then
            assertFalse(invitation.isExpired())
            assertTrue(invitation.canAccept())
        }

        @Test
        fun `should be expired when past expiry time`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter,
                expiresAt = LocalDateTime.now().minusDays(1)
            )

            // when & then
            assertTrue(invitation.isExpired())
            assertFalse(invitation.canAccept())
        }
    }

    @Nested
    @DisplayName("초대 수락")
    inner class AcceptInvitationTest {

        @Test
        fun `should accept pending invitation`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter
            )

            // when
            invitation.accept()

            // then
            assertEquals(InvitationStatus.ACCEPTED, invitation.status)
            assertNotNull(invitation.respondedAt)
        }

        @Test
        fun `should throw exception when accepting expired invitation`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter,
                expiresAt = LocalDateTime.now().minusDays(1)
            )

            // when & then
            assertThrows(IllegalArgumentException::class.java) {
                invitation.accept()
            }
        }

        @Test
        fun `should throw exception when accepting already accepted invitation`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter
            )
            invitation.accept()

            // when & then
            assertThrows(IllegalArgumentException::class.java) {
                invitation.accept()
            }
        }
    }

    @Nested
    @DisplayName("초대 거절")
    inner class DeclineInvitationTest {

        @Test
        fun `should decline pending invitation`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter
            )

            // when
            invitation.decline()

            // then
            assertEquals(InvitationStatus.DECLINED, invitation.status)
            assertNotNull(invitation.respondedAt)
        }

        @Test
        fun `should throw exception when declining already declined invitation`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter
            )
            invitation.decline()

            // when & then
            assertThrows(IllegalArgumentException::class.java) {
                invitation.decline()
            }
        }
    }

    @Nested
    @DisplayName("초대 취소")
    inner class CancelInvitationTest {

        @Test
        fun `should cancel pending invitation`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter
            )

            // when
            invitation.cancel()

            // then
            assertEquals(InvitationStatus.CANCELLED, invitation.status)
        }

        @Test
        fun `should throw exception when cancelling already accepted invitation`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter
            )
            invitation.accept()

            // when & then
            assertThrows(IllegalArgumentException::class.java) {
                invitation.cancel()
            }
        }
    }

    @Nested
    @DisplayName("만료 처리")
    inner class MarkAsExpiredTest {

        @Test
        fun `should mark as expired when pending and past expiry time`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter,
                expiresAt = LocalDateTime.now().minusDays(1)
            )

            // when
            invitation.markAsExpired()

            // then
            assertEquals(InvitationStatus.EXPIRED, invitation.status)
        }

        @Test
        fun `should not mark as expired when not expired yet`() {
            // given
            val team = createTestTeam()
            val inviter = createTestUser()
            val invitation = TeamInvitation(
                team = team,
                invitedEmail = "invited@example.com",
                invitedBy = inviter,
                expiresAt = LocalDateTime.now().plusDays(7)
            )

            // when
            invitation.markAsExpired()

            // then
            assertEquals(InvitationStatus.PENDING, invitation.status)
        }
    }
}
