import apiClient from './client'
import type { Team, CreateTeamRequest, TeamMember, TeamRole } from '@/types/team'

// 팀 수정 요청 타입
export interface UpdateTeamRequest {
    name?: string
    description?: string
    logoUrl?: string
}

// 멤버 초대 요청 타입
export interface InviteMemberRequest {
    email: string
    role?: TeamRole
}

// 팀 API
export const teamApi = {
    /**
     * 팀 목록 조회
     */
    async getTeams(): Promise<Team[]> {
        const response = await apiClient.get<Team[]>('/teams')
        return response.data
    },

    /**
     * 내 팀 조회
     */
    async getMyTeam(): Promise<Team | null> {
        try {
            const response = await apiClient.get<Team>('/teams/me')
            return response.data
        } catch {
            return null
        }
    },

    /**
     * 팀 상세 조회
     */
    async getTeam(id: string): Promise<Team> {
        const response = await apiClient.get<Team>(`/teams/${id}`)
        return response.data
    },

    /**
     * 팀 생성
     */
    async createTeam(data: CreateTeamRequest): Promise<Team> {
        const response = await apiClient.post<Team>('/teams', data)
        return response.data
    },

    /**
     * 팀 수정
     */
    async updateTeam(id: string, data: UpdateTeamRequest): Promise<Team> {
        const response = await apiClient.patch<Team>(`/teams/${id}`, data)
        return response.data
    },

    /**
     * 팀 삭제
     */
    async deleteTeam(id: string): Promise<void> {
        await apiClient.delete(`/teams/${id}`)
    },

    /**
     * 멤버 초대
     */
    async inviteMember(teamId: string, data: InviteMemberRequest): Promise<TeamMember> {
        const response = await apiClient.post<TeamMember>(`/teams/${teamId}/members`, data)
        return response.data
    },

    /**
     * 멤버 제거
     */
    async removeMember(teamId: string, memberId: string): Promise<void> {
        await apiClient.delete(`/teams/${teamId}/members/${memberId}`)
    },

    /**
     * 멤버 역할 변경
     */
    async updateMemberRole(teamId: string, memberId: string, role: TeamRole): Promise<TeamMember> {
        const response = await apiClient.patch<TeamMember>(
            `/teams/${teamId}/members/${memberId}`,
            { role }
        )
        return response.data
    },

    /**
     * 팀 탈퇴
     */
    async leaveTeam(teamId: string): Promise<void> {
        await apiClient.post(`/teams/${teamId}/leave`)
    }
}

export default teamApi
