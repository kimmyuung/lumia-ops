import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { userApi } from '../user'
import apiClient from '../client'

vi.mock('../client', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn()
    }
}))

describe('userApi', () => {
    const mockGet = vi.mocked(apiClient.get)
    const mockPost = vi.mocked(apiClient.post)
    const mockPut = vi.mocked(apiClient.put)

    beforeEach(() => {
        vi.clearAllMocks()
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    describe('getMyInfo', () => {
        it('should call GET /users/me', async () => {
            const mockUser = {
                id: 1,
                email: 'test@example.com',
                nickname: 'TestUser',
                status: 'ACTIVE'
            }
            mockGet.mockResolvedValue({ data: mockUser })

            const result = await userApi.getMyInfo()

            expect(mockGet).toHaveBeenCalledWith('/users/me')
            expect(result).toEqual(mockUser)
        })
    })

    describe('setInitialNickname', () => {
        it('should call POST /users/me/nickname', async () => {
            const mockUser = {
                id: 1,
                email: 'test@example.com',
                nickname: 'NewNick',
                status: 'ACTIVE'
            }
            mockPost.mockResolvedValue({ data: mockUser })

            const result = await userApi.setInitialNickname('NewNick')

            expect(mockPost).toHaveBeenCalledWith('/users/me/nickname', { nickname: 'NewNick' })
            expect(result).toEqual(mockUser)
        })
    })

    describe('updateNickname', () => {
        it('should call PUT /users/me/nickname', async () => {
            const mockUser = {
                id: 1,
                email: 'test@example.com',
                nickname: 'UpdatedNick',
                status: 'ACTIVE'
            }
            mockPut.mockResolvedValue({ data: mockUser })

            const result = await userApi.updateNickname('UpdatedNick')

            expect(mockPut).toHaveBeenCalledWith('/users/me/nickname', { nickname: 'UpdatedNick' })
            expect(result).toEqual(mockUser)
        })
    })

    describe('getNicknameRemainingDays', () => {
        it('should call GET /users/me/nickname/remaining-days', async () => {
            const mockResponse = { daysRemaining: 15 }
            mockGet.mockResolvedValue({ data: mockResponse })

            const result = await userApi.getNicknameRemainingDays()

            expect(mockGet).toHaveBeenCalledWith('/users/me/nickname/remaining-days')
            expect(result).toEqual(mockResponse)
        })

        it('should return 0 days when user can change nickname', async () => {
            const mockResponse = { daysRemaining: 0 }
            mockGet.mockResolvedValue({ data: mockResponse })

            const result = await userApi.getNicknameRemainingDays()

            expect(result.daysRemaining).toBe(0)
        })
    })
})
