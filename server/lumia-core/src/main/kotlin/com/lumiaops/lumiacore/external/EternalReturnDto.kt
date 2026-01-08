package com.lumiaops.lumiacore.external

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 이터널 리턴 API 공통 응답 래퍼
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErApiResponse<T>(
    val code: Int,
    val message: String,
    @JsonProperty("data")
    val data: T? = null
)

// ==================== 유저 정보 ====================

/**
 * 유저 기본 정보
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErUserInfo(
    val userNum: Long,
    val nickname: String
)

/**
 * 유저 시즌 통계 (랭크 모드)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErUserStats(
    val seasonId: Int = 0,
    val userNum: Long = 0,
    val matchingMode: Int = 0,                // 3 = 랭크
    val matchingTeamMode: Int = 0,            // 1=솔로, 2=듀오, 3=스쿼드
    val mmr: Int = 0,
    val nickname: String = "",
    val rank: Int = 0,
    val rankSize: Int = 0,
    val totalGames: Int = 0,
    val totalWins: Int = 0,                   // 1등 횟수
    val rankPercent: Double = 0.0,
    val averageRank: Double = 0.0,
    val averageKills: Double = 0.0,
    val averageAssistants: Double = 0.0,
    val averageHunts: Double = 0.0,
    val top1: Int = 0,
    val top2: Int = 0,
    val top3: Int = 0,
    val top5: Int = 0,
    val top7: Int = 0
)

// ==================== 실험체 통계 ====================

/**
 * 실험체별 사용 통계
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErCharacterStats(
    val characterCode: Int = 0,
    val totalGames: Int = 0,
    val usages: Int = 0,
    val maxKillings: Int = 0,
    val top1: Int = 0,
    val top2: Int = 0,
    val top3: Int = 0,
    val wins: Int = 0,
    val averageRank: Double = 0.0
)

/**
 * 실험체 사용 통계 목록 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErCharacterStatsWrapper(
    val userNum: Long = 0,
    val seasonId: Int = 0,
    val characterStats: List<ErCharacterStats> = emptyList()
)

// ==================== 게임 기록 ====================

/**
 * 게임 기록
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErGameRecord(
    val gameId: Long = 0,
    val seasonId: Int = 0,
    val userNum: Long = 0,
    val nickname: String = "",
    val characterNum: Int = 0,
    val characterLevel: Int = 0,
    val gameRank: Int = 0,
    val playerKill: Int = 0,
    val playerAssistant: Int = 0,
    val monsterKill: Int = 0,
    val matchingMode: Int = 0,
    val matchingTeamMode: Int = 0,
    val startDtm: String = "",
    val playTime: Int = 0,
    val mmrBefore: Int = 0,
    val mmrGain: Int = 0,
    val mmrAfter: Int = 0
)

// ==================== 가공된 응답 DTO ====================

/**
 * 플레이어 종합 통계 (가공된 데이터)
 */
data class PlayerStatsData(
    val userNum: Long,
    val nickname: String,
    val seasonId: Int,
    val mmr: Int,
    val rank: Int,
    val rankPercent: Double,
    val totalGames: Int,
    val wins: Int,
    val top3Count: Int,
    val top4Count: Int,          // 4등 이상 (= top3 + top5 구간에서 계산)
    val averageRank: Double,
    val averageKills: Double,
    val winRate: Double,         // 승률 (1등 비율)
    val top3Rate: Double,        // 3등 이상 비율
    val top4Rate: Double         // 4등 이상 비율
)

/**
 * 많이 사용한 실험체 정보
 */
data class TopCharacterData(
    val characterCode: Int,
    val characterName: String?,
    val totalGames: Int,
    val wins: Int,
    val top3Count: Int,
    val averageRank: Double,
    val winRate: Double
)

/**
 * 최근 게임 기록 (가공된 데이터)
 */
data class RecentGameData(
    val gameId: Long,
    val characterCode: Int,
    val characterName: String?,
    val gameRank: Int,
    val kills: Int,
    val assists: Int,
    val mmrChange: Int,
    val playTime: Int,
    val startDtm: String
)

// ==================== 상위 랭커 ====================

/**
 * 상위 랭커 정보
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErTopRanker(
    val userNum: Long = 0,
    val nickname: String = "",
    val rank: Int = 0,
    val mmr: Int = 0,
    val totalGames: Int = 0,
    val totalWins: Int = 0,
    val rankPercent: Double = 0.0,
    val averageRank: Double = 0.0,
    val averageKills: Double = 0.0,
    val top1: Int = 0,
    val top2: Int = 0,
    val top3: Int = 0
)

/**
 * 상위 랭커 목록 래퍼
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ErTopRankersWrapper(
    val topRanks: List<ErTopRanker> = emptyList()
)

// ==================== 메타 분석 데이터 ====================

/**
 * 메타 실험체 정보 (랭커들의 사용 빈도 기반)
 */
data class MetaCharacterData(
    val characterCode: Int,
    val characterName: String?,
    val pickCount: Int,          // 상위 랭커들 중 사용자 수
    val totalGames: Int,         // 총 플레이 횟수
    val averageWinRate: Double,  // 평균 승률
    val averageRank: Double,     // 평균 순위
    val pickRate: Double         // 픽률 (랭커들 중 비율)
)

/**
 * 상위 랭커 정보 (가공된 데이터)
 */
data class TopRankerData(
    val rank: Int,
    val userNum: Long,
    val nickname: String,
    val mmr: Int,
    val totalGames: Int,
    val wins: Int,
    val winRate: Double,
    val averageRank: Double,
    val topCharacter: TopCharacterData?  // 주력 실험체
)

/**
 * 조합 추천 결과
 */
data class CompositionRecommendation(
    val score: Double,           // 추천 점수
    val characters: List<RecommendedCharacter>,
    val reason: String           // 추천 이유
)

/**
 * 추천 실험체 정보
 */
data class RecommendedCharacter(
    val characterCode: Int,
    val characterName: String?,
    val role: String?,           // 역할 (딜러, 서포터 등)
    val averageWinRate: Double,
    val synergy: Double          // 팀 시너지 점수
)

