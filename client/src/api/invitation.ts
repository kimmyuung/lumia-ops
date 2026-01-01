import apiClient from './client'
import type {
    TeamInvitation,
    CreateInvitationRequest,
    InvitationResponse
} from '@/types/invitation'

// 초대 API
export const invitationApi = {
    /**
     * 팀에 멤버 초대 (이메일 발송)
     */
    async createInvitation(teamId: string, data: CreateInvitationRequest): Promise<InvitationResponse> {
        const response = await apiClient.post<InvitationResponse>(
            `/teams/${teamId}/invitations`,
            data
        )
        return response.data
    },

    /**
     * 팀의 대기 중인 초대 목록 조회
     */
    async getTeamInvitations(teamId: string): Promise<TeamInvitation[]> {
        const response = await apiClient.get<TeamInvitation[]>(
            `/teams/${teamId}/invitations`
        )
        return response.data
    },

    /**
     * 초대 취소
     */
    async cancelInvitation(teamId: string, invitationId: string): Promise<void> {
        await apiClient.delete(`/teams/${teamId}/invitations/${invitationId}`)
    },

    /**
     * 초대 재발송
     */
    async resendInvitation(teamId: string, invitationId: string): Promise<void> {
        await apiClient.post(`/teams/${teamId}/invitations/${invitationId}/resend`)
    },

    /**
     * 내게 온 초대 목록 조회
     */
    async getMyInvitations(): Promise<TeamInvitation[]> {
        const response = await apiClient.get<TeamInvitation[]>('/invitations/pending')
        return response.data
    },

    /**
     * 초대 상세 조회 (토큰으로)
     */
    async getInvitationByToken(token: string): Promise<TeamInvitation> {
        const response = await apiClient.get<TeamInvitation>(`/invitations/${token}`)
        return response.data
    },

    /**
     * 초대 수락
     */
    async acceptInvitation(token: string): Promise<{ teamId: string }> {
        const response = await apiClient.post<{ teamId: string }>(
            `/invitations/${token}/accept`
        )
        return response.data
    },

    /**
     * 초대 거절
     */
    async declineInvitation(token: string): Promise<void> {
        await apiClient.post(`/invitations/${token}/decline`)
    }
}

export default invitationApi
