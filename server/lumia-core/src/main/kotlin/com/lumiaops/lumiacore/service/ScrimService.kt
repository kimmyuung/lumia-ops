package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.domain.scrim.Scrim
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import com.lumiaops.lumiacore.domain.scrim.ScrimStatus
import com.lumiaops.lumiacore.exception.NotFoundException
import com.lumiaops.lumiacore.repository.ScrimRepository
import com.lumiaops.lumiacore.repository.MatchResultRepository
import com.lumiaops.lumiacore.repository.ScrimMatchRepository
import com.lumiaops.lumiacore.util.ScoreCalculator
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(javaClass)

    // ==================== Scrim 조회 ====================

    fun findScrimById(id: Long): Scrim? = scrimRepository.findById(id).orElse(null)

    fun getScrimById(id: Long): Scrim = scrimRepository.findById(id)
        .orElseThrow { NotFoundException("스크림을 찾을 수 없습니다: $id") }

    fun findAllScrims(): List<Scrim> = scrimRepository.findAll()

    fun findActiveScrims(): List<Scrim> = scrimRepository.findByStatusIn(
        listOf(ScrimStatus.SCHEDULED, ScrimStatus.IN_PROGRESS)
    )

    fun findFinishedScrims(): List<Scrim> = scrimRepository.findByStatus(ScrimStatus.FINISHED)

    fun findScrimsByStatus(status: ScrimStatus): List<Scrim> = scrimRepository.findByStatus(status)

    fun findScrimsByDateRange(start: LocalDateTime, end: LocalDateTime): List<Scrim> =
        scrimRepository.findByStartTimeBetween(start, end)

    fun searchScrimsByTitle(title: String): List<Scrim> =
        scrimRepository.findByTitleContainingIgnoreCase(title)

    // ==================== Scrim 생성/수정/삭제 ====================

    @Transactional
    fun createScrim(title: String, startTime: LocalDateTime): Scrim {
        val scrim = scrimRepository.save(Scrim(title = title, startTime = startTime))
        log.info("스크림 생성: scrimId=${scrim.id}, title=$title, startTime=$startTime")
        return scrim
    }

    @Transactional
    fun updateScrim(scrimId: Long, title: String?, startTime: LocalDateTime?): Scrim {
        val scrim = getScrimById(scrimId)
        val oldTitle = scrim.title
        val oldStartTime = scrim.startTime
        scrim.update(title, startTime)
        log.info("스크림 수정: scrimId=$scrimId, title: $oldTitle→${scrim.title}, startTime: $oldStartTime→${scrim.startTime}")
        return scrim
    }

    @Transactional
    fun deleteScrim(scrimId: Long) {
        log.info("스크림 삭제: scrimId=$scrimId")
        scrimRepository.deleteById(scrimId)
    }

    // ==================== Scrim 상태 변경 ====================

    @Transactional
    fun startScrim(scrimId: Long): Scrim {
        val scrim = getScrimById(scrimId)
        scrim.start()
        log.info("스크림 시작: scrimId=$scrimId, title=${scrim.title}")
        return scrim
    }

    @Transactional
    fun finishScrim(scrimId: Long): Scrim {
        val scrim = getScrimById(scrimId)
        scrim.finish()
        log.info("스크림 종료: scrimId=$scrimId, title=${scrim.title}, matchCount=${scrim.matchCount()}")
        return scrim
    }

    @Transactional
    fun cancelScrim(scrimId: Long): Scrim {
        val scrim = getScrimById(scrimId)
        scrim.cancel()
        log.warn("스크림 취소: scrimId=$scrimId, title=${scrim.title}")
        return scrim
    }

    // ==================== ScrimMatch 관리 ====================

    fun findMatchById(id: Long): ScrimMatch? = scrimMatchRepository.findById(id).orElse(null)

    fun findMatchesByScrim(scrim: Scrim): List<ScrimMatch> =
        scrimMatchRepository.findByScrimOrderByRoundNumberAsc(scrim)

    fun findMatchByGameId(gameId: String): ScrimMatch? = scrimMatchRepository.findByGameId(gameId)

    @Transactional
    fun addMatch(scrimId: Long, gameId: String? = null): ScrimMatch {
        val scrim = getScrimById(scrimId)
        val match = scrim.addMatch(gameId)
        scrimRepository.save(scrim)
        log.info("매치 추가: scrimId=$scrimId, roundNumber=${match.roundNumber}, gameId=$gameId")
        return match
    }

    @Transactional
    fun updateGameId(matchId: Long, gameId: String): ScrimMatch {
        val match = findMatchById(matchId) 
            ?: throw NotFoundException("매치를 찾을 수 없습니다: $matchId")
        match.updateGameId(gameId)  // 도메인 메서드 호출
        return match
    }

    // ==================== MatchResult 관리 ====================

    fun findResultsByMatch(match: ScrimMatch): List<MatchResult> =
        matchResultRepository.findByMatchOrderByRankAsc(match)

    fun findResultsByTeam(team: Team): List<MatchResult> = matchResultRepository.findByTeam(team)

    @Transactional
    fun addMatchResult(matchId: Long, team: Team, rank: Int, killCount: Int): MatchResult {
        val match = findMatchById(matchId)
            ?: throw NotFoundException("매치를 찾을 수 없습니다: $matchId")
        val result = match.addResult(team, rank, killCount)
        log.info("매치 결과 등록: matchId=$matchId, teamId=${team.id}, rank=$rank, killCount=$killCount, totalScore=${result.totalScore}")
        return result
    }

    @Transactional
    fun addMatchResultWithScore(matchId: Long, team: Team, rank: Int, killCount: Int, totalScore: Int): MatchResult {
        val match = findMatchById(matchId)
            ?: throw NotFoundException("매치를 찾을 수 없습니다: $matchId")
        return match.addResultWithScore(team, rank, killCount, totalScore)  // 도메인 메서드 호출
    }

    // ==================== 하위 호환성 유지 (Deprecated) ====================

    @Deprecated("Use addMatchResult(matchId, team, rank, killCount) instead", ReplaceWith("addMatchResult(matchId, team, rank, killCount)"))
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

    @Deprecated("Use addMatch(scrimId, gameId) instead")
    @Transactional
    fun addMatch(scrim: Scrim, roundNumber: Int, gameId: String? = null): ScrimMatch {
        return scrimMatchRepository.save(ScrimMatch(scrim = scrim, roundNumber = roundNumber, gameId = gameId))
    }

    @Transactional
    fun updateMatchResult(resultId: Long, rank: Int, killCount: Int, totalScore: Int): MatchResult {
        val result = matchResultRepository.findById(resultId).orElse(null)
            ?: throw NotFoundException("결과를 찾을 수 없습니다: $resultId")
        result.rank = rank
        result.killCount = killCount
        result.totalScore = totalScore
        return result
    }

    // ==================== 점수 재계산 ====================

    @Transactional
    fun recalculateScrimScores(scrimId: Long, killMultiplier: Int = ScoreCalculator.DEFAULT_KILL_MULTIPLIER) {
        val scrim = getScrimById(scrimId)
        
        scrim.matches.forEach { match ->
            match.results.forEach { result ->
                result.totalScore = ScoreCalculator.calculateScore(
                    result.rank, result.killCount, killMultiplier
                )
            }
        }
    }
}
