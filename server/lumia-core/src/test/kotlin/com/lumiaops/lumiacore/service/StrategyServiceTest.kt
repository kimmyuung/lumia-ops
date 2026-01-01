package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.repository.StrategyRepository
import com.lumiaops.lumiacore.domain.strategy.Strategy
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DisplayName("StrategyService 단위 테스트")
class StrategyServiceTest {

    @MockK
    private lateinit var strategyRepository: StrategyRepository

    @InjectMockKs
    private lateinit var strategyService: StrategyService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    @DisplayName("Strategy CRUD 테스트")
    inner class StrategyCrudTest {

        @Test
        @DisplayName("전략 ID로 조회 성공")
        fun `should return strategy when strategy exists`() {
            // given
            val strategyId = 1L
            val strategy = mockk<Strategy> {
                every { id } returns strategyId
                every { title } returns "테스트 전략"
            }
            every { strategyRepository.findById(strategyId) } returns Optional.of(strategy)

            // when
            val result = strategyService.findById(strategyId)

            // then
            assertNotNull(result)
            assertEquals(strategyId, result.id)
        }

        @Test
        @DisplayName("존재하지 않는 전략 ID로 조회 시 null 반환")
        fun `should return null when strategy does not exist`() {
            // given
            val strategyId = 999L
            every { strategyRepository.findById(strategyId) } returns Optional.empty()

            // when
            val result = strategyService.findById(strategyId)

            // then
            assertNull(result)
        }

        @Test
        @DisplayName("모든 전략 조회")
        fun `should return all strategies`() {
            // given
            val strategies = listOf(
                mockk<Strategy> { every { title } returns "전략1" },
                mockk<Strategy> { every { title } returns "전략2" }
            )
            every { strategyRepository.findAll() } returns strategies

            // when
            val result = strategyService.findAll()

            // then
            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("전략 생성 성공")
        fun `should create strategy successfully`() {
            // given
            val title = "새 전략"
            val mapData = "{\"markers\": []}"
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val createdBy = 1L
            val savedStrategy = Strategy(title = title, mapData = mapData, team = team, createdBy = createdBy)
            
            every { strategyRepository.save(any<Strategy>()) } returns savedStrategy

            // when
            val result = strategyService.createStrategy(title, mapData, team, createdBy)

            // then
            assertEquals(title, result.title)
            assertEquals(mapData, result.mapData)
            assertEquals(team, result.team)
            assertEquals(createdBy, result.createdBy)
            verify(exactly = 1) { strategyRepository.save(any<Strategy>()) }
        }

        @Test
        @DisplayName("팀 없이 개인 전략 생성 성공")
        fun `should create personal strategy without team`() {
            // given
            val title = "개인 전략"
            val mapData = "{\"markers\": []}"
            val createdBy = 1L
            val savedStrategy = Strategy(title = title, mapData = mapData, team = null, createdBy = createdBy)
            
            every { strategyRepository.save(any<Strategy>()) } returns savedStrategy

            // when
            val result = strategyService.createStrategy(title, mapData, null, createdBy)

            // then
            assertEquals(title, result.title)
            assertNull(result.team)
        }

        @Test
        @DisplayName("전략 업데이트 성공")
        fun `should update strategy successfully`() {
            // given
            val strategyId = 1L
            val strategy = Strategy(
                title = "기존 전략",
                mapData = "{\"old\": true}",
                team = null,
                createdBy = 1L
            )
            val newTitle = "수정된 전략"
            val newMapData = "{\"new\": true}"
            
            every { strategyRepository.findById(strategyId) } returns Optional.of(strategy)

            // when
            val result = strategyService.updateStrategy(strategyId, newTitle, newMapData)

            // then
            assertEquals(newTitle, result.title)
            assertEquals(newMapData, result.mapData)
        }

        @Test
        @DisplayName("존재하지 않는 전략 업데이트 시 예외 발생")
        fun `should throw exception when updating non-existent strategy`() {
            // given
            val strategyId = 999L
            every { strategyRepository.findById(strategyId) } returns Optional.empty()

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                strategyService.updateStrategy(strategyId, "제목", "데이터")
            }
            assertEquals("전략을 찾을 수 없습니다: $strategyId", exception.message)
        }

        @Test
        @DisplayName("전략 삭제 성공")
        fun `should delete strategy successfully`() {
            // given
            val strategyId = 1L
            every { strategyRepository.deleteById(strategyId) } just Runs

            // when
            strategyService.deleteStrategy(strategyId)

            // then
            verify(exactly = 1) { strategyRepository.deleteById(strategyId) }
        }
    }

    @Nested
    @DisplayName("Strategy 조회 테스트")
    inner class StrategyQueryTest {

        @Test
        @DisplayName("팀별 전략 조회")
        fun `should find strategies by team`() {
            // given
            val team = Team(name = "테스트팀", description = null, ownerId = 1L)
            val strategies = listOf(
                mockk<Strategy> { every { title } returns "팀 전략1" },
                mockk<Strategy> { every { title } returns "팀 전략2" }
            )
            every { strategyRepository.findByTeam(team) } returns strategies

            // when
            val result = strategyService.findByTeam(team)

            // then
            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("작성자별 전략 조회")
        fun `should find strategies by created by`() {
            // given
            val createdBy = 1L
            val strategies = listOf(
                mockk<Strategy> { every { title } returns "내 전략" }
            )
            every { strategyRepository.findByCreatedBy(createdBy) } returns strategies

            // when
            val result = strategyService.findByCreatedBy(createdBy)

            // then
            assertEquals(1, result.size)
        }

        @Test
        @DisplayName("제목으로 전략 검색")
        fun `should search strategies by title`() {
            // given
            val keyword = "공격"
            val strategies = listOf(
                mockk<Strategy> { every { title } returns "공격 전략" }
            )
            every { strategyRepository.findByTitleContainingIgnoreCase(keyword) } returns strategies

            // when
            val result = strategyService.searchByTitle(keyword)

            // then
            assertEquals(1, result.size)
        }
    }
}
