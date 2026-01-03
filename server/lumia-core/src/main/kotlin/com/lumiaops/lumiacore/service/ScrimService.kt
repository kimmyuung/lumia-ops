package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.domain.scrim.Scrim
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import com.lumiaops.lumiacore.repository.ScrimRepository
import com.lumiaops.lumiacore.repository.MatchResultRepository
import com.lumiaops.lumiacore.repository.ScrimMatchRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ScrimService(
    private val scrimRepository: ScrimRepository,
    private val scrimMatchRepository: ScrimMatchRepository,
    private val matchResultRepository: MatchResultRepository
) {
    // Scrim 관련 메서드
    fun findScrimById(id: Long): Scrim? = scrimRepository.findById(id).orElse(null)

    fun findAllScrims(): List<Scrim> = scrimRepository.findAll()

    fun findActiveScrims(): List<Scrim> = scrimRepository.findByIsFinished(false)

    fun findFinishedScrims(): List<Scrim> = scrimRepository.findByIsFinished(true)

    fun findScrimsByDateRange(start: LocalDateTime, end: LocalDateTime): List<Scrim> =
        scrimRepository.findByStartTimeBetween(start, end)

    fun searchScrimsByTitle(title: String): List<Scrim> =
        scrimRepository.findByTitleContainingIgnoreCase(title)

    @Transactional
    fun createScrim(title: String, startTime: LocalDateTime): Scrim {
        return scrimRepository.save(Scrim(title = title, startTime = startTime))
    }

    @Transactional
    fun updateScrim(scrimId: Long, title: String?, startTime: LocalDateTime?): Scrim {
        val scrim = findScrimById(scrimId) ?: throw IllegalArgumentException("스크림을 찾을 수 없습니다: $scrimId")
        title?.let { scrim.title = it }
        startTime?.let { scrim.startTime = it }
        return scrim
    }

    @Transactional
    fun finishScrim(scrimId: Long): Scrim {
        val scrim = findScrimById(scrimId) ?: throw IllegalArgumentException("스크림을 찾을 수 없습니다: $scrimId")
        scrim.isFinished = true
        return scrim
    }

    @Transactional
    fun deleteScrim(scrimId: Long) {
        scrimRepository.deleteById(scrimId)
    }

    // ScrimMatch 관련 메서드
    fun findMatchById(id: Long): ScrimMatch? = scrimMatchRepository.findById(id).orElse(null)

    fun findMatchesByScrim(scrim: Scrim): List<ScrimMatch> =
        scrimMatchRepository.findByScrimOrderByRoundNumberAsc(scrim)

    fun findMatchByGameId(gameId: String): ScrimMatch? = scrimMatchRepository.findByGameId(gameId)

    @Transactional
    fun addMatch(scrim: Scrim, roundNumber: Int, gameId: String? = null): ScrimMatch {
        return scrimMatchRepository.save(ScrimMatch(scrim = scrim, roundNumber = roundNumber, gameId = gameId))
    }

    @Transactional
    fun updateGameId(matchId: Long, gameId: String): ScrimMatch {
        val match = findMatchById(matchId) ?: throw IllegalArgumentException("매치를 찾을 수 없습니다: $matchId")
        match.gameId = gameId
        return match
    }

    // MatchResult 관련 메서드
    fun findResultsByMatch(match: ScrimMatch): List<MatchResult> =
        matchResultRepository.findByMatchOrderByRankAsc(match)

    fun findResultsByTeam(team: Team): List<MatchResult> = matchResultRepository.findByTeam(team)

    @Transactional
    fun addMatchResult(match: ScrimMatch, team: Team, rank: Int, killCount: Int, totalScore: Int): MatchResult {
        return matchResultRepository.save(
            MatchResult(
                match = match,
                team = team,
                rank = rank,
                killCount = killCount,
                totalScore = totalScore
            )
        )
    }

    @Transactional
    fun updateMatchResult(resultId: Long, rank: Int, killCount: Int, totalScore: Int): MatchResult {
        val result = matchResultRepository.findById(resultId).orElse(null)
            ?: throw IllegalArgumentException("결과를 찾을 수 없습니다: $resultId")
        result.rank = rank
        result.killCount = killCount
        result.totalScore = totalScore
        return result
    }
}
