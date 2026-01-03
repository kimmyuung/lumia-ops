import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTeamStore } from '../team'
import type { Team, TeamMember } from '@/types/team'

// API 모킹
vi.mock('@/api/team', () => ({
    teamApi: {
        getMyTeam: vi.fn(),
        getTeam: vi.fn(),
        createTeam: vi.fn(),
        updateTeam: vi.fn(),
        deleteTeam: vi.fn(),
        leaveTeam: vi.fn(),
        inviteMember: vi.fn(),
        removeMember: vi.fn(),
        updateMemberRole: vi.fn()
    }
}))

// Toast 모킹
vi.mock('@/composables/useToast', () => ({
    useToast: () => ({
        success: vi.fn(),
        error: vi.fn(),
        info: vi.fn(),
        warning: vi.fn()
    })
}))

// 테스트용 mock 데이터
const mockMember: TeamMember = {
    id: 'member-1',
    userId: 'user-1',
    nickname: 'TestUser',
    role: 'MEMBER',
    joinedAt: '2026-01-01T00:00:00Z'
}

const mockTeam: Team = {
    id: 'team-1',
    name: 'Test Team',
    description: 'A test team',
    members: [
        { ...mockMember, role: 'OWNER', userId: 'user-owner', nickname: 'Owner' },
        mockMember
    ],
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
}

describe('useTeamStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        vi.clearAllMocks()
    })

    describe('initial state', () => {
        it('should have null currentTeam initially', () => {
            const store = useTeamStore()
            expect(store.currentTeam).toBeNull()
        })

        it('should not be loading initially', () => {
            const store = useTeamStore()
            expect(store.isLoading).toBe(false)
        })

        it('should have no error initially', () => {
            const store = useTeamStore()
            expect(store.error).toBeNull()
        })
    })

    describe('hasTeam', () => {
        it('should return false when no team', () => {
            const store = useTeamStore()
            expect(store.hasTeam).toBe(false)
        })

        it('should return true when team exists', () => {
            const store = useTeamStore()
            store.currentTeam = mockTeam

            expect(store.hasTeam).toBe(true)
        })
    })

    describe('memberCount', () => {
        it('should return 0 when no team', () => {
            const store = useTeamStore()
            expect(store.memberCount).toBe(0)
        })

        it('should return correct member count', () => {
            const store = useTeamStore()
            store.currentTeam = mockTeam

            expect(store.memberCount).toBe(2)
        })
    })

    describe('clearError', () => {
        it('should clear error', () => {
            const store = useTeamStore()
            store.error = 'Some error'

            store.clearError()

            expect(store.error).toBeNull()
        })
    })
})
