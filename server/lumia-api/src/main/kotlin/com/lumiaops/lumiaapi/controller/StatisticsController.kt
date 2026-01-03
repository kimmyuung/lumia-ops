package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.service.TeamStatisticsService
import com.lumiaops.lumiacore.util.ScoreCalculator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 통계 API 컨트롤러
 */
@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val teamStatisticsService: TeamStatisticsService
) {

    /**
     * 팀 종합 통계 조회
     * GET /api/statistics/teams/{id}
     */
    @GetMapping("/teams/{id}")
    fun getTeamStats(@PathVariable id: Long): ResponseEntity<TeamStatsResponse> {
        return try {
            val stats = teamStatisticsService.getTeamStats(id)
            ResponseEntity.ok(TeamStatsResponse.from(stats))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 팀 최근 성적 조회
     * GET /api/statistics/teams/{id}/recent?count=10
     */
    @GetMapping("/teams/{id}/recent")
    fun getRecentPerformance(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "10") count: Int
    ): ResponseEntity<RecentPerformanceResponse> {
        return try {
            val performance = teamStatisticsService.getRecentPerformance(id, count)
            ResponseEntity.ok(RecentPerformanceResponse.from(performance))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * 전체 팀 순위표 조회
     * GET /api/statistics/leaderboard
     */
    @GetMapping("/leaderboard")
    fun getLeaderboard(): ResponseEntity<LeaderboardResponse> {
        val leaderboard = teamStatisticsService.getLeaderboard()
        val response = LeaderboardResponse(
            teams = leaderboard.map { LeaderboardEntryResponse.from(it) }
        )
        return ResponseEntity.ok(response)
    }

    /**
     * 점수 계산 (유틸리티)
     * POST /api/statistics/calculate-score
     */
    @PostMapping("/calculate-score")
    fun calculateScore(@RequestBody request: CalculateScoreRequest): ResponseEntity<CalculateScoreResponse> {
        val placementPoints = ScoreCalculator.getPlacementPoints(request.rank)
        val killPoints = request.kills * request.killMultiplier
        val totalScore = ScoreCalculator.calculateScore(request.rank, request.kills, request.killMultiplier)

        return ResponseEntity.ok(CalculateScoreResponse(
            placementPoints = placementPoints,
            killPoints = killPoints,
            totalScore = totalScore
        ))
    }

    /**
     * 순위별 점수 테이블 조회
     * GET /api/statistics/placement-points
     */
    @GetMapping("/placement-points")
    fun getPlacementPoints(): ResponseEntity<Map<Int, Int>> {
        return ResponseEntity.ok(ScoreCalculator.getPlacementPointsTable())
    }
}
