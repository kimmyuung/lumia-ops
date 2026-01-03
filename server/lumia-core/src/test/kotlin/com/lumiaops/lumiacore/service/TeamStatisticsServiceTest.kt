package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import com.lumiaops.lumiacore.domain.scrim.Scrim
import com.lumiaops.lumiacore.repository.MatchResultRepository
import com.lumiaops.lumiacore.repository.TeamRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import java.util.*

@DisplayName("TeamStatisticsService 테스트")
class TeamStatisticsServiceTest {

    private lateinit var matchResultRepository: MatchResultRepository
    private lateinit var teamRepository: TeamRepository
    private lateinit var teamStatisticsService: TeamStatisticsService

    @BeforeEach
    fun setUp() {
        matchResultRepository = mockk()
        teamRepository = mockk()
        teamStatisticsService = TeamStatisticsService(matchResultRepository, teamRepository)
    }

    @Nested
    @DisplayName("팀 통계 조회")
    inner class GetTeamStats {

        @Test
        @DisplayName("정상적인 팀 통계 조회")
        fun getTeamStats_Success() {
            // given
            val team = createTeam(1L, "TestTeam")
            every { teamRepository.findById(1L) } returns Optional.of(team)
            every { matchResultRepository.countByTeam(team) } returns 10
            every { matchResultRepository.avgRankByTeam(team) } returns 3.5
            every { matchResultRepository.sumKillsByTeam(team) } returns 50
            every { matchResultRepository.sumScoreByTeam(team) } returns 150
            every { matchResultRepository.countWinsByTeam(team) } returns 3
            every { matchResultRepository.countTop3ByTeam(team) } returns 6

            // when
            val stats = teamStatisticsService.getTeamStats(1L)

            // then
            assertEquals(1L, stats.teamId)
            assertEquals("TestTeam", stats.teamName)
            assertEquals(10, stats.totalMatches)
            assertEquals(3.5, stats.averageRank)
            assertEquals(50, stats.totalKills)
            assertEquals(5.0, stats.averageKillsPerMatch)
            assertEquals(150, stats.totalScore)
            assertEquals(3, stats.winCount)
            assertEquals(6, stats.top3Count)
            assertEquals(30.0, stats.winRate) // 3/10 * 100
            assertEquals(60.0, stats.top3Rate) // 6/10 * 100
        }

        @Test
        @DisplayName("매치가 없는 팀 통계")
        fun getTeamStats_NoMatches() {
            // given
            val team = createTeam(1L, "NewTeam")
            every { teamRepository.findById(1L) } returns Optional.of(team)
            every { matchResultRepository.countByTeam(team) } returns 0

            // when
            val stats = teamStatisticsService.getTeamStats(1L)

            // then
            assertEquals(0, stats.totalMatches)
            assertEquals(0.0, stats.averageRank)
            assertEquals(0.0, stats.winRate)
        }

        @Test
        @DisplayName("존재하지 않는 팀")
        fun getTeamStats_TeamNotFound() {
            // given
            every { teamRepository.findById(999L) } returns Optional.empty()

            // when & then
            assertThrows(IllegalArgumentException::class.java) {
                teamStatisticsService.getTeamStats(999L)
            }
        }
    }

    @Nested
    @DisplayName("최근 성적 조회")
    inner class GetRecentPerformance {

        @Test
        @DisplayName("최근 경기 성적 조회")
        fun getRecentPerformance_Success() {
            // given
            val team = createTeam(1L, "TestTeam")
            val scrim = Scrim(title = "Test Scrim", startTime = LocalDateTime.now())
            val match = ScrimMatch(scrim = scrim, roundNumber = 1)
            
            // match의 id를 설정하기 위해 리플렉션 사용
            setFieldValue(match, "id", 1L)

            val results = listOf(
                createMatchResult(1L, match, team, 1, 5, 15),
                createMatchResult(2L, match, team, 2, 3, 9),
                createMatchResult(3L, match, team, 3, 2, 7)
            )

            every { teamRepository.findById(1L) } returns Optional.of(team)
            every { matchResultRepository.findRecentByTeam(team, any()) } returns results

            // when
            val performance = teamStatisticsService.getRecentPerformance(1L, 10)

            // then
            assertEquals(3, performance.matchCount)
            assertEquals(2.0, performance.averageRank)
            assertEquals(10, performance.totalKills)
            assertEquals(31, performance.totalScore)
            assertEquals(1, performance.winCount)
        }
    }

    @Nested
    @DisplayName("순위표 조회")
    inner class GetLeaderboard {

        @Test
        @DisplayName("순위표 조회 성공")
        fun getLeaderboard_Success() {
            // given
            val data = listOf(
                arrayOf<Any>(1L, "Team1", 10L, 2.5, 50L, 200L, 4L, 7L),
                arrayOf<Any>(2L, "Team2", 8L, 3.0, 40L, 150L, 2L, 5L)
            )
            every { matchResultRepository.getLeaderboardData() } returns data

            // when
            val leaderboard = teamStatisticsService.getLeaderboard()

            // then
            assertEquals(2, leaderboard.size)
            assertEquals(1, leaderboard[0].rank)
            assertEquals("Team1", leaderboard[0].teamName)
            assertEquals(200, leaderboard[0].totalScore)
            assertEquals(2, leaderboard[1].rank)
            assertEquals("Team2", leaderboard[1].teamName)
        }
    }

    // Helper methods
    private fun createTeam(id: Long, name: String): Team {
        val team = Team(name = name, description = null, ownerId = 1L)
        setFieldValue(team, "id", id)
        return team
    }

    private fun createMatchResult(
        id: Long,
        match: ScrimMatch,
        team: Team,
        rank: Int,
        killCount: Int,
        totalScore: Int
    ): MatchResult {
        val result = MatchResult(match = match, team = team, rank = rank, killCount = killCount, totalScore = totalScore)
        setFieldValue(result, "id", id)
        return result
    }

    private fun setFieldValue(obj: Any, fieldName: String, value: Any) {
        val field = obj::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(obj, value)
    }
}
