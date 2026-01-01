// 스크림 관련 타입

export interface Scrim {
    id: string
    title: string
    scheduledAt: string
    status: ScrimStatus
    teamId: string
    opponentTeamName?: string
    mapName?: string
    notes?: string
    results: ScrimResult[]
    createdAt: string
}

export type ScrimStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

export interface ScrimResult {
    id: string
    scrimId: string
    round: number
    placement: number
    kills: number
    notes?: string
}

export interface CreateScrimRequest {
    title: string
    scheduledAt: string
    opponentTeamName?: string
    mapName?: string
    notes?: string
}
