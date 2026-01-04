import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { authApi } from '../auth'
import apiClient from '../client'

// apiClient 모킹
vi.mock('../client', () => ({
    default: {
        post: vi.fn()
    }
}))

describe('authApi', () => {
    const mockPost = vi.mocked(apiClient.post)

    beforeEach(() => {
        vi.clearAllMocks()
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    describe('register', () => {
        it('should call POST /auth/register with correct data', async () => {
            const mockResponse = { data: { success: true, message: '회원가입 완료' } }
            mockPost.mockResolvedValue(mockResponse)

            const result = await authApi.register({
                email: 'test@example.com',
                password: 'password123'
            })

            expect(mockPost).toHaveBeenCalledWith('/auth/register', {
                email: 'test@example.com',
                password: 'password123'
            })
            expect(result).toEqual(mockResponse.data)
        })
    })

    describe('login', () => {
        it('should call POST /auth/login with correct data', async () => {
            const mockResponse = {
                data: {
                    token: 'access-token-123',
                    refreshToken: 'refresh-token-456',
                    userId: 1,
                    email: 'test@example.com',
                    nickname: 'TestUser',
                    status: 'ACTIVE',
                    needsNickname: false
                }
            }
            mockPost.mockResolvedValue(mockResponse)

            const result = await authApi.login({
                email: 'test@example.com',
                password: 'password123'
            })

            expect(mockPost).toHaveBeenCalledWith('/auth/login', {
                email: 'test@example.com',
                password: 'password123'
            })
            expect(result).toEqual(mockResponse.data)
        })

        it('should return needsNickname=true for users without nickname', async () => {
            const mockResponse = {
                data: {
                    token: 'access-token-123',
                    refreshToken: 'refresh-token-456',
                    userId: 1,
                    email: 'test@example.com',
                    nickname: null,
                    status: 'PENDING_NICKNAME',
                    needsNickname: true
                }
            }
            mockPost.mockResolvedValue(mockResponse)

            const result = await authApi.login({
                email: 'test@example.com',
                password: 'password123'
            })

            expect(result.needsNickname).toBe(true)
        })
    })

    describe('verifyEmail', () => {
        it('should call POST /auth/verify-email with token', async () => {
            const mockResponse = { data: { success: true, message: '이메일 인증 완료' } }
            mockPost.mockResolvedValue(mockResponse)

            const result = await authApi.verifyEmail('verification-token-123')

            expect(mockPost).toHaveBeenCalledWith('/auth/verify-email', {
                token: 'verification-token-123'
            })
            expect(result).toEqual(mockResponse.data)
        })
    })

    describe('resendVerification', () => {
        it('should call POST /auth/resend-verification with email', async () => {
            const mockResponse = { data: { success: true, message: '인증 이메일 발송 완료' } }
            mockPost.mockResolvedValue(mockResponse)

            const result = await authApi.resendVerification('test@example.com')

            expect(mockPost).toHaveBeenCalledWith('/auth/resend-verification', {
                email: 'test@example.com'
            })
            expect(result).toEqual(mockResponse.data)
        })
    })

    describe('findUsername', () => {
        it('should call POST /auth/find-username with email', async () => {
            const mockResponse = { data: { email: 'test@example.com', exists: true } }
            mockPost.mockResolvedValue(mockResponse)

            const result = await authApi.findUsername('test@example.com')

            expect(mockPost).toHaveBeenCalledWith('/auth/find-username', {
                email: 'test@example.com'
            })
            expect(result).toEqual(mockResponse.data)
        })

        it('should return exists=false when user not found', async () => {
            const mockResponse = { data: { email: null, exists: false } }
            mockPost.mockResolvedValue(mockResponse)

            const result = await authApi.findUsername('notfound@example.com')

            expect(result.exists).toBe(false)
        })
    })

    describe('logout', () => {
        it('should call POST /auth/logout', async () => {
            mockPost.mockResolvedValue({})

            await authApi.logout()

            expect(mockPost).toHaveBeenCalledWith('/auth/logout')
        })
    })

    describe('refreshToken', () => {
        it('should call POST /auth/refresh with refreshToken', async () => {
            const mockResponse = {
                data: {
                    token: 'new-access-token',
                    refreshToken: 'new-refresh-token'
                }
            }
            mockPost.mockResolvedValue(mockResponse)

            const result = await authApi.refreshToken('old-refresh-token')

            expect(mockPost).toHaveBeenCalledWith('/auth/refresh', {
                refreshToken: 'old-refresh-token'
            })
            expect(result).toEqual(mockResponse.data)
        })
    })
})
