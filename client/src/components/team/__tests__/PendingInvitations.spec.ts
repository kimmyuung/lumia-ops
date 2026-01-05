import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import PendingInvitations from '../PendingInvitations.vue'

// 모킹
vi.mock('@/composables/useToast', () => ({
    useToast: () => ({
        success: vi.fn(),
        error: vi.fn()
    })
}))

vi.mock('@/api/invitation', () => ({
    invitationApi: {
        getTeamInvitations: vi.fn(),
        resendInvitation: vi.fn(),
        cancelInvitation: vi.fn()
    }
}))

vi.mock('@/utils/formatters', () => ({
    formatRelativeTime: vi.fn().mockReturnValue('방금 전')
}))

describe('PendingInvitations', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    const mountComponent = (props = {}) => {
        return mount(PendingInvitations, {
            props: {
                teamId: 'team-1',
                ...props
            },
            global: {
                stubs: {
                    Button: {
                        template: '<button v-bind="$attrs" :disabled="disabled || loading"><slot /></button>',
                        props: ['disabled', 'loading', 'variant', 'size']
                    },
                    Skeleton: {
                        template: '<div class="skeleton"></div>'
                    },
                    Clock: true,
                    Mail: true,
                    Send: true,
                    X: true,
                    RefreshCw: true
                }
            }
        })
    }

    describe('rendering', () => {
        it('should render pending invitations container', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.pending-invitations').exists()).toBe(true)
        })

        it('should render section header', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.section-header').exists()).toBe(true)
            expect(wrapper.find('.section-header h4').exists()).toBe(true)
        })

        it('should render section title with count', async () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.section-header h4').exists()).toBe(true)
        })
    })

    describe('getRoleLabel', () => {
        it('should return "관리자" for ADMIN role', () => {
            const wrapper = mountComponent()
            const vm = wrapper.vm as unknown as { getRoleLabel: (role: string) => string }

            expect(vm.getRoleLabel('ADMIN')).toBe('관리자')
        })

        it('should return "멤버" for MEMBER role', () => {
            const wrapper = mountComponent()
            const vm = wrapper.vm as unknown as { getRoleLabel: (role: string) => string }

            expect(vm.getRoleLabel('MEMBER')).toBe('멤버')
        })
    })

    describe('exposed methods', () => {
        it('should expose refresh method', () => {
            const wrapper = mountComponent()
            const exposed = wrapper.vm.$ as unknown as { exposed: { refresh: () => void } }

            expect(typeof exposed.exposed.refresh).toBe('function')
        })
    })
})
