package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.scrim.Scrim
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ScrimMatchRepository : JpaRepository<ScrimMatch, Long> {
    fun findByScrim(scrim: Scrim): List<ScrimMatch>
    fun findByScrimOrderByRoundNumberAsc(scrim: Scrim): List<ScrimMatch>
    fun findByGameId(gameId: String): ScrimMatch?
}
