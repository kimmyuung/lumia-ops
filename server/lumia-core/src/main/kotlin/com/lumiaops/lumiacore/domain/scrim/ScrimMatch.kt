package com.lumiaops.lumiacore.domain.scrim

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.util.ScoreCalculator
import jakarta.persistence.*

/**
 * 스크림 매치 엔티티
 * 스크림 내 개별 라운드(게임)를 나타냅니다.
 */
@Entity
@Table(name = "scrim_matches")
class ScrimMatch(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrim_id")
    val scrim: Scrim,

    @Column(nullable = false)
    val roundNumber: Int, // 1라운드, 2라운드...

    @Column(unique = true)
    var gameId: String? = null // 이터널 리턴 공식 게임 ID (API 연동용)
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @OneToMany(mappedBy = "match", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private val _results: MutableList<MatchResult> = mutableListOf()

    val results: List<MatchResult>
        get() = _results.toList()

    // ==================== 조회 메서드 ====================

    /**
     * 우승팀 조회 (1등)
     */
    fun getWinner(): MatchResult? = _results.minByOrNull { it.rank }

    /**
     * 팀별 결과 조회
     */
    fun getResultFor(team: Team): MatchResult? =
        _results.find { it.team.id == team.id }

    /**
     * 결과 수
     */
    fun resultCount(): Int = _results.size

    /**
     * 결과가 완료되었는지 (특정 팀 수 이상)
     */
    fun isComplete(expectedTeamCount: Int): Boolean =
        _results.size >= expectedTeamCount

    // ==================== 결과 관리 메서드 ====================

    /**
     * 결과 추가 (동일 팀 중복 방지, 점수 자동 계산)
     */
    fun addResult(team: Team, rank: Int, killCount: Int, killMultiplier: Int = ScoreCalculator.DEFAULT_KILL_MULTIPLIER): MatchResult {
        require(_results.none { it.team.id == team.id }) {
            "이미 해당 팀(${team.name})의 결과가 등록되어 있습니다"
        }
        require(rank >= 1) { "순위는 1 이상이어야 합니다" }
        require(killCount >= 0) { "킬 수는 0 이상이어야 합니다" }

        val totalScore = ScoreCalculator.calculateScore(rank, killCount, killMultiplier)
        val result = MatchResult(
            match = this,
            team = team,
            rank = rank,
            killCount = killCount,
            totalScore = totalScore
        )
        _results.add(result)
        return result
    }

    /**
     * 결과 추가 (점수 수동 지정)
     */
    fun addResultWithScore(team: Team, rank: Int, killCount: Int, totalScore: Int): MatchResult {
        require(_results.none { it.team.id == team.id }) {
            "이미 해당 팀(${team.name})의 결과가 등록되어 있습니다"
        }
        require(rank >= 1) { "순위는 1 이상이어야 합니다" }

        val result = MatchResult(
            match = this,
            team = team,
            rank = rank,
            killCount = killCount,
            totalScore = totalScore
        )
        _results.add(result)
        return result
    }

    /**
     * 게임 ID 업데이트
     */
    fun updateGameId(newGameId: String) {
        this.gameId = newGameId
    }
}