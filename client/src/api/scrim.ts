import apiClient from './client'
import type { Scrim, CreateScrimRequest, ScrimResult, ScrimStatus } from '@/types/scrim'

// 스크림 수정 요청 타입
export interface UpdateScrimRequest {
    title?: string
    scheduledAt?: string
    opponentTeamName?: string
    mapName?: string
    notes?: string
    status?: ScrimStatus
}

// 결과 추가 요청 타입
export interface AddResultRequest {
    round: number
    placement: number
    kills: number
    notes?: string
}

// 스크림 API
export const scrimApi = {
    /**
     * 스크림 목록 조회
     */
    async getScrims(teamId?: string): Promise<Scrim[]> {
        const params = teamId ? { teamId } : {}
        const response = await apiClient.get<Scrim[]>('/scrims', { params })
        return response.data
    },

    /**
     * 스크림 상세 조회
     */
    async getScrim(id: string): Promise<Scrim> {
        const response = await apiClient.get<Scrim>(`/scrims/${id}`)
        return response.data
    },

    /**
     * 스크림 생성
     */
    async createScrim(data: CreateScrimRequest): Promise<Scrim> {
        const response = await apiClient.post<Scrim>('/scrims', data)
        return response.data
    },

    /**
     * 스크림 수정
     */
    async updateScrim(id: string, data: UpdateScrimRequest): Promise<Scrim> {
        const response = await apiClient.patch<Scrim>(`/scrims/${id}`, data)
        return response.data
    },

    /**
     * 스크림 삭제
     */
    async deleteScrim(id: string): Promise<void> {
        await apiClient.delete(`/scrims/${id}`)
    },

    /**
     * 스크림 상태 변경
     */
    async updateStatus(id: string, status: ScrimStatus): Promise<Scrim> {
        const response = await apiClient.patch<Scrim>(`/scrims/${id}/status`, { status })
        return response.data
    },

    /**
     * 결과 추가
     */
    async addResult(scrimId: string, data: AddResultRequest): Promise<ScrimResult> {
        const response = await apiClient.post<ScrimResult>(`/scrims/${scrimId}/results`, data)
        return response.data
    },

    /**
     * 결과 수정
     */
    async updateResult(scrimId: string, resultId: string, data: Partial<AddResultRequest>): Promise<ScrimResult> {
        const response = await apiClient.patch<ScrimResult>(
            `/scrims/${scrimId}/results/${resultId}`,
            data
        )
        return response.data
    },

    /**
     * 결과 삭제
     */
    async deleteResult(scrimId: string, resultId: string): Promise<void> {
        await apiClient.delete(`/scrims/${scrimId}/results/${resultId}`)
    }
}

export default scrimApi
