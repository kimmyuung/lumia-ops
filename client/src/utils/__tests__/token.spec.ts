import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { decodeJwt, isTokenExpired, getTokenRemainingTime, getTokenUserId } from '../token'

describe('token utils', () => {
    // 테스트용 JWT 토큰 생성 헬퍼
    const createMockToken = (payload: object): string => {
        const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
        const payloadEncoded = btoa(JSON.stringify(payload))
        const signature = 'test-signature'
        return `${header}.${payloadEncoded}.${signature}`
    }

    describe('decodeJwt', () => {
        it('should decode a valid JWT token', () => {
            const payload = { sub: 'user123', exp: 1234567890, iat: 1234567800 }
            const token = createMockToken(payload)

            const result = decodeJwt(token)

            expect(result?.sub).toBe('user123')
            expect(result?.exp).toBe(1234567890)
        })

        it('should return null for invalid token format', () => {
            expect(decodeJwt('invalid-token')).toBeNull()
            expect(decodeJwt('only.two')).toBeNull()
            expect(decodeJwt('')).toBeNull()
        })

        it('should return null for token with invalid base64', () => {
            const result = decodeJwt('header.!!!invalid!!!.signature')
            expect(result).toBeNull()
        })
    })

    describe('isTokenExpired', () => {
        beforeEach(() => {
            vi.useFakeTimers()
        })

        afterEach(() => {
            vi.useRealTimers()
        })

        it('should return false for non-expired token', () => {
            const futureExp = Math.floor(Date.now() / 1000) + 3600 // 1시간 후
            const token = createMockToken({ sub: 'user', exp: futureExp, iat: Date.now() / 1000 })

            expect(isTokenExpired(token)).toBe(false)
        })

        it('should return true for expired token', () => {
            const pastExp = Math.floor(Date.now() / 1000) - 3600 // 1시간 전
            const token = createMockToken({ sub: 'user', exp: pastExp, iat: Date.now() / 1000 })

            expect(isTokenExpired(token)).toBe(true)
        })

        it('should consider buffer seconds', () => {
            const exp = Math.floor(Date.now() / 1000) + 30 // 30초 후
            const token = createMockToken({ sub: 'user', exp, iat: Date.now() / 1000 })

            // 기본 버퍼 60초이므로 만료로 판단
            expect(isTokenExpired(token)).toBe(true)

            // 버퍼 10초로 설정하면 아직 유효
            expect(isTokenExpired(token, 10)).toBe(false)
        })

        it('should return true for invalid token', () => {
            expect(isTokenExpired('invalid-token')).toBe(true)
        })
    })

    describe('getTokenRemainingTime', () => {
        beforeEach(() => {
            vi.useFakeTimers()
            vi.setSystemTime(new Date('2026-01-01T00:00:00Z'))
        })

        afterEach(() => {
            vi.useRealTimers()
        })

        it('should return remaining time in milliseconds', () => {
            const exp = Math.floor(Date.now() / 1000) + 3600 // 1시간 후
            const token = createMockToken({ sub: 'user', exp, iat: Date.now() / 1000 })

            const remaining = getTokenRemainingTime(token)

            // 약 3600 * 1000 = 3,600,000ms
            expect(remaining).toBeGreaterThan(3500000)
            expect(remaining).toBeLessThanOrEqual(3600000)
        })

        it('should return 0 for expired token', () => {
            const pastExp = Math.floor(Date.now() / 1000) - 3600
            const token = createMockToken({ sub: 'user', exp: pastExp, iat: Date.now() / 1000 })

            expect(getTokenRemainingTime(token)).toBe(0)
        })

        it('should return 0 for invalid token', () => {
            expect(getTokenRemainingTime('invalid-token')).toBe(0)
        })
    })

    describe('getTokenUserId', () => {
        it('should extract user ID from token', () => {
            const token = createMockToken({ sub: 'user-123', exp: 9999999999, iat: 1000000000 })

            expect(getTokenUserId(token)).toBe('user-123')
        })

        it('should return null for invalid token', () => {
            expect(getTokenUserId('invalid-token')).toBeNull()
        })

        it('should return null for token without sub claim', () => {
            const token = createMockToken({ exp: 9999999999, iat: 1000000000 })

            expect(getTokenUserId(token)).toBeNull()
        })
    })
})
