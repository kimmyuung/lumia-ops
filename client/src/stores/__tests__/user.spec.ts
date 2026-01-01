import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../user'

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

            store.loadToken()

            expect(store.token).toBe('saved-token')
        })

        it('should not set token if localStorage is empty', () => {
            const store = useUserStore()

            store.loadToken()

            expect(store.token).toBeNull()
        })
    })
})
