package com.lumiaops.lumiacore.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("TeamMember 엔티티 단위 테스트")
class TeamMemberTest {

    @Nested
    @DisplayName("TeamMember 생성 테스트")
    inner class TeamMemberCreationTest {

        @Test
        @DisplayName("TeamMember 객체 생성 성공")
        fun `should create team member with valid parameters`() {
            // given
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "테스트유저")
            val role = TeamRole.MEMBER

            // when
            val teamMember = TeamMember(team = team, user = user, role = role)

            // then
            assertEquals(team, teamMember.team)
            assertEquals(user, teamMember.user)
            assertEquals(role, teamMember.role)
        }

        @Test
        @DisplayName("기본 역할은 MEMBER")
        fun `should have default role as MEMBER`() {
            // given
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "테스트유저")

            // when
            val teamMember = TeamMember(team = team, user = user)

            // then
            assertEquals(TeamRole.MEMBER, teamMember.role)
        }

        @Test
        @DisplayName("OWNER 역할로 TeamMember 생성")
        fun `should create team member with OWNER role`() {
            // given
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val user = User(email = "owner@example.com", password = "encodedPassword", nickname = "팀장")

            // when
            val teamMember = TeamMember(team = team, user = user, role = TeamRole.OWNER)

            // then
            assertEquals(TeamRole.OWNER, teamMember.role)
        }

        @Test
        @DisplayName("LEADER 역할로 TeamMember 생성")
        fun `should create team member with LEADER role`() {
            // given
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val user = User(email = "leader@example.com", password = "encodedPassword", nickname = "리더")

            // when
            val teamMember = TeamMember(team = team, user = user, role = TeamRole.LEADER)

            // then
            assertEquals(TeamRole.LEADER, teamMember.role)
        }
    }

    @Nested
    @DisplayName("TeamMember 역할 수정 테스트")
    inner class TeamMemberRoleUpdateTest {

        @Test
        @DisplayName("역할 변경 성공")
        fun `should update role successfully`() {
            // given
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "테스트유저")
            val teamMember = TeamMember(team = team, user = user, role = TeamRole.MEMBER)

            // when
            teamMember.role = TeamRole.LEADER

            // then
            assertEquals(TeamRole.LEADER, teamMember.role)
        }

        @Test
        @DisplayName("MEMBER에서 OWNER로 역할 변경")
        fun `should change role from MEMBER to OWNER`() {
            // given
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "테스트유저")
            val teamMember = TeamMember(team = team, user = user, role = TeamRole.MEMBER)

            // when
            teamMember.role = TeamRole.OWNER

            // then
            assertEquals(TeamRole.OWNER, teamMember.role)
        }
    }

    @Nested
    @DisplayName("TeamRole enum 테스트")
    inner class TeamRoleTest {

        @Test
        @DisplayName("TeamRole에 3가지 역할이 존재")
        fun `should have three roles`() {
            // then
            assertEquals(3, TeamRole.entries.size)
        }

        @Test
        @DisplayName("TeamRole 값 확인")
        fun `should have correct role values`() {
            // then
            assertEquals("OWNER", TeamRole.OWNER.name)
            assertEquals("LEADER", TeamRole.LEADER.name)
            assertEquals("MEMBER", TeamRole.MEMBER.name)
        }
    }
}
