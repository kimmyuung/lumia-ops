package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MatchResultRepository : JpaRepository<MatchResult, Long> {
    fun findByMatch(match: ScrimMatch): List<MatchResult>
    fun findByMatchOrderByRankAsc(match: ScrimMatch): List<MatchResult>
    fun findByTeam(team: Team): List<MatchResult>

    /**
     * 팀의 최근 매치 결과 조회 (페이징)
     */
    @Query("SELECT m FROM MatchResult m WHERE m.team = :team ORDER BY m.id DESC")
    fun findRecentByTeam(@Param("team") team: Team, pageable: Pageable): List<MatchResult>

    /**
     * 팀의 총 매치 수
     */
    fun countByTeam(team: Team): Long

    /**
     * 팀의 1위 횟수 (승리 수)
     */
    @Query("SELECT COUNT(m) FROM MatchResult m WHERE m.team = :team AND m.rank = 1")
    fun countWinsByTeam(@Param("team") team: Team): Long

    /**
     * 팀의 상위 3위 횟수
     */
    @Query("SELECT COUNT(m) FROM MatchResult m WHERE m.team = :team AND m.rank <= 3")
    fun countTop3ByTeam(@Param("team") team: Team): Long

    /**
     * 팀의 총 킬 수
     */
    @Query("SELECT COALESCE(SUM(m.killCount), 0) FROM MatchResult m WHERE m.team = :team")
    fun sumKillsByTeam(@Param("team") team: Team): Long

    /**
     * 팀의 총 점수
     */
    @Query("SELECT COALESCE(SUM(m.totalScore), 0) FROM MatchResult m WHERE m.team = :team")
    fun sumScoreByTeam(@Param("team") team: Team): Long

    /**
     * 팀의 평균 순위
     */
    @Query("SELECT COALESCE(AVG(m.rank), 0) FROM MatchResult m WHERE m.team = :team")
    fun avgRankByTeam(@Param("team") team: Team): Double

    /**
     * 전체 팀 통계 (순위표용)
     */
    @Query("""
        SELECT m.team.id, m.team.name, 
               COUNT(m), AVG(m.rank), SUM(m.killCount), SUM(m.totalScore),
               SUM(CASE WHEN m.rank = 1 THEN 1 ELSE 0 END),
               SUM(CASE WHEN m.rank <= 3 THEN 1 ELSE 0 END)
        FROM MatchResult m 
        GROUP BY m.team.id, m.team.name
        ORDER BY SUM(m.totalScore) DESC
    """)
    fun getLeaderboardData(): List<Array<Any>>
}
