package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.TeamMember
import com.lumiaops.lumiacore.domain.TeamRole
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.TeamMemberRepository
import com.lumiaops.lumiacore.repository.TeamRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("TeamService 단위 테스트")
class TeamServiceTest {

    @MockK
    private lateinit var teamRepository: TeamRepository

    @MockK
    private lateinit var teamMemberRepository: TeamMemberRepository

    @InjectMockKs
    private lateinit var teamService: TeamService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    @DisplayName("Team CRUD 테스트")
    inner class TeamCrudTest {
        
        @Test
        @DisplayName("팀 ID로 조회 성공")
        fun `should return team when team exists`() {
            // given
            val teamId = 1L
            val team = mockk<Team> {
                every { id } returns teamId
                every { name } returns "테스트팀"
            }
            every { teamRepository.findById(teamId) } returns Optional.of(team)

            // when
            val result = teamService.findById(teamId)

            // then
            assertNotNull(result)
            assertEquals(teamId, result.id)
            verify(exactly = 1) { teamRepository.findById(teamId) }
        }

        @Test
        @DisplayName("존재하지 않는 팀 ID로 조회 시 null 반환")
        fun `should return null when team does not exist`() {
            // given
            val teamId = 999L
            every { teamRepository.findById(teamId) } returns Optional.empty()

            // when
            val result = teamService.findById(teamId)

            // then
            assertNull(result)
        }

        @Test
        @DisplayName("팀 이름으로 조회 성공")
        fun `should return team when finding by name`() {
            // given
            val teamName = "테스트팀"
            val team = mockk<Team> {
                every { name } returns teamName
            }
            every { teamRepository.findByName(teamName) } returns team

            // when
            val result = teamService.findByName(teamName)

            // then
            assertNotNull(result)
            assertEquals(teamName, result.name)
        }

        @Test
        @DisplayName("팀 생성 성공")
        fun `should create team successfully`() {
            // given
            val name = "새팀"
            val description = "팀 설명"
            val ownerId = 1L
            val savedTeam = Team(name = name, description = description, ownerId = ownerId)
            
            every { teamRepository.save(any<Team>()) } returns savedTeam

            // when
            val result = teamService.createTeam(name, description, ownerId)

            // then
            assertEquals(name, result.name)
            assertEquals(description, result.description)
            assertEquals(ownerId, result.ownerId)
            verify(exactly = 1) { teamRepository.save(any<Team>()) }
        }

        @Test
        @DisplayName("팀 업데이트 성공")
        fun `should update team successfully`() {
            // given
            val teamId = 1L
            val team = Team(name = "기존팀", description = "기존설명", ownerId = 1L)
            val newName = "수정된팀"
            val newDescription = "수정된설명"
            
            every { teamRepository.findById(teamId) } returns Optional.of(team)

            // when
            val result = teamService.updateTeam(teamId, newName, newDescription)

            // then
            assertEquals(newName, result.name)
            assertEquals(newDescription, result.description)
        }

        @Test
        @DisplayName("존재하지 않는 팀 업데이트 시 예외 발생")
        fun `should throw exception when updating non-existent team`() {
            // given
            val teamId = 999L
            every { teamRepository.findById(teamId) } returns Optional.empty()

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                teamService.updateTeam(teamId, "팀명", "설명")
            }
            assertEquals("팀을 찾을 수 없습니다: $teamId", exception.message)
        }

        @Test
        @DisplayName("팀 삭제 성공")
        fun `should delete team successfully`() {
            // given
            val teamId = 1L
            every { teamRepository.deleteById(teamId) } just Runs

            // when
            teamService.deleteTeam(teamId)

            // then
            verify(exactly = 1) { teamRepository.deleteById(teamId) }
        }

