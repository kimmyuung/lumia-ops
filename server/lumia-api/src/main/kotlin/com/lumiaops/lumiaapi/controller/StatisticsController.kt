package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.service.TeamStatisticsService
import com.lumiaops.lumiacore.util.ScoreCalculator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 통계 API 컨트롤러
 */
@Tag(name = "통계", description = "팀 통계 및 리더보드 API")
@RestController
@RequestMapping("/statistics")
class StatisticsController(
    private val teamStatisticsService: TeamStatisticsService
) {

    @Operation(summary = "팀 종합 통계 조회", description = "특정 팀의 종합 통계를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    )
    @GetMapping("/teams/{id}")
    fun getTeamStats(@Parameter(description = "팀 ID") @PathVariable id: Long): ResponseEntity<TeamStatsResponse> {
        return try {
            val stats = teamStatisticsService.getTeamStats(id)
            ResponseEntity.ok(TeamStatsResponse.from(stats))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "팀 최근 성적 조회", description = "팀의 최근 N개 스크림 성적을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    )
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

    @Operation(summary = "전체 팀 순위표 조회", description = "전체 팀 순위표를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/leaderboard")
    fun getLeaderboard(): ResponseEntity<LeaderboardResponse> {
        val leaderboard = teamStatisticsService.getLeaderboard()
        val response = LeaderboardResponse(
            teams = leaderboard.map { LeaderboardEntryResponse.from(it) }
        )
        return ResponseEntity.ok(response)
    }

    @Operation(summary = "점수 계산", description = "순위와 킬 수로 점수를 계산합니다.")
    @ApiResponse(responseCode = "200", description = "계산 성공")
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

    @Operation(summary = "순위별 점수 테이블 조회", description = "순위별 배치 점수 테이블을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/placement-points")
    fun getPlacementPoints(): ResponseEntity<Map<Int, Int>> {
        return ResponseEntity.ok(ScoreCalculator.getPlacementPointsTable())
    }
}
