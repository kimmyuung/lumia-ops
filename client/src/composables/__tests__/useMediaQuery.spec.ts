import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick } from 'vue'
import { useMediaQuery, useIsMobile } from '../useMediaQuery'

describe('useMediaQuery', () => {
    let mockAddEventListener: ReturnType<typeof vi.fn>
    let mockRemoveEventListener: ReturnType<typeof vi.fn>
    let changeHandler: ((e: MediaQueryListEvent) => void) | null = null

    const createMockMatchMedia = (matches: boolean) => {
        mockAddEventListener = vi.fn((event, handler) => {
            if (event === 'change') {
                changeHandler = handler
            }
        })
        mockRemoveEventListener = vi.fn()

        return vi.fn(() => ({
            matches,
            addEventListener: mockAddEventListener,
            removeEventListener: mockRemoveEventListener
        })) as unknown as typeof window.matchMedia
    }

    beforeEach(() => {
        changeHandler = null
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    describe('useMediaQuery basic functionality', () => {
        it('should call matchMedia with the provided query', () => {
            const mockMedia = createMockMatchMedia(false)
            window.matchMedia = mockMedia

            const TestComponent = defineComponent({
                setup() {
                    const matches = useMediaQuery('(max-width: 1024px)')
                    return { matches }
                },
                template: '<div>{{ matches }}</div>'
            })

            mount(TestComponent)
            expect(mockMedia).toHaveBeenCalledWith('(max-width: 1024px)')
        })

        it('should add event listener on mount', () => {
            window.matchMedia = createMockMatchMedia(false)

            const TestComponent = defineComponent({
                setup() {
                    const matches = useMediaQuery('(min-width: 768px)')
                    return { matches }
                },
                template: '<div>{{ matches }}</div>'
            })

            mount(TestComponent)
            expect(mockAddEventListener).toHaveBeenCalledWith('change', expect.any(Function))
        })

        it('should remove event listener on unmount', () => {
            window.matchMedia = createMockMatchMedia(false)

            const TestComponent = defineComponent({
                setup() {
                    const matches = useMediaQuery('(min-width: 768px)')
                    return { matches }
                },
                template: '<div>{{ matches }}</div>'
            })

            const wrapper = mount(TestComponent)
            wrapper.unmount()
            expect(mockRemoveEventListener).toHaveBeenCalledWith('change', expect.any(Function))
        })

        it('should update matches when media query changes', async () => {
            window.matchMedia = createMockMatchMedia(false)

            const TestComponent = defineComponent({
                setup() {
                    const matches = useMediaQuery('(min-width: 768px)')
                    return { matches }
                },
                template: '<div>{{ matches }}</div>'
            })

            const wrapper = mount(TestComponent)

            // 미디어 쿼리 변경 시뮬레이션
            if (changeHandler) {
                changeHandler({ matches: true } as MediaQueryListEvent)
            }

            await nextTick()
            expect(wrapper.text()).toContain('true')
        })
    })

    describe('useIsMobile', () => {
        it('should use correct mobile breakpoint query', () => {
            const mockMedia = createMockMatchMedia(false)
            window.matchMedia = mockMedia

            const TestComponent = defineComponent({
                setup() {
                    const isMobile = useIsMobile()
                    return { isMobile }
                },
                template: '<div>{{ isMobile }}</div>'
            })

            mount(TestComponent)
            expect(mockMedia).toHaveBeenCalledWith('(max-width: 768px)')
        })
    })
})
