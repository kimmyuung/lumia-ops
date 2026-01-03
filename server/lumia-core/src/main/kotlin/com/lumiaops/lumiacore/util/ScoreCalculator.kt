package com.lumiaops.lumiacore.util

/**
 * 스크림 점수 계산 유틸리티
 * PUBG/배틀로얄 대회 룰 기반 점수 계산
 */
object ScoreCalculator {

    /**
     * 순위별 기본 점수 (PUBG 대회 룰 참고)
     * 1위: 10점, 2위: 6점, 3위: 5점, ...
     */
    private val PLACEMENT_POINTS = mapOf(
        1 to 10,
        2 to 6,
        3 to 5,
        4 to 4,
        5 to 3,
        6 to 2,
        7 to 1,
        8 to 1
    )

    /**
     * 기본 킬 점수 배수
     */
    const val DEFAULT_KILL_MULTIPLIER = 1

    /**
     * 총 점수 계산
     * @param rank 순위 (1부터 시작)
     * @param kills 킬 수
     * @param killMultiplier 킬 점수 배수 (대회 룰에 따라 조정)
     * @return 총 점수
     */
    fun calculateScore(rank: Int, kills: Int, killMultiplier: Int = DEFAULT_KILL_MULTIPLIER): Int {
        require(rank >= 1) { "순위는 1 이상이어야 합니다" }
        require(kills >= 0) { "킬 수는 0 이상이어야 합니다" }
        require(killMultiplier >= 0) { "킬 배수는 0 이상이어야 합니다" }

        val placementPoints = getPlacementPoints(rank)
        val killPoints = kills * killMultiplier

        return placementPoints + killPoints
    }

    /**
     * 순위 점수 조회
     * @param rank 순위
     * @return 순위 점수 (8위 이하는 0점)
     */
    fun getPlacementPoints(rank: Int): Int {
        return PLACEMENT_POINTS[rank] ?: 0
    }

    /**
     * 승리 여부 확인
     */
    fun isWin(rank: Int): Boolean = rank == 1

    /**
     * 상위 3위 여부 확인
     */
    fun isTop3(rank: Int): Boolean = rank in 1..3

    /**
     * 상위 절반 여부 확인 (배틀로얄 기준)
     * @param rank 순위
     * @param totalTeams 총 참가 팀 수
     */
    fun isTopHalf(rank: Int, totalTeams: Int): Boolean {
        require(totalTeams > 0) { "총 팀 수는 1 이상이어야 합니다" }
        return rank <= (totalTeams / 2)
    }

    /**
     * 순위별 점수 테이블 조회
     */
    fun getPlacementPointsTable(): Map<Int, Int> = PLACEMENT_POINTS.toMap()

    /**
     * 여러 매치 결과의 총점 계산
     */
    fun calculateTotalScore(results: List<MatchScoreInput>): Int {
        return results.sumOf { calculateScore(it.rank, it.kills, it.killMultiplier) }
    }

    /**
     * 평균 점수 계산
     */
    fun calculateAverageScore(results: List<MatchScoreInput>): Double {
        if (results.isEmpty()) return 0.0
        return calculateTotalScore(results).toDouble() / results.size
    }
}

/**
 * 점수 계산용 입력 데이터 클래스
 */
data class MatchScoreInput(
    val rank: Int,
    val kills: Int,
    val killMultiplier: Int = ScoreCalculator.DEFAULT_KILL_MULTIPLIER
)
