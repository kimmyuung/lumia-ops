// 팀 관련 타입

export interface Team {
  id: string
  name: string
  description: string
  logoUrl?: string
  createdAt: string
  updatedAt: string
  members: TeamMember[]
}

export interface TeamMember {
  id: string
  userId: string
  nickname: string
  role: TeamRole
  position?: string // 게임 내 포지션
  joinedAt: string
}

export type TeamRole = 'OWNER' | 'ADMIN' | 'MEMBER'

export interface CreateTeamRequest {
  name: string
  description?: string
}

export interface InviteTeamMemberRequest {
  userId: string
  role?: TeamRole
}
