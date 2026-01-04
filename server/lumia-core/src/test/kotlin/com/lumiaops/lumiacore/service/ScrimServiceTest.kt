package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.domain.scrim.Scrim
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import com.lumiaops.lumiacore.domain.scrim.ScrimStatus
import com.lumiaops.lumiacore.exception.NotFoundException
import com.lumiaops.lumiacore.repository.MatchResultRepository
import com.lumiaops.lumiacore.repository.ScrimMatchRepository
import com.lumiaops.lumiacore.repository.ScrimRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("ScrimService 단위 테스트")
class ScrimServiceTest {

    @MockK
    private lateinit var scrimRepository: ScrimRepository

    @MockK
    private lateinit var scrimMatchRepository: ScrimMatchRepository

    @MockK
    private lateinit var matchResultRepository: MatchResultRepository

    @InjectMockKs
    private lateinit var scrimService: ScrimService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    @DisplayName("Scrim CRUD 테스트")
    inner class ScrimCrudTest {

        @Test
        @DisplayName("스크림 ID로 조회 성공")
        fun `should return scrim when scrim exists`() {
            // given
            val scrimId = 1L
            val scrim = mockk<Scrim> {
                every { id } returns scrimId
                every { title } returns "테스트 스크림"
            }
            every { scrimRepository.findById(scrimId) } returns Optional.of(scrim)

            // when
            val result = scrimService.findScrimById(scrimId)

            // then
            assertNotNull(result)
            assertEquals(scrimId, result.id)
        }

        @Test
        @DisplayName("존재하지 않는 스크림 ID로 조회 시 null 반환")
        fun `should return null when scrim does not exist`() {
            // given
            val scrimId = 999L
            every { scrimRepository.findById(scrimId) } returns Optional.empty()

            // when
            val result = scrimService.findScrimById(scrimId)

            // then
            assertNull(result)
        }

        @Test
        @DisplayName("스크림 생성 성공")
        fun `should create scrim successfully`() {
            // given
            val title = "새 스크림"
            val startTime = LocalDateTime.now().plusDays(1)
            val savedScrim = Scrim(title = title, startTime = startTime)
            
            every { scrimRepository.save(any<Scrim>()) } returns savedScrim

            // when
            val result = scrimService.createScrim(title, startTime)

            // then
            assertEquals(title, result.title)
            assertEquals(startTime, result.startTime)
            verify(exactly = 1) { scrimRepository.save(any<Scrim>()) }
        }

        @Test
        @DisplayName("진행 중인 스크림 목록 조회")
        fun `should return active scrims`() {
            // given
            val activeScrims = listOf(
                Scrim(title = "스크림1", startTime = LocalDateTime.now()),
                Scrim(title = "스크림2", startTime = LocalDateTime.now())
            )
            every { scrimRepository.findByStatusIn(listOf(ScrimStatus.SCHEDULED, ScrimStatus.IN_PROGRESS)) } returns activeScrims

            // when
            val result = scrimService.findActiveScrims()

            // then
            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("완료된 스크림 목록 조회")
        fun `should return finished scrims`() {
            // given
            val finishedScrim = Scrim(title = "완료스크림", startTime = LocalDateTime.now())
            finishedScrim.finish()
            val finishedScrims = listOf(finishedScrim)
            every { scrimRepository.findByStatus(ScrimStatus.FINISHED) } returns finishedScrims

            // when
            val result = scrimService.findFinishedScrims()

            // then
            assertEquals(1, result.size)
        }

        @Test
        @DisplayName("스크림 완료 처리")
        fun `should finish scrim successfully`() {
            // given
            val scrimId = 1L
            val scrim = Scrim(title = "스크림", startTime = LocalDateTime.now())
            every { scrimRepository.findById(scrimId) } returns Optional.of(scrim)

            // when
            val result = scrimService.finishScrim(scrimId)

            // then
            assertTrue(result.isFinished)
            assertEquals(ScrimStatus.FINISHED, result.status)
        }

        @Test
        @DisplayName("존재하지 않는 스크림 완료 처리 시 예외 발생")
        fun `should throw exception when finishing non-existent scrim`() {
            // given
            val scrimId = 999L
            every { scrimRepository.findById(scrimId) } returns Optional.empty()

            // when & then
            assertThrows<NotFoundException> {
                scrimService.finishScrim(scrimId)
            }
        }

        @Test
        @DisplayName("제목으로 스크림 검색")
        fun `should search scrims by title`() {
            // given
            val keyword = "대회"
            val scrims = listOf(
                Scrim(title = "대회 스크림", startTime = LocalDateTime.now())
            )
            every { scrimRepository.findByTitleContainingIgnoreCase(keyword) } returns scrims

            // when
            val result = scrimService.searchScrimsByTitle(keyword)

            // then
            assertEquals(1, result.size)
        }

        @Test
        @DisplayName("날짜 범위로 스크림 조회")
        fun `should find scrims by date range`() {
            // given
            val start = LocalDateTime.now()
            val end = LocalDateTime.now().plusDays(7)
            val scrims = listOf(
                Scrim(title = "이번주 스크림", startTime = start.plusDays(1))
            )
            every { scrimRepository.findByStartTimeBetween(start, end) } returns scrims

            // when
            val result = scrimService.findScrimsByDateRange(start, end)

            // then
            assertEquals(1, result.size)
        }
    }

    @Nested
    @DisplayName("ScrimMatch 테스트")
    inner class ScrimMatchTest {
        
        private lateinit var scrim: Scrim

        @BeforeEach
        fun setUpScrim() {
            scrim = Scrim(title = "테스트 스크림", startTime = LocalDateTime.now())
        }

        @Test
        @DisplayName("스크림의 매치 목록 조회")
        fun `should find matches by scrim`() {
            // given
            val matches = listOf(
                ScrimMatch(scrim = scrim, roundNumber = 1),
                ScrimMatch(scrim = scrim, roundNumber = 2)
            )
            every { scrimMatchRepository.findByScrimOrderByRoundNumberAsc(scrim) } returns matches

            // when
            val result = scrimService.findMatchesByScrim(scrim)

            // then
            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("게임 ID로 매치 조회")
        fun `should find match by game id`() {
            // given
            val gameId = "game-123"
            val match = ScrimMatch(scrim = scrim, roundNumber = 1, gameId = gameId)
            every { scrimMatchRepository.findByGameId(gameId) } returns match

            // when
            val result = scrimService.findMatchByGameId(gameId)

            // then
            assertNotNull(result)
            assertEquals(gameId, result.gameId)
        }

        @Test
        @DisplayName("매치 게임 ID 업데이트")
        fun `should update game id successfully`() {
            // given
            val matchId = 1L
            val match = ScrimMatch(scrim = scrim, roundNumber = 1, gameId = null)
            val newGameId = "new-game-id"
            
            every { scrimMatchRepository.findById(matchId) } returns Optional.of(match)

            // when
            val result = scrimService.updateGameId(matchId, newGameId)

            // then
            assertEquals(newGameId, result.gameId)
        }

        @Test
        @DisplayName("존재하지 않는 매치 게임 ID 업데이트 시 예외 발생")
        fun `should throw exception when updating non-existent match`() {
            // given
            val matchId = 999L
            every { scrimMatchRepository.findById(matchId) } returns Optional.empty()

            // when & then
            assertThrows<NotFoundException> {
                scrimService.updateGameId(matchId, "game-id")
            }
        }
    }

    @Nested
    @DisplayName("MatchResult 테스트")
    inner class MatchResultTest {
        
        private lateinit var scrim: Scrim
        private lateinit var match: ScrimMatch
        private lateinit var team: Team

        @BeforeEach
        fun setUpMatchResult() {
            scrim = Scrim(title = "테스트 스크림", startTime = LocalDateTime.now())
            match = ScrimMatch(scrim = scrim, roundNumber = 1)
            team = Team(name = "테스트팀", description = null, ownerId = 1L)
        }

        @Test
        @DisplayName("매치의 결과 목록 조회")
        fun `should find results by match`() {
            // given
            val results = listOf(
                MatchResult(match = match, team = team, rank = 1, killCount = 10, totalScore = 100)
            )
            every { matchResultRepository.findByMatchOrderByRankAsc(match) } returns results

            // when
            val result = scrimService.findResultsByMatch(match)

            // then
            assertEquals(1, result.size)
        }

        @Test
        @DisplayName("팀의 결과 목록 조회")
        fun `should find results by team`() {
            // given
            val results = listOf(
                MatchResult(match = match, team = team, rank = 1, killCount = 10, totalScore = 100)
            )
            every { matchResultRepository.findByTeam(team) } returns results

            // when
            val result = scrimService.findResultsByTeam(team)

            // then
            assertEquals(1, result.size)
        }

        @Test
        @DisplayName("매치 결과 업데이트 성공")
        fun `should update match result successfully`() {
            // given
            val resultId = 1L
            val existingResult = MatchResult(match = match, team = team, rank = 2, killCount = 5, totalScore = 50)
            val newRank = 1
            val newKillCount = 10
            val newTotalScore = 100
            
            every { matchResultRepository.findById(resultId) } returns Optional.of(existingResult)

            // when
            val result = scrimService.updateMatchResult(resultId, newRank, newKillCount, newTotalScore)

            // then
            assertEquals(newRank, result.rank)
            assertEquals(newKillCount, result.killCount)
            assertEquals(newTotalScore, result.totalScore)
        }

        @Test
        @DisplayName("존재하지 않는 매치 결과 업데이트 시 예외 발생")
        fun `should throw exception when updating non-existent result`() {
            // given
            val resultId = 999L
            every { matchResultRepository.findById(resultId) } returns Optional.empty()

            // when & then
            assertThrows<NotFoundException> {
                scrimService.updateMatchResult(resultId, 1, 10, 100)
            }
        }
    }
}
