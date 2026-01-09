package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.dto.CalculateScoreRequest
import com.lumiaops.lumiacore.service.TeamStatisticsService
import com.lumiaops.lumiacore.service.TeamStats
import com.lumiaops.lumiacore.service.RecentPerformance
import com.lumiaops.lumiacore.service.LeaderboardEntry
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("StatisticsController 통합 테스트")
class StatisticsControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var teamStatisticsService: TeamStatisticsService

    private lateinit var testTeamStats: TeamStats
    private lateinit var testRecentPerformance: RecentPerformance
    private lateinit var testLeaderboardEntry: LeaderboardEntry

    @BeforeEach
    fun setUp() {
        testTeamStats = TeamStats(
            teamId = 1L,
            teamName = "Test Team",
            totalScrims = 10,
            averageRank = 2.5,
            averageKills = 5.2,
            totalScore = 1500
        )

        testRecentPerformance = RecentPerformance(
            teamId = 1L,
            teamName = "Test Team",
            games = emptyList()
        )

        testLeaderboardEntry = LeaderboardEntry(
            rank = 1,
            teamId = 1L,
            teamName = "Test Team",
            totalScore = 1500,
            gamesPlayed = 10
        )
    }

    @Nested
    @DisplayName("GET /statistics/teams/{id}")
    inner class GetTeamStats {

        @Test
        @WithMockUser
        fun `팀 통계 조회 성공`() {
            `when`(teamStatisticsService.getTeamStats(1L)).thenReturn(testTeamStats)

            mockMvc.perform(get("/statistics/teams/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.teamName").value("Test Team"))
                .andExpect(jsonPath("$.totalScrims").value(10))
        }

        @Test
        @WithMockUser
        fun `존재하지 않는 팀 조회시 404 반환`() {
            `when`(teamStatisticsService.getTeamStats(999L))
                .thenThrow(IllegalArgumentException("Team not found"))

            mockMvc.perform(get("/statistics/teams/999"))
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("GET /statistics/teams/{id}/recent")
    inner class GetRecentPerformance {

        @Test
        @WithMockUser
        fun `최근 성적 조회 성공`() {
            `when`(teamStatisticsService.getRecentPerformance(1L, 10)).thenReturn(testRecentPerformance)

            mockMvc.perform(get("/statistics/teams/1/recent")
                .param("count", "10"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.teamName").value("Test Team"))
        }

        @Test
        @WithMockUser
        fun `기본값으로 최근 10개 조회`() {
            `when`(teamStatisticsService.getRecentPerformance(1L, 10)).thenReturn(testRecentPerformance)

            mockMvc.perform(get("/statistics/teams/1/recent"))
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("GET /statistics/leaderboard")
    inner class GetLeaderboard {

        @Test
        @WithMockUser
        fun `순위표 조회 성공`() {
            `when`(teamStatisticsService.getLeaderboard()).thenReturn(listOf(testLeaderboardEntry))

            mockMvc.perform(get("/statistics/leaderboard"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.teams[0].rank").value(1))
                .andExpect(jsonPath("$.teams[0].teamName").value("Test Team"))
        }
    }

    @Nested
    @DisplayName("POST /statistics/calculate-score")
    inner class CalculateScore {

        @Test
        @WithMockUser
        fun `점수 계산 성공`() {
            val request = CalculateScoreRequest(rank = 1, kills = 5, killMultiplier = 1)

            mockMvc.perform(post("/statistics/calculate-score")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.placementPoints").exists())
                .andExpect(jsonPath("$.killPoints").value(5))
                .andExpect(jsonPath("$.totalScore").exists())
        }
    }

    @Nested
    @DisplayName("GET /statistics/placement-points")
    inner class GetPlacementPoints {

        @Test
        @WithMockUser
        fun `순위별 점수 테이블 조회 성공`() {
            mockMvc.perform(get("/statistics/placement-points"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.1").exists()) // 1등 점수 존재
        }
    }
}
