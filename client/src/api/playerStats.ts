import apiClient from './client'

// ==================== 타입 정의 ====================

/** 플레이어 통계 응답 */
export interface PlayerStatsResponse {
  userNum: number
  nickname: string
  seasonId: number
  mmr: number
  rank: number
  rankPercent: number
  totalGames: number
  wins: number
  top3Count: number
  top4Count: number
  averageRank: number
  averageKills: number
  winRate: number
  top3Rate: number
  top4Rate: number
}

/** 실험체 통계 */
export interface CharacterStatsResponse {
  characterCode: number
  characterName: string | null
  totalGames: number
  wins: number
  top3Count: number
  averageRank: number
  winRate: number
}

/** 게임 기록 */
export interface GameRecordResponse {
  gameId: number
  characterCode: number
  characterName: string | null
  gameRank: number
  kills: number
  assists: number
  mmrChange: number
  playTime: number
  startDtm: string
}

/** API 상태 */
export interface ApiStatusResponse {
  configured: boolean
}

// ==================== API 함수 ====================

export const playerStatsApi = {
  /**
   * 플레이어 종합 통계 조회
   */
  async getPlayerStats(
    nickname: string,
    seasonId?: number,
    teamMode?: number
  ): Promise<PlayerStatsResponse> {
    const response = await apiClient.get<PlayerStatsResponse>(`/player-stats/${nickname}`, {
      params: { seasonId, teamMode }
    })
    return response.data
  },

  /**
   * 많이 사용한 실험체 조회
   */
  async getTopCharacters(
    nickname: string,
    limit = 5,
    seasonId?: number
  ): Promise<CharacterStatsResponse[]> {
    const response = await apiClient.get<CharacterStatsResponse[]>(
      `/player-stats/${nickname}/characters`,
      { params: { limit, seasonId } }
    )
    return response.data
  },

  /**
   * 최근 게임 기록 조회
   */
  async getRecentGames(nickname: string, limit = 10): Promise<GameRecordResponse[]> {
    const response = await apiClient.get<GameRecordResponse[]>(`/player-stats/${nickname}/games`, {
      params: { limit }
    })
    return response.data
  },

  /**
   * API 설정 상태 확인
   */
  async getApiStatus(): Promise<ApiStatusResponse> {
    const response = await apiClient.get<ApiStatusResponse>('/player-stats/status')
    return response.data
  }
}

export default playerStatsApi
