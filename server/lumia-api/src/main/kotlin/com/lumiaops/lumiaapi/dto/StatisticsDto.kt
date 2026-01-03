package com.lumiaops.lumiaapi.dto

import com.lumiaops.lumiacore.service.*

/**
 * 팀 통계 응답 DTO
 */
data class TeamStatsResponse(
    val teamId: Long,
    val teamName: String,
    val totalMatches: Int,
    val averageRank: Double,
    val totalKills: Int,
    val averageKillsPerMatch: Double,
    val totalScore: Int,
    val winCount: Int,
    val top3Count: Int,
    val winRate: Double,
    val top3Rate: Double
) {
    companion object {
        fun from(stats: TeamStats) = TeamStatsResponse(
            teamId = stats.teamId,
            teamName = stats.teamName,
            totalMatches = stats.totalMatches,
            averageRank = stats.averageRank,
            totalKills = stats.totalKills,
            averageKillsPerMatch = stats.averageKillsPerMatch,
            totalScore = stats.totalScore,
            winCount = stats.winCount,
            top3Count = stats.top3Count,
            winRate = stats.winRate,
            top3Rate = stats.top3Rate
        )
    }
}

/**
 * 최근 성적 응답 DTO
 */
data class RecentPerformanceResponse(
    val teamId: Long,
    val teamName: String,
    val matchCount: Int,
    val matches: List<MatchSummaryResponse>,
    val averageRank: Double,
    val totalKills: Int,
    val totalScore: Int,
    val winCount: Int,
    val trend: String
) {
    companion object {
        fun from(performance: RecentPerformance) = RecentPerformanceResponse(
            teamId = performance.teamId,
            teamName = performance.teamName,
            matchCount = performance.matchCount,
            matches = performance.matches.map { MatchSummaryResponse.from(it) },
            averageRank = performance.averageRank,
            totalKills = performance.totalKills,
            totalScore = performance.totalScore,
            winCount = performance.winCount,
            trend = performance.trend.name
        )
    }
}

data class MatchSummaryResponse(
    val matchId: Long,
    val rank: Int,
    val kills: Int,
    val score: Int
) {
    companion object {
        fun from(summary: MatchSummary) = MatchSummaryResponse(
            matchId = summary.matchId,
            rank = summary.rank,
            kills = summary.kills,
            score = summary.score
        )
    }
}

/**
 * 순위표 응답 DTO
 */
data class LeaderboardResponse(
    val teams: List<LeaderboardEntryResponse>
)

data class LeaderboardEntryResponse(
    val rank: Int,
    val teamId: Long,
    val teamName: String,
    val totalMatches: Int,
    val averageRank: Double,
    val totalKills: Int,
    val totalScore: Int,
    val winCount: Int,
    val top3Count: Int
) {
    companion object {
        fun from(entry: LeaderboardEntry) = LeaderboardEntryResponse(
            rank = entry.rank,
            teamId = entry.teamId,
            teamName = entry.teamName,
            totalMatches = entry.totalMatches,
            averageRank = entry.averageRank,
            totalKills = entry.totalKills,
            totalScore = entry.totalScore,
            winCount = entry.winCount,
            top3Count = entry.top3Count
        )
    }
}

/**
 * 점수 계산 요청 DTO
 */
data class CalculateScoreRequest(
    val rank: Int,
    val kills: Int,
    val killMultiplier: Int = 1
)

/**
 * 점수 계산 응답 DTO
 */
data class CalculateScoreResponse(
    val placementPoints: Int,
    val killPoints: Int,
    val totalScore: Int
)
