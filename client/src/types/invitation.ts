// 팀 초대 관련 타입

export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED'

export interface TeamInvitation {
  id: string
  teamId: string
  teamName: string
  invitedEmail: string
  invitedById: string
  inviterName: string
  role: 'ADMIN' | 'MEMBER'
  status: InvitationStatus
  token: string
  expiresAt: string
  createdAt: string
  respondedAt?: string
}

export interface CreateInvitationRequest {
  email: string
  role?: 'ADMIN' | 'MEMBER'
  message?: string // 초대 메시지 (선택)
}

export interface InvitationResponse {
  invitation: TeamInvitation
  emailSent: boolean
}
