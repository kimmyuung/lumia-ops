package com.lumiaops.lumiaapi.dto

import com.lumiaops.lumiacore.external.*

/**
 * 메타 실험체 응답
 */
data class MetaCharacterResponse(
    val characterCode: Int,
    val characterName: String?,
    val pickCount: Int,
    val totalGames: Int,
    val averageWinRate: Double,
    val averageRank: Double,
    val pickRate: Double
) {
    companion object {
        fun from(data: MetaCharacterData) = MetaCharacterResponse(
            characterCode = data.characterCode,
            characterName = data.characterName,
            pickCount = data.pickCount,
            totalGames = data.totalGames,
            averageWinRate = data.averageWinRate,
            averageRank = data.averageRank,
            pickRate = data.pickRate
        )
    }
}

/**
 * 상위 랭커 응답
 */
data class TopRankerResponse(
    val rank: Int,
    val userNum: Long,
    val nickname: String,
    val mmr: Int,
    val totalGames: Int,
    val wins: Int,
    val winRate: Double,
    val averageRank: Double,
    val topCharacter: CharacterStatsResponse?
) {
    companion object {
        fun from(data: TopRankerData) = TopRankerResponse(
            rank = data.rank,
            userNum = data.userNum,
            nickname = data.nickname,
            mmr = data.mmr,
            totalGames = data.totalGames,
            wins = data.wins,
            winRate = data.winRate,
            averageRank = data.averageRank,
            topCharacter = data.topCharacter?.let { CharacterStatsResponse.from(it) }
        )
    }
}

/**
 * 조합 추천 응답
 */
data class CompositionRecommendationResponse(
    val score: Double,
    val characters: List<RecommendedCharacterResponse>,
    val reason: String
) {
    companion object {
        fun from(data: CompositionRecommendation) = CompositionRecommendationResponse(
            score = data.score,
            characters = data.characters.map { RecommendedCharacterResponse.from(it) },
            reason = data.reason
        )
    }
}

/**
 * 추천 실험체 응답
 */
data class RecommendedCharacterResponse(
    val characterCode: Int,
    val characterName: String?,
    val role: String?,
    val averageWinRate: Double,
    val synergy: Double
) {
    companion object {
        fun from(data: RecommendedCharacter) = RecommendedCharacterResponse(
            characterCode = data.characterCode,
            characterName = data.characterName,
            role = data.role,
            averageWinRate = data.averageWinRate,
            synergy = data.synergy
        )
    }
}
