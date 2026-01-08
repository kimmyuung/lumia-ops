package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.external.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 메타 분석 서비스
 * 상위 랭커 분석 및 실험체 추천
 */
@Service
class MetaAnalysisService(
    private val apiClient: EternalReturnApiClient,
    private val playerStatsService: PlayerStatsService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // 실험체 역할 분류 (간단한 분류)
    private val characterRoles = mapOf(
        // 딜러
        1 to "딜러", 2 to "딜러", 3 to "딜러", 10 to "딜러", 11 to "딜러",
        13 to "딜러", 14 to "딜러", 18 to "딜러", 19 to "딜러", 27 to "딜러",
        35 to "딜러", 37 to "딜러", 43 to "딜러", 47 to "딜러", 49 to "딜러",
        // 탱커
        4 to "탱커", 17 to "탱커", 24 to "탱커", 31 to "탱커", 44 to "탱커",
        50 to "탱커", 62 to "탱커",
        // 서포터
        7 to "서포터", 15 to "서포터", 20 to "서포터", 26 to "서포터",
        28 to "서포터", 38 to "서포터", 41 to "서포터", 53 to "서포터",
        // 암살자
        5 to "암살자", 6 to "암살자", 12 to "암살자", 21 to "암살자",
        22 to "암살자", 25 to "암살자", 30 to "암살자", 36 to "암살자",
        40 to "암살자", 45 to "암살자", 48 to "암살자", 52 to "암살자"
    )

    /**
     * API 설정 상태 확인
     */
    fun isConfigured(): Boolean = apiClient.isConfigured()

    /**
     * 상위 랭커들이 많이 사용하는 실험체 TOP N 조회
     * @param seasonId 시즌 ID
     * @param teamMode 팀 모드 (1=솔로, 2=듀오, 3=스쿼드)
     * @param limit 조회할 실험체 수
     */
    fun getMetaCharacters(
        seasonId: Int,
        teamMode: Int = 3,
        limit: Int = 10
    ): List<MetaCharacterData> {
        val topRankers = apiClient.getTopRankers(seasonId, teamMode)
        if (topRankers.isEmpty()) {
            logger.warn("No top rankers found for season $seasonId, teamMode $teamMode")
            return emptyList()
        }

        // 상위 100명의 실험체 통계 수집
        val characterStatsMap = mutableMapOf<Int, MutableList<ErCharacterStats>>()
        
        topRankers.take(100).forEach { ranker ->
            val stats = apiClient.getCharacterStats(ranker.userNum, seasonId)
            stats.forEach { charStat ->
                characterStatsMap.getOrPut(charStat.characterCode) { mutableListOf() }.add(charStat)
            }
        }

        val totalRankers = topRankers.take(100).size

        // 실험체별 메타 데이터 계산
        return characterStatsMap.map { (code, statsList) ->
            val totalGames = statsList.sumOf { it.totalGames }
            val totalWins = statsList.sumOf { it.top1 }
            val avgWinRate = if (totalGames > 0) (totalWins.toDouble() / totalGames) * 100 else 0.0
            val avgRank = if (statsList.isNotEmpty()) statsList.map { it.averageRank }.average() else 0.0

            MetaCharacterData(
                characterCode = code,
                characterName = playerStatsService.getCharacterName(code),
                pickCount = statsList.size,
                totalGames = totalGames,
                averageWinRate = avgWinRate,
                averageRank = avgRank,
                pickRate = (statsList.size.toDouble() / totalRankers) * 100
            )
        }
            .sortedByDescending { it.pickCount }
            .take(limit)
    }

    /**
     * 상위 랭커 목록 조회 (주력 실험체 포함)
     */
    fun getTopRankers(
        seasonId: Int,
        teamMode: Int = 3,
        limit: Int = 20
    ): List<TopRankerData> {
        val topRankers = apiClient.getTopRankers(seasonId, teamMode)
        
        return topRankers.take(limit).map { ranker ->
            val winRate = if (ranker.totalGames > 0) {
                (ranker.totalWins.toDouble() / ranker.totalGames) * 100
            } else 0.0

            // 주력 실험체 조회
            val topCharacter = try {
                val charStats = apiClient.getCharacterStats(ranker.userNum, seasonId)
                charStats.maxByOrNull { it.totalGames }?.let { stat ->
                    val charWinRate = if (stat.totalGames > 0) {
                        (stat.top1.toDouble() / stat.totalGames) * 100
                    } else 0.0
                    
                    TopCharacterData(
                        characterCode = stat.characterCode,
                        characterName = playerStatsService.getCharacterName(stat.characterCode),
                        totalGames = stat.totalGames,
                        wins = stat.top1,
                        top3Count = stat.top3,
                        averageRank = stat.averageRank,
                        winRate = charWinRate
                    )
                }
            } catch (e: Exception) {
                logger.debug("Failed to get character stats for ${ranker.nickname}")
                null
            }

            TopRankerData(
                rank = ranker.rank,
                userNum = ranker.userNum,
                nickname = ranker.nickname,
                mmr = ranker.mmr,
                totalGames = ranker.totalGames,
                wins = ranker.totalWins,
                winRate = winRate,
                averageRank = ranker.averageRank,
                topCharacter = topCharacter
            )
        }
    }

    /**
     * 팀원 기반 조합 추천
     * @param memberNicknames 팀원들의 게임 닉네임 목록
     * @param seasonId 시즌 ID
     */
    fun recommendComposition(
        memberNicknames: List<String>,
        seasonId: Int? = null
    ): List<CompositionRecommendation> {
        if (memberNicknames.isEmpty()) {
            return emptyList()
        }

        // 각 팀원의 주력 실험체 조회
        val memberTopCharacters = memberNicknames.mapNotNull { nickname ->
            val topChars = playerStatsService.getTopCharacters(nickname, 3, seasonId)
            if (topChars.isNotEmpty()) nickname to topChars else null
        }.toMap()

        if (memberTopCharacters.isEmpty()) {
            return emptyList()
        }

        // 메타 실험체 가져오기
        val metaCharacters = if (seasonId != null) {
            getMetaCharacters(seasonId, 3, 20)
        } else {
            emptyList()
        }

        // 조합 추천 생성
        val recommendations = mutableListOf<CompositionRecommendation>()

        // 추천 1: 각자 가장 잘하는 실험체
        val bestCharacters = memberTopCharacters.map { (nickname, chars) ->
            val best = chars.firstOrNull()
            if (best != null) {
                RecommendedCharacter(
                    characterCode = best.characterCode,
                    characterName = best.characterName,
                    role = characterRoles[best.characterCode],
                    averageWinRate = best.winRate,
                    synergy = calculateSynergy(best.characterCode, chars.map { it.characterCode })
                )
            } else null
        }.filterNotNull()

        if (bestCharacters.isNotEmpty()) {
            val avgWinRate = bestCharacters.map { it.averageWinRate }.average()
            recommendations.add(
                CompositionRecommendation(
                    score = avgWinRate,
                    characters = bestCharacters,
                    reason = "각 팀원이 가장 높은 승률을 기록한 실험체 조합입니다."
                )
            )
        }

        // 추천 2: 역할 밸런스 기반
        val roleBalancedChars = createRoleBalancedComposition(memberTopCharacters)
        if (roleBalancedChars.isNotEmpty()) {
            val avgWinRate = roleBalancedChars.map { it.averageWinRate }.average()
            recommendations.add(
                CompositionRecommendation(
                    score = avgWinRate * 1.1, // 밸런스 보너스
                    characters = roleBalancedChars,
                    reason = "딜러, 탱커, 서포터 역할이 균형잡힌 조합입니다."
                )
            )
        }

        return recommendations.sortedByDescending { it.score }
    }

    /**
     * 역할 밸런스 조합 생성
     */
    private fun createRoleBalancedComposition(
        memberTopCharacters: Map<String, List<TopCharacterData>>
    ): List<RecommendedCharacter> {
        val usedRoles = mutableSetOf<String>()
        val result = mutableListOf<RecommendedCharacter>()

        memberTopCharacters.forEach { (_, chars) ->
            // 아직 사용되지 않은 역할의 실험체 선택
            val selectedChar = chars.firstOrNull { char ->
                val role = characterRoles[char.characterCode]
                role != null && role !in usedRoles
            } ?: chars.firstOrNull()

            if (selectedChar != null) {
                val role = characterRoles[selectedChar.characterCode]
                if (role != null) usedRoles.add(role)
                
                result.add(
                    RecommendedCharacter(
                        characterCode = selectedChar.characterCode,
                        characterName = selectedChar.characterName,
                        role = role,
                        averageWinRate = selectedChar.winRate,
                        synergy = 1.0
                    )
                )
            }
        }

        return result
    }

    /**
     * 시너지 점수 계산 (간단한 버전)
     */
    private fun calculateSynergy(characterCode: Int, teamCharacters: List<Int>): Double {
        // 기본 시너지 1.0
        var synergy = 1.0

        val myRole = characterRoles[characterCode]
        val teamRoles = teamCharacters.mapNotNull { characterRoles[it] }

        // 같은 역할이 많으면 시너지 감소
        val sameRoleCount = teamRoles.count { it == myRole }
        if (sameRoleCount > 1) synergy -= 0.1 * (sameRoleCount - 1)

        // 다양한 역할이 있으면 시너지 증가
        val uniqueRoles = teamRoles.toSet().size
        synergy += 0.05 * uniqueRoles

        return synergy.coerceIn(0.5, 1.5)
    }

    /**
     * 실험체 역할 조회
     */
    fun getCharacterRole(characterCode: Int): String? = characterRoles[characterCode]
}
