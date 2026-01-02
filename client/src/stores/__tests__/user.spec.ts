import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user'

// 토큰 유효성 검사 모킹
vi.mock('@/utils/token', () => ({
    isTokenExpired: () => false,
    getTokenRemainingTime: () => 3600000 // 1시간
}))

describe('useUserStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        localStorage.clear()
    })

    describe('initial state', () => {
        it('should have null user and token initially', () => {
            const store = useUserStore()
            expect(store.user).toBeNull()
            expect(store.token).toBeNull()
        })

        it('should not be logged in initially', () => {
            const store = useUserStore()
            expect(store.isLoggedIn).toBe(false)
        })

        it('should not have team initially', () => {
            const store = useUserStore()
            expect(store.hasTeam).toBe(false)
        })
    })

    describe('setUser', () => {
        it('should set user data', () => {
            const store = useUserStore()
            const mockUser = {
                id: '1',
                nickname: 'TestUser',
                email: 'test@example.com'
            }

            store.setUser(mockUser)
            store.setToken('valid-token') // 토큰도 설정해야 isLoggedIn이 true

            expect(store.user).toEqual(mockUser)
            expect(store.isLoggedIn).toBe(true)
        })

        it('should detect team membership', () => {
            const store = useUserStore()
            const mockUserWithTeam = {
                id: '1',
                nickname: 'TestUser',
                email: 'test@example.com',
                teamId: 'team-1'
            }

            store.setUser(mockUserWithTeam)

            expect(store.hasTeam).toBe(true)
        })
    })

    describe('setToken', () => {
        it('should set token and save to localStorage', () => {
            const store = useUserStore()

            store.setToken('test-token-123')

            expect(store.token).toBe('test-token-123')
            expect(localStorage.getItem('token')).toBe('test-token-123')
        })
    })

    describe('logout', () => {
        it('should clear user, token, and localStorage', () => {
            const store = useUserStore()
            store.setUser({ id: '1', nickname: 'Test', email: 'test@test.com' })
            store.setToken('test-token')

            store.logout()

            expect(store.user).toBeNull()
            expect(store.token).toBeNull()
            expect(store.isLoggedIn).toBe(false)
            expect(localStorage.getItem('token')).toBeNull()
        })
    })

    describe('loadToken', () => {
        it('should load token from localStorage', () => {
            localStorage.setItem('token', 'saved-token')
            const store = useUserStore()

            const result = store.loadToken()

            expect(result).toBe(true)
            expect(store.token).toBe('saved-token')
        })

        it('should not set token if localStorage is empty', () => {
            const store = useUserStore()

            const result = store.loadToken()

            expect(result).toBe(false)
            expect(store.token).toBeNull()
        })
    })

    describe('tempUser for nickname setup', () => {
        it('should set and clear tempUser', () => {
            const store = useUserStore()

            store.setTempUser({
                id: 1,
                email: 'test@example.com',
                status: 'PENDING_NICKNAME'
            })

            expect(store.tempUser).not.toBeNull()
            expect(store.needsNickname).toBe(true)

            // setUser를 호출하면 tempUser가 클리어됨
            store.setUser({ id: '1', nickname: 'Test', email: 'test@example.com' })

            expect(store.tempUser).toBeNull()
            expect(store.needsNickname).toBe(false)
        })
    })
})
