import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'

describe('useTheme', () => {
    const THEME_STORAGE_KEY = 'lumia-theme'

    beforeEach(() => {
        localStorage.clear()
        vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({
            matches: false,
            addEventListener: vi.fn(),
            removeEventListener: vi.fn()
        }))
        vi.spyOn(document.documentElement, 'setAttribute')
    })

    afterEach(() => {
        vi.restoreAllMocks()
        vi.resetModules()
    })

    describe('setTheme', () => {
        it('should set theme to light and save to localStorage', async () => {
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            theme.setTheme('light')

            expect(theme.currentTheme.value).toBe('light')
            expect(theme.resolvedTheme.value).toBe('light')
            expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('light')
        })

        it('should set theme to dark and save to localStorage', async () => {
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            theme.setTheme('dark')

            expect(theme.currentTheme.value).toBe('dark')
            expect(theme.resolvedTheme.value).toBe('dark')
            expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('dark')
        })

        it('should apply data-theme attribute to document', async () => {
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            theme.setTheme('dark')

            expect(document.documentElement.setAttribute).toHaveBeenCalledWith('data-theme', 'dark')
        })
    })

    describe('toggleTheme', () => {
        it('should toggle from light to dark', async () => {
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            theme.setTheme('light')
            theme.toggleTheme()

            expect(theme.currentTheme.value).toBe('dark')
        })

        it('should toggle from dark to light', async () => {
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            theme.setTheme('dark')
            theme.toggleTheme()

            expect(theme.currentTheme.value).toBe('light')
        })
    })

    describe('loadTheme', () => {
        it('should load theme from localStorage', async () => {
            localStorage.setItem(THEME_STORAGE_KEY, 'dark')
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            theme.loadTheme()

            expect(theme.currentTheme.value).toBe('dark')
        })

        it('should ignore invalid saved values', async () => {
            localStorage.setItem(THEME_STORAGE_KEY, 'invalid-theme')
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            // 현재 상태 저장 후 loadTheme 호출
            const prevTheme = theme.currentTheme.value
            theme.loadTheme()

            // 유효하지 않으면 변경되지 않아야 함
            expect(['light', 'dark', 'system']).toContain(theme.currentTheme.value)
        })
    })

    describe('resolvedTheme', () => {
        it('should resolve to light when system prefers light', async () => {
            vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({
                matches: false, // prefers light
                addEventListener: vi.fn(),
                removeEventListener: vi.fn()
            }))
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            theme.setTheme('system')
            theme.loadTheme()

            expect(theme.resolvedTheme.value).toBe('light')
        })

        it('should resolve to dark when system prefers dark', async () => {
            vi.stubGlobal('matchMedia', vi.fn().mockReturnValue({
                matches: true, // prefers dark
                addEventListener: vi.fn(),
                removeEventListener: vi.fn()
            }))
            vi.resetModules()
            const { useTheme } = await import('../useTheme')
            const theme = useTheme()

            theme.setTheme('system')
            theme.loadTheme()

            expect(theme.resolvedTheme.value).toBe('dark')
        })
    })
})
