import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { teamApi } from '../team'
import apiClient from '../client'

vi.mock('../client', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        patch: vi.fn(),
        delete: vi.fn()
    }
}))

describe('teamApi', () => {
    const mockGet = vi.mocked(apiClient.get)
    const mockPost = vi.mocked(apiClient.post)
    const mockPatch = vi.mocked(apiClient.patch)
    const mockDelete = vi.mocked(apiClient.delete)

    beforeEach(() => {
        vi.clearAllMocks()
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    describe('getTeams', () => {
        it('should call GET /teams', async () => {
            const mockTeams = [{ id: '1', name: 'Team1' }, { id: '2', name: 'Team2' }]
            mockGet.mockResolvedValue({ data: mockTeams })

            const result = await teamApi.getTeams()

            expect(mockGet).toHaveBeenCalledWith('/teams')
            expect(result).toEqual(mockTeams)
        })
    })

    describe('getMyTeam', () => {
        it('should call GET /teams/me', async () => {
            const mockTeam = { id: '1', name: 'MyTeam' }
            mockGet.mockResolvedValue({ data: mockTeam })

            const result = await teamApi.getMyTeam()

            expect(mockGet).toHaveBeenCalledWith('/teams/me')
            expect(result).toEqual(mockTeam)
        })

        it('should return null when no team exists', async () => {
            mockGet.mockRejectedValue(new Error('Not found'))

            const result = await teamApi.getMyTeam()

            expect(result).toBeNull()
        })
    })

    describe('getTeam', () => {
        it('should call GET /teams/:id', async () => {
            const mockTeam = { id: '123', name: 'Team123' }
            mockGet.mockResolvedValue({ data: mockTeam })

            const result = await teamApi.getTeam('123')

            expect(mockGet).toHaveBeenCalledWith('/teams/123')
            expect(result).toEqual(mockTeam)
        })
    })

    describe('createTeam', () => {
        it('should call POST /teams with data', async () => {
            const createData = { name: 'NewTeam', description: 'Desc' }
            const mockTeam = { id: '1', ...createData }
            mockPost.mockResolvedValue({ data: mockTeam })

            const result = await teamApi.createTeam(createData)

            expect(mockPost).toHaveBeenCalledWith('/teams', createData)
            expect(result).toEqual(mockTeam)
        })
    })

    describe('updateTeam', () => {
        it('should call PATCH /teams/:id with data', async () => {
            const updateData = { name: 'UpdatedTeam' }
            const mockTeam = { id: '1', name: 'UpdatedTeam' }
            mockPatch.mockResolvedValue({ data: mockTeam })

            const result = await teamApi.updateTeam('1', updateData)

            expect(mockPatch).toHaveBeenCalledWith('/teams/1', updateData)
            expect(result).toEqual(mockTeam)
        })
    })

    describe('deleteTeam', () => {
        it('should call DELETE /teams/:id', async () => {
            mockDelete.mockResolvedValue({})

            await teamApi.deleteTeam('1')

            expect(mockDelete).toHaveBeenCalledWith('/teams/1')
        })
    })

    describe('inviteMember', () => {
        it('should call POST /teams/:id/members', async () => {
            const inviteData = { email: 'test@example.com', role: 'MEMBER' as const }
            const mockMember = { id: '1', userNickname: 'TestUser', role: 'MEMBER' }
            mockPost.mockResolvedValue({ data: mockMember })

            const result = await teamApi.inviteMember('team1', inviteData)

            expect(mockPost).toHaveBeenCalledWith('/teams/team1/members', inviteData)
            expect(result).toEqual(mockMember)
        })
    })

    describe('removeMember', () => {
        it('should call DELETE /teams/:teamId/members/:memberId', async () => {
            mockDelete.mockResolvedValue({})

            await teamApi.removeMember('team1', 'member1')

            expect(mockDelete).toHaveBeenCalledWith('/teams/team1/members/member1')
        })
    })

    describe('updateMemberRole', () => {
        it('should call PATCH /teams/:teamId/members/:memberId', async () => {
            const mockMember = { id: 'member1', role: 'ADMIN' }
            mockPatch.mockResolvedValue({ data: mockMember })

            const result = await teamApi.updateMemberRole('team1', 'member1', 'ADMIN')

            expect(mockPatch).toHaveBeenCalledWith('/teams/team1/members/member1', { role: 'ADMIN' })
            expect(result).toEqual(mockMember)
        })
    })

    describe('leaveTeam', () => {
        it('should call POST /teams/:teamId/leave', async () => {
            mockPost.mockResolvedValue({})

            await teamApi.leaveTeam('team1')

            expect(mockPost).toHaveBeenCalledWith('/teams/team1/leave')
        })
    })
})
