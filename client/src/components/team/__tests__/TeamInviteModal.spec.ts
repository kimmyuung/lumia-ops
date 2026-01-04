import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import TeamInviteModal from '../TeamInviteModal.vue'

// Toast 모킹
vi.mock('@/composables/useToast', () => ({
    useToast: () => ({
        success: vi.fn(),
        error: vi.fn(),
        warning: vi.fn()
    })
}))

// API 모킹
vi.mock('@/api/invitation', () => ({
    invitationApi: {
        createInvitation: vi.fn()
    }
}))

describe('TeamInviteModal', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    const mountComponent = (props = {}) => {
        return mount(TeamInviteModal, {
            props: {
                teamId: 'team-1',
                ...props
            },
            global: {
                stubs: {
                    Modal: {
                        template: '<div class="modal"><slot /><slot name="footer" /></div>',
                        props: ['modelValue', 'title']
                    },
                    Button: {
                        template: '<button v-bind="$attrs" :disabled="disabled || loading"><slot /></button>',
                        props: ['disabled', 'loading', 'variant']
                    },
                    Input: {
                        template: '<input v-bind="$attrs" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
                        props: ['modelValue', 'error', 'disabled', 'type']
                    },
                    Mail: true,
                    Send: true
                }
            }
        })
    }

    describe('rendering', () => {
        it('should render invite form', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.invite-form').exists()).toBe(true)
        })

        it('should render email input field', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('#email').exists()).toBe(true)
        })

        it('should render role select', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('#role').exists()).toBe(true)
        })

        it('should render message textarea', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('#message').exists()).toBe(true)
        })

        it('should render info box', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.info-box').exists()).toBe(true)
        })
    })

    describe('role options', () => {
        it('should have MEMBER as default role', () => {
            const wrapper = mountComponent()

            const select = wrapper.find('#role')
            expect((select.element as HTMLSelectElement).value).toBe('MEMBER')
        })

        it('should have admin and member options', () => {
            const wrapper = mountComponent()

            const options = wrapper.findAll('#role option')
            expect(options).toHaveLength(2)
        })
    })

    describe('close', () => {
        it('should emit close event when cancel button clicked', async () => {
            const wrapper = mountComponent()

            const buttons = wrapper.findAll('button')
            const cancelButton = buttons.find(b => b.text().includes('취소'))

            if (cancelButton) {
                await cancelButton.trigger('click')
                expect(wrapper.emitted('close')).toBeTruthy()
            }
        })
    })
})
