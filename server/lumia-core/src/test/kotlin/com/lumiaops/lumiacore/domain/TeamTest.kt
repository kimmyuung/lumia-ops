package com.lumiaops.lumiacore.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DisplayName("Team 엔티티 단위 테스트")
class TeamTest {

    @Nested
    @DisplayName("Team 생성 테스트")
    inner class TeamCreationTest {

        @Test
        @DisplayName("Team 객체 생성 성공 (설명 포함)")
        fun `should create team with description`() {
            // given
            val name = "테스트팀"
            val description = "팀 설명입니다"
            val ownerId = 1L

            // when
            val team = Team(name = name, description = description, ownerId = ownerId)

            // then
            assertEquals(name, team.name)
            assertEquals(description, team.description)
            assertEquals(ownerId, team.ownerId)
        }

        @Test
        @DisplayName("Team 객체 생성 성공 (설명 없이)")
        fun `should create team without description`() {
            // given
            val name = "테스트팀"
            val ownerId = 1L

            // when
            val team = Team(name = name, description = null, ownerId = ownerId)

            // then
            assertEquals(name, team.name)
            assertNull(team.description)
            assertEquals(ownerId, team.ownerId)
        }
    }

    @Nested
    @DisplayName("Team 수정 테스트")
    inner class TeamUpdateTest {

        @Test
        @DisplayName("팀 이름 수정")
        fun `should update team name`() {
            // given
            val team = Team(name = "기존팀명", description = null, ownerId = 1L)
            val newName = "새팀명"

            // when
            team.name = newName

            // then
            assertEquals(newName, team.name)
        }

        @Test
        @DisplayName("팀 설명 수정")
        fun `should update team description`() {
            // given
            val team = Team(name = "팀명", description = null, ownerId = 1L)
            val newDescription = "새로운 설명"

            // when
            team.description = newDescription

            // then
            assertEquals(newDescription, team.description)
        }
    }

    @Nested
    @DisplayName("Team BaseTimeEntity 상속 테스트")
    inner class TeamTimeEntityTest {

        @Test
        @DisplayName("생성 시 createdAt이 설정됨")
        fun `should have createdAt when created`() {
            // given & when
            val team = Team(name = "팀", description = null, ownerId = 1L)

            // then
            assertNotNull(team.createdAt)
        }

        @Test
        @DisplayName("생성 시 updatedAt이 설정됨")
        fun `should have updatedAt when created`() {
            // given & when
            val team = Team(name = "팀", description = null, ownerId = 1L)

            // then
            assertNotNull(team.updatedAt)
        }
    }
}
