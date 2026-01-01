package com.lumiaops.lumiacore.domain.strategy

import com.lumiaops.lumiacore.domain.Team
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DisplayName("Strategy 엔티티 단위 테스트")
class StrategyTest {

    @Nested
    @DisplayName("Strategy 생성 테스트")
    inner class StrategyCreationTest {

        @Test
        @DisplayName("팀 전략 생성 성공")
        fun `should create team strategy with valid parameters`() {
            // given
            val title = "공격 전략 A"
            val mapData = "{\"markers\": [{\"x\": 100, \"y\": 200}]}"
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val createdBy = 1L

            // when
            val strategy = Strategy(title = title, mapData = mapData, team = team, createdBy = createdBy)

            // then
            assertEquals(title, strategy.title)
            assertEquals(mapData, strategy.mapData)
            assertNotNull(strategy.team)
            assertEquals(team, strategy.team)
            assertEquals(createdBy, strategy.createdBy)
        }

        @Test
        @DisplayName("개인 전략 생성 성공 (팀 없음)")
        fun `should create personal strategy without team`() {
            // given
            val title = "개인 전략"
            val mapData = "{\"markers\": []}"
            val createdBy = 1L

            // when
            val strategy = Strategy(title = title, mapData = mapData, team = null, createdBy = createdBy)

            // then
            assertEquals(title, strategy.title)
            assertEquals(mapData, strategy.mapData)
            assertNull(strategy.team)
        }

        @Test
        @DisplayName("빈 mapData로 전략 생성")
        fun `should create strategy with empty map data`() {
            // given
            val title = "빈 전략"
            val mapData = "{}"
            val createdBy = 1L

            // when
            val strategy = Strategy(title = title, mapData = mapData, team = null, createdBy = createdBy)

            // then
            assertEquals(mapData, strategy.mapData)
        }
    }

    @Nested
    @DisplayName("Strategy 업데이트 테스트")
    inner class StrategyUpdateTest {

        @Test
        @DisplayName("updateData 메서드로 제목과 데이터 업데이트")
        fun `should update title and map data using updateData method`() {
            // given
            val strategy = Strategy(
                title = "기존 전략",
                mapData = "{\"old\": true}",
                team = null,
                createdBy = 1L
            )
            val newTitle = "새 전략"
            val newMapData = "{\"new\": true}"

            // when
            strategy.updateData(newTitle, newMapData)

            // then
            assertEquals(newTitle, strategy.title)
            assertEquals(newMapData, strategy.mapData)
        }

        @Test
        @DisplayName("제목만 업데이트")
        fun `should update only title`() {
            // given
            val originalMapData = "{\"markers\": []}"
            val strategy = Strategy(
                title = "기존 제목",
                mapData = originalMapData,
                team = null,
                createdBy = 1L
            )
            val newTitle = "새 제목"

            // when
            strategy.updateData(newTitle, originalMapData)

            // then
            assertEquals(newTitle, strategy.title)
            assertEquals(originalMapData, strategy.mapData)
        }

        @Test
        @DisplayName("mapData만 업데이트")
        fun `should update only map data`() {
            // given
            val originalTitle = "전략 제목"
            val strategy = Strategy(
                title = originalTitle,
                mapData = "{\"old\": true}",
                team = null,
                createdBy = 1L
            )
            val newMapData = "{\"new\": true, \"markers\": [{\"x\": 50}]}"

            // when
            strategy.updateData(originalTitle, newMapData)

            // then
            assertEquals(originalTitle, strategy.title)
            assertEquals(newMapData, strategy.mapData)
        }
    }

    @Nested
    @DisplayName("Strategy BaseTimeEntity 상속 테스트")
    inner class StrategyTimeEntityTest {

        @Test
        @DisplayName("생성 시 createdAt이 설정됨")
        fun `should have createdAt when created`() {
            // given & when
            val strategy = Strategy(
                title = "전략",
                mapData = "{}",
                team = null,
                createdBy = 1L
            )

            // then
            assertNotNull(strategy.createdAt)
        }

        @Test
        @DisplayName("생성 시 updatedAt이 설정됨")
        fun `should have updatedAt when created`() {
            // given & when
            val strategy = Strategy(
                title = "전략",
                mapData = "{}",
                team = null,
                createdBy = 1L
            )

            // then
            assertNotNull(strategy.updatedAt)
        }
    }
}
