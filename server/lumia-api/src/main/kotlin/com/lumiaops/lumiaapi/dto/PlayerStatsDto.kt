package com.lumiaops.lumiaapi.dto

import com.lumiaops.lumiacore.external.PlayerStatsData
import com.lumiaops.lumiacore.external.RecentGameData
import com.lumiaops.lumiacore.external.TopCharacterData

/**
 * 플레이어 종합 통계 응답
 */
data class PlayerStatsResponse(
    val userNum: Long,
    val nickname: String,
    val seasonId: Int,
    val mmr: Int,
    val rank: Int,
    val rankPercent: Double,
    val totalGames: Int,
    val wins: Int,
    val top3Count: Int,
    val top4Count: Int,
    val averageRank: Double,
    val averageKills: Double,
    val winRate: Double,
    val top3Rate: Double,
    val top4Rate: Double
) {
    companion object {
        fun from(data: PlayerStatsData) = PlayerStatsResponse(
            userNum = data.userNum,
            nickname = data.nickname,
            seasonId = data.seasonId,
            mmr = data.mmr,
            rank = data.rank,
            rankPercent = data.rankPercent,
            totalGames = data.totalGames,
            wins = data.wins,
            top3Count = data.top3Count,
            top4Count = data.top4Count,
            averageRank = data.averageRank,
            averageKills = data.averageKills,
            winRate = data.winRate,
            top3Rate = data.top3Rate,
            top4Rate = data.top4Rate
        )
    }
}

/**
 * 실험체 통계 응답
 */
data class CharacterStatsResponse(
    val characterCode: Int,
    val characterName: String?,
    val totalGames: Int,
    val wins: Int,
    val top3Count: Int,
    val averageRank: Double,
    val winRate: Double
) {
    companion object {
        fun from(data: TopCharacterData) = CharacterStatsResponse(
            characterCode = data.characterCode,
            characterName = data.characterName,
            totalGames = data.totalGames,
            wins = data.wins,
            top3Count = data.top3Count,
            averageRank = data.averageRank,
            winRate = data.winRate
        )
    }
}

/**
 * 최근 게임 기록 응답
 */
data class GameRecordResponse(
    val gameId: Long,
    val characterCode: Int,
    val characterName: String?,
    val gameRank: Int,
    val kills: Int,
    val assists: Int,
    val mmrChange: Int,
    val playTime: Int,
    val startDtm: String
) {
    companion object {
        fun from(data: RecentGameData) = GameRecordResponse(
            gameId = data.gameId,
            characterCode = data.characterCode,
            characterName = data.characterName,
            gameRank = data.gameRank,
            kills = data.kills,
            assists = data.assists,
            mmrChange = data.mmrChange,
            playTime = data.playTime,
            startDtm = data.startDtm
        )
    }
}

/**
 * 게임 닉네임 업데이트 요청
 */
data class UpdateGameNicknameRequest(
    val gameNickname: String?
)
