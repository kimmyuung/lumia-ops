package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.external.*
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

/**
 * 플레이어 통계 서비스
 * 이터널 리턴 API를 통해 플레이어 전적/실험체 정보 제공
 */
@Service
class PlayerStatsService(
    private val apiClient: EternalReturnApiClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // 실험체 코드 -> 이름 매핑 (기본 제공)
    private val characterNames = mapOf(
        1 to "재키", 2 to "아야", 3 to "피오라", 4 to "매그너스", 5 to "자히르",
        6 to "나딘", 7 to "현우", 8 to "하트", 9 to "아이솔", 10 to "리다이린",
        11 to "유키", 12 to "혜진", 13 to "쇼이치", 14 to "쇼우", 15 to "엠마",
        16 to "레녹스", 17 to "로지", 18 to "루크", 19 to "캐시", 20 to "아드리아나",
        21 to "시셀라", 22 to "실비아", 23 to "바냐", 24 to "알렉스", 25 to "글렌",
        26 to "치아라", 27 to "라우라", 28 to "아델라", 29 to "버니스", 30 to "니키",
        31 to "얀", 32 to "이바", 33 to "다니엘", 34 to "위키", 35 to "레온",
        36 to "프리야", 37 to "이렘", 38 to "테오도르", 39 to "리나", 40 to "일레븐",
        41 to "띠아", 42 to "레니", 43 to "마이", 44 to "아이덴", 45 to "마르티나",
        46 to "헤레라", 47 to "류", 48 to "에스텔", 49 to "피올로", 50 to "다르코",
        51 to "스텔라", 52 to "나타폰", 53 to "연우", 54 to "수아", 55 to "데비&말로",
        56 to "카밀로", 57 to "클로에", 58 to "비앙카", 59 to "알론소", 60 to "레이첼",
        61 to "펠릭스", 62 to "에키온", 63 to "칼라", 64 to "가넷", 65 to "타지아",
        66 to "샬럿", 67 to "도미니크", 68 to "리오"
    )

    /**
     * API 키가 설정되어 있는지 확인
     */
    fun isConfigured(): Boolean = apiClient.isConfigured()

    /**
     * 닉네임으로 플레이어 종합 통계 조회
     * @param nickname 이터널 리턴 인게임 닉네임
     * @param seasonId 시즌 ID (null이면 현재 시즌)
     * @param teamMode 팀 모드 (1=솔로, 2=듀오, 3=스쿼드, null=전체)
     */
    @Cacheable(value = ["playerStats"], key = "#nickname + '-' + #seasonId + '-' + #teamMode")
    fun getPlayerStats(
        nickname: String,
        seasonId: Int? = null,
        teamMode: Int? = null
    ): PlayerStatsData? {
        val userInfo = apiClient.getUserByNickname(nickname)
            ?: return null

        val allStats = apiClient.getUserStats(userInfo.userNum, seasonId)
        
        // 팀 모드 필터링 (기본: 스쿼드 모드)
        val stats = allStats.firstOrNull { 
            teamMode == null || it.matchingTeamMode == teamMode 
        } ?: allStats.firstOrNull()
        
        if (stats == null) {
            return PlayerStatsData(
                userNum = userInfo.userNum,
                nickname = userInfo.nickname,
                seasonId = 0,
                mmr = 0,
                rank = 0,
                rankPercent = 0.0,
                totalGames = 0,
                wins = 0,
                top3Count = 0,
                top4Count = 0,
                averageRank = 0.0,
                averageKills = 0.0,
                winRate = 0.0,
                top3Rate = 0.0,
                top4Rate = 0.0
            )
        }

        val top4Count = stats.top5  // top5는 5등 이상의 횟수
        val winRate = if (stats.totalGames > 0) (stats.top1.toDouble() / stats.totalGames) * 100 else 0.0
        val top3Rate = if (stats.totalGames > 0) (stats.top3.toDouble() / stats.totalGames) * 100 else 0.0
        val top4Rate = if (stats.totalGames > 0) (top4Count.toDouble() / stats.totalGames) * 100 else 0.0

        return PlayerStatsData(
            userNum = userInfo.userNum,
            nickname = userInfo.nickname,
            seasonId = stats.seasonId,
            mmr = stats.mmr,
            rank = stats.rank,
            rankPercent = stats.rankPercent,
            totalGames = stats.totalGames,
            wins = stats.top1,
            top3Count = stats.top3,
            top4Count = top4Count,
            averageRank = stats.averageRank,
            averageKills = stats.averageKills,
            winRate = winRate,
            top3Rate = top3Rate,
            top4Rate = top4Rate
        )
    }

    /**
     * 많이 사용한 실험체 Top N 조회
     */
    @Cacheable(value = ["playerStats"], key = "'chars-' + #nickname + '-' + #seasonId")
    fun getTopCharacters(
        nickname: String, 
        limit: Int = 5,
        seasonId: Int? = null
    ): List<TopCharacterData> {
        val userInfo = apiClient.getUserByNickname(nickname)
            ?: return emptyList()

        val characterStats = apiClient.getCharacterStats(userInfo.userNum, seasonId)
        
        return characterStats
            .sortedByDescending { it.totalGames }
            .take(limit)
            .map { stat ->
                val winRate = if (stat.totalGames > 0) {
                    (stat.top1.toDouble() / stat.totalGames) * 100
                } else 0.0
                
                TopCharacterData(
                    characterCode = stat.characterCode,
                    characterName = characterNames[stat.characterCode],
                    totalGames = stat.totalGames,
                    wins = stat.top1,
                    top3Count = stat.top3,
                    averageRank = stat.averageRank,
                    winRate = winRate
                )
            }
    }

    /**
     * 최근 게임 기록 조회
     */
    @Cacheable(value = ["playerGames"], key = "#nickname")
    fun getRecentGames(
        nickname: String,
        limit: Int = 10
    ): List<RecentGameData> {
        val userInfo = apiClient.getUserByNickname(nickname)
            ?: return emptyList()

        val games = apiClient.getUserGames(userInfo.userNum)
        
        return games
            .take(limit)
            .map { game ->
                RecentGameData(
                    gameId = game.gameId,
                    characterCode = game.characterNum,
                    characterName = characterNames[game.characterNum],
                    gameRank = game.gameRank,
                    kills = game.playerKill,
                    assists = game.playerAssistant,
                    mmrChange = game.mmrGain,
                    playTime = game.playTime,
                    startDtm = game.startDtm
                )
            }
    }

    /**
     * 실험체 이름 조회
     */
    fun getCharacterName(code: Int): String? = characterNames[code]
}
