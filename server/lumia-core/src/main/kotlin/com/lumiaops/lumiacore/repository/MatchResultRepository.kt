package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MatchResultRepository : JpaRepository<MatchResult, Long> {
    fun findByMatch(match: ScrimMatch): List<MatchResult>
    fun findByMatchOrderByRankAsc(match: ScrimMatch): List<MatchResult>
    fun findByTeam(team: Team): List<MatchResult>
}