        @Test
        @DisplayName("소유자 ID로 팀 목록 조회")
        fun `should find teams by owner id`() {
            // given
            val ownerId = 1L
            val teams = listOf(
                Team(name = "팀1", description = null, ownerId = ownerId),
                Team(name = "팀2", description = null, ownerId = ownerId)
            )
            every { teamRepository.findByOwnerId(ownerId) } returns teams

            // when
            val result = teamService.findByOwnerId(ownerId)

            // then
            assertEquals(2, result.size)
        }
    }

    @Nested
    @DisplayName("TeamMember 관리 테스트")
    inner class TeamMemberTest {
        
        private lateinit var team: Team
        private lateinit var user: User

        @BeforeEach
        fun setUpMember() {
            team = Team(name = "테스트팀", description = null, ownerId = 1L)
            user = User(email = "test@example.com", nickname = "테스트유저")
        }

        @Test
        @DisplayName("팀 멤버 여부 확인 - true")
        fun `should return true when user is member`() {
            // given
            every { teamMemberRepository.existsByTeamAndUser(team, user) } returns true

            // when
            val result = teamService.isMember(team, user)

            // then
            assertTrue(result)
        }

        @Test
        @DisplayName("팀 멤버 여부 확인 - false")
        fun `should return false when user is not member`() {
            // given
            every { teamMemberRepository.existsByTeamAndUser(team, user) } returns false

            // when
            val result = teamService.isMember(team, user)

            // then
            assertFalse(result)
        }

        @Test
        @DisplayName("팀 멤버 추가 성공")
        fun `should add member successfully`() {
            // given
            val newMember = TeamMember(team = team, user = user, role = TeamRole.MEMBER)
            every { teamMemberRepository.existsByTeamAndUser(team, user) } returns false
            every { teamMemberRepository.save(any<TeamMember>()) } returns newMember

            // when
            val result = teamService.addMember(team, user)

            // then
            assertEquals(TeamRole.MEMBER, result.role)
            verify(exactly = 1) { teamMemberRepository.save(any<TeamMember>()) }
        }

        @Test
        @DisplayName("이미 멤버인 사용자 추가 시 예외 발생")
        fun `should throw exception when adding existing member`() {
            // given
            every { teamMemberRepository.existsByTeamAndUser(team, user) } returns true

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                teamService.addMember(team, user)
            }
            assertEquals("이미 팀의 멤버입니다", exception.message)
        }

        @Test
        @DisplayName("팀 멤버 제거 성공")
        fun `should remove member successfully`() {
            // given
            val member = TeamMember(team = team, user = user, role = TeamRole.MEMBER)
            every { teamMemberRepository.findByTeamAndUser(team, user) } returns member
            every { teamMemberRepository.delete(member) } just Runs

            // when
            teamService.removeMember(team, user)

            // then
            verify(exactly = 1) { teamMemberRepository.delete(member) }
        }

        @Test
        @DisplayName("존재하지 않는 팀 멤버 제거 시 예외 발생")
        fun `should throw exception when removing non-existent member`() {
            // given
            every { teamMemberRepository.findByTeamAndUser(team, user) } returns null

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                teamService.removeMember(team, user)
            }
            assertEquals("팀 멤버를 찾을 수 없습니다", exception.message)
        }

        @Test
        @DisplayName("팀 멤버 역할 변경 성공")
        fun `should update member role successfully`() {
            // given
            val member = TeamMember(team = team, user = user, role = TeamRole.MEMBER)
            every { teamMemberRepository.findByTeamAndUser(team, user) } returns member

            // when
            val result = teamService.updateMemberRole(team, user, TeamRole.LEADER)

            // then
            assertEquals(TeamRole.LEADER, result.role)
        }

        @Test
        @DisplayName("팀의 멤버 목록 조회")
        fun `should find members by team`() {
            // given
            val members = listOf(
                TeamMember(team = team, user = user, role = TeamRole.MEMBER)
            )
            every { teamMemberRepository.findByTeam(team) } returns members

            // when
            val result = teamService.findMembersByTeam(team)

            // then
            assertEquals(1, result.size)
        }

        @Test
        @DisplayName("사용자가 속한 팀 목록 조회")
        fun `should find teams by user`() {
            // given
            val memberships = listOf(
                TeamMember(team = team, user = user, role = TeamRole.MEMBER)
            )
            every { teamMemberRepository.findByUser(user) } returns memberships

            // when
            val result = teamService.findTeamsByUser(user)

            // then
            assertEquals(1, result.size)
        }
    }
}
