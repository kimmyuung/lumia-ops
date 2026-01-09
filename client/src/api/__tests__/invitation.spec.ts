import { describe, it, expect, vi, beforeEach } from 'vitest'
import { invitationApi } from '../invitation'
import apiClient from '../client'

vi.mock('../client', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        delete: vi.fn()
    }
}))

describe('invitationApi', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    describe('createInvitation', () => {
        it('should create invitation', async () => {
            const mockResponse = {
                data: {
                    id: 'inv-1',
                    email: 'invitee@example.com',
                    status: 'PENDING'
                }
            }
            vi.mocked(apiClient.post).mockResolvedValue(mockResponse)

            const result = await invitationApi.createInvitation('team-1', {
                email: 'invitee@example.com'
            })

            expect(apiClient.post).toHaveBeenCalledWith('/teams/team-1/invitations', {
                email: 'invitee@example.com'
            })
            expect(result).toEqual(mockResponse.data)
        })
    })

    describe('getTeamInvitations', () => {
        it('should get team invitations', async () => {
            const mockInvitations = [
                { id: 'inv-1', email: 'user1@example.com', status: 'PENDING' },
                { id: 'inv-2', email: 'user2@example.com', status: 'PENDING' }
            ]
            vi.mocked(apiClient.get).mockResolvedValue({ data: mockInvitations })

            const result = await invitationApi.getTeamInvitations('team-1')

            expect(apiClient.get).toHaveBeenCalledWith('/teams/team-1/invitations')
            expect(result).toEqual(mockInvitations)
        })
    })

    describe('cancelInvitation', () => {
        it('should cancel invitation', async () => {
            vi.mocked(apiClient.delete).mockResolvedValue({ data: {} })

            await invitationApi.cancelInvitation('team-1', 'inv-1')

            expect(apiClient.delete).toHaveBeenCalledWith('/teams/team-1/invitations/inv-1')
        })
    })

    describe('resendInvitation', () => {
        it('should resend invitation', async () => {
            vi.mocked(apiClient.post).mockResolvedValue({ data: {} })

            await invitationApi.resendInvitation('team-1', 'inv-1')

            expect(apiClient.post).toHaveBeenCalledWith('/teams/team-1/invitations/inv-1/resend')
        })
    })

    describe('getMyInvitations', () => {
        it('should get pending invitations for current user', async () => {
            const mockInvitations = [
                { id: 'inv-1', teamName: 'Team A', status: 'PENDING' }
            ]
            vi.mocked(apiClient.get).mockResolvedValue({ data: mockInvitations })

            const result = await invitationApi.getMyInvitations()

            expect(apiClient.get).toHaveBeenCalledWith('/invitations/pending')
            expect(result).toEqual(mockInvitations)
        })
    })

    describe('getInvitationByToken', () => {
        it('should get invitation details by token', async () => {
            const mockInvitation = {
                id: 'inv-1',
                teamName: 'Team A',
                inviterName: 'John Doe',
                status: 'PENDING'
            }
            vi.mocked(apiClient.get).mockResolvedValue({ data: mockInvitation })

            const result = await invitationApi.getInvitationByToken('valid-token')

            expect(apiClient.get).toHaveBeenCalledWith('/invitations/valid-token')
            expect(result).toEqual(mockInvitation)
        })
    })

    describe('acceptInvitation', () => {
        it('should accept invitation', async () => {
            const mockResponse = { data: { teamId: 'team-1' } }
            vi.mocked(apiClient.post).mockResolvedValue(mockResponse)

            const result = await invitationApi.acceptInvitation('valid-token')

            expect(apiClient.post).toHaveBeenCalledWith('/invitations/valid-token/accept')
            expect(result).toEqual(mockResponse.data)
        })
    })

    describe('declineInvitation', () => {
        it('should decline invitation', async () => {
            vi.mocked(apiClient.post).mockResolvedValue({ data: {} })

            await invitationApi.declineInvitation('valid-token')

            expect(apiClient.post).toHaveBeenCalledWith('/invitations/valid-token/decline')
        })
    })
})
