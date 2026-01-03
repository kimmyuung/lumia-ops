package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.repository.MatchResultRepository
import com.lumiaops.lumiacore.repository.TeamRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 팀 통계 집계 서비스
 */
@Service
@Transactional(readOnly = true)
class TeamStatisticsService(
    private val matchResultRepository: MatchResultRepository,
    private val teamRepository: TeamRepository
) {

    /**
     * 팀 종합 통계 조회
     */
    fun getTeamStats(teamId: Long): TeamStats {
        val team = teamRepository.findById(teamId).orElse(null)
            ?: throw IllegalArgumentException("팀을 찾을 수 없습니다: $teamId")
        
        return calculateTeamStats(team)
    }

    /**
     * 팀 통계 계산
     */
    private fun calculateTeamStats(team: Team): TeamStats {
        val totalMatches = matchResultRepository.countByTeam(team)
        
        if (totalMatches == 0L) {
            return TeamStats(
                teamId = team.id!!,
                teamName = team.name,
                totalMatches = 0,
                averageRank = 0.0,
                totalKills = 0,
                averageKillsPerMatch = 0.0,
                totalScore = 0,
                winCount = 0,
                top3Count = 0,
                winRate = 0.0,
                top3Rate = 0.0
            )
        }

        val avgRank = matchResultRepository.avgRankByTeam(team)
        val totalKills = matchResultRepository.sumKillsByTeam(team)
        val totalScore = matchResultRepository.sumScoreByTeam(team)
        val winCount = matchResultRepository.countWinsByTeam(team)
        val top3Count = matchResultRepository.countTop3ByTeam(team)

        return TeamStats(
            teamId = team.id!!,
            teamName = team.name,
            totalMatches = totalMatches.toInt(),
            averageRank = avgRank,
            totalKills = totalKills.toInt(),
            averageKillsPerMatch = totalKills.toDouble() / totalMatches,
            totalScore = totalScore.toInt(),
            winCount = winCount.toInt(),
            top3Count = top3Count.toInt(),
            winRate = (winCount.toDouble() / totalMatches) * 100,
            top3Rate = (top3Count.toDouble() / totalMatches) * 100
        )
    }

    /**
     * 최근 N경기 성적 조회
     */
    fun getRecentPerformance(teamId: Long, count: Int = 10): RecentPerformance {
        val team = teamRepository.findById(teamId).orElse(null)
            ?: throw IllegalArgumentException("팀을 찾을 수 없습니다: $teamId")

        val recentResults = matchResultRepository.findRecentByTeam(team, PageRequest.of(0, count))
        
        if (recentResults.isEmpty()) {
            return RecentPerformance(
                teamId = team.id!!,
                teamName = team.name,
                matchCount = 0,
                matches = emptyList(),
                averageRank = 0.0,
                totalKills = 0,
                totalScore = 0,
                winCount = 0,
                trend = PerformanceTrend.STABLE
            )
        }

        val avgRank = recentResults.map { it.rank }.average()
        val totalKills = recentResults.sumOf { it.killCount }
        val totalScore = recentResults.sumOf { it.totalScore }
        val winCount = recentResults.count { it.rank == 1 }

        // 트렌드 분석 (최근 절반 vs 이전 절반 비교)
        val trend = analyzeTrend(recentResults)

        return RecentPerformance(
            teamId = team.id!!,
            teamName = team.name,
            matchCount = recentResults.size,
            matches = recentResults.map { 
                MatchSummary(
                    matchId = it.match.id!!,
                    rank = it.rank,
                    kills = it.killCount,
                    score = it.totalScore
                )
            },
            averageRank = avgRank,
            totalKills = totalKills,
            totalScore = totalScore,
            winCount = winCount,
            trend = trend
        )
    }

    /**
     * 성적 트렌드 분석
     */
    private fun analyzeTrend(results: List<MatchResult>): PerformanceTrend {
        if (results.size < 4) return PerformanceTrend.STABLE

        val half = results.size / 2
        val recentHalf = results.take(half)
        val olderHalf = results.drop(half).take(half)

        val recentAvgScore = recentHalf.map { it.totalScore }.average()
        val olderAvgScore = olderHalf.map { it.totalScore }.average()

        val scoreDiff = recentAvgScore - olderAvgScore
        val threshold = olderAvgScore * 0.1 // 10% 변화 기준

        return when {
            scoreDiff > threshold -> PerformanceTrend.IMPROVING
            scoreDiff < -threshold -> PerformanceTrend.DECLINING
            else -> PerformanceTrend.STABLE
        }
    }

    /**
     * 전체 팀 순위표 조회
     */
    fun getLeaderboard(): List<LeaderboardEntry> {
        val data = matchResultRepository.getLeaderboardData()
        
        return data.mapIndexed { index, row ->
            LeaderboardEntry(
                rank = index + 1,
                teamId = (row[0] as Number).toLong(),
                teamName = row[1] as String,
                totalMatches = (row[2] as Number).toInt(),
                averageRank = (row[3] as Number).toDouble(),
                totalKills = (row[4] as Number).toInt(),
                totalScore = (row[5] as Number).toInt(),
                winCount = (row[6] as Number).toInt(),
                top3Count = (row[7] as Number).toInt()
            )
        }
    }
}

// ==================== 데이터 클래스 ====================

data class TeamStats(
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
)

data class RecentPerformance(
    val teamId: Long,
    val teamName: String,
    val matchCount: Int,
    val matches: List<MatchSummary>,
    val averageRank: Double,
    val totalKills: Int,
    val totalScore: Int,
    val winCount: Int,
    val trend: PerformanceTrend
)

data class MatchSummary(
    val matchId: Long,
    val rank: Int,
    val kills: Int,
    val score: Int
)

data class LeaderboardEntry(
    val rank: Int,
    val teamId: Long,
    val teamName: String,
    val totalMatches: Int,
    val averageRank: Double,
    val totalKills: Int,
    val totalScore: Int,
    val winCount: Int,
    val top3Count: Int
)

enum class PerformanceTrend {
    IMPROVING,  // 상승세
    STABLE,     // 안정
    DECLINING   // 하락세
}
