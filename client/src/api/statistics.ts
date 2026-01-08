import apiClient from './client'

// ==================== 타입 정의 ====================

/** 팀 통계 응답 */
export interface TeamStatsResponse {
    teamId: number
    teamName: string
    totalMatches: number
    averageRank: number
    totalKills: number
    averageKillsPerMatch: number
    totalScore: number
    winCount: number
    top3Count: number
    winRate: number
    top3Rate: number
}

/** 매치 요약 */
export interface MatchSummary {
    matchId: number
    rank: number
    kills: number
    score: number
}

/** 성적 트렌드 */
export type PerformanceTrend = 'IMPROVING' | 'STABLE' | 'DECLINING'

/** 최근 성적 응답 */
export interface RecentPerformanceResponse {
    teamId: number
    teamName: string
    matchCount: number
    matches: MatchSummary[]
    averageRank: number
    totalKills: number
    totalScore: number
    winCount: number
    trend: PerformanceTrend
}

/** 리더보드 항목 */
export interface LeaderboardEntry {
    rank: number
    teamId: number
    teamName: string
    totalMatches: number
    averageRank: number
    totalKills: number
    totalScore: number
    winCount: number
    top3Count: number
}

/** 리더보드 응답 */
export interface LeaderboardResponse {
    teams: LeaderboardEntry[]
}

/** 점수 계산 요청 */
export interface CalculateScoreRequest {
    rank: number
    kills: number
    killMultiplier?: number
}

/** 점수 계산 응답 */
export interface CalculateScoreResponse {
    placementPoints: number
    killPoints: number
    totalScore: number
}

// ==================== API 함수 ====================

export const statisticsApi = {
    /**
     * 팀 종합 통계 조회
     */
    async getTeamStats(teamId: number): Promise<TeamStatsResponse> {
        const response = await apiClient.get<TeamStatsResponse>(`/statistics/teams/${teamId}`)
        return response.data
    },

    /**
     * 팀 최근 성적 조회
     */
    async getRecentPerformance(teamId: number, count = 10): Promise<RecentPerformanceResponse> {
        const response = await apiClient.get<RecentPerformanceResponse>(
            `/statistics/teams/${teamId}/recent`,
            { params: { count } }
        )
        return response.data
    },

    /**
     * 전체 팀 순위표 조회
     */
    async getLeaderboard(): Promise<LeaderboardResponse> {
        const response = await apiClient.get<LeaderboardResponse>('/statistics/leaderboard')
        return response.data
    },

    /**
     * 점수 계산
     */
    async calculateScore(request: CalculateScoreRequest): Promise<CalculateScoreResponse> {
        const response = await apiClient.post<CalculateScoreResponse>(
            '/statistics/calculate-score',
            request
        )
        return response.data
    },

    /**
     * 순위별 배치 점수표 조회
     */
    async getPlacementPoints(): Promise<Record<number, number>> {
        const response = await apiClient.get<Record<number, number>>('/statistics/placement-points')
        return response.data
    }
}

export default statisticsApi
