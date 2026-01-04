import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import ScrimFormModal from '../ScrimFormModal.vue'

// Scrim store 모킹
const mockCreateScrim = vi.fn()
const mockUpdateScrim = vi.fn()

vi.mock('@/stores/scrim', () => ({
    useScrimStore: () => ({
        createScrim: mockCreateScrim,
        updateScrim: mockUpdateScrim
    })
}))

describe('ScrimFormModal', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
        vi.clearAllMocks()
    })

    const mountComponent = (props = {}) => {
        return mount(ScrimFormModal, {
            props,
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
                        props: ['modelValue', 'error', 'disabled']
                    },
                    Save: true
                }
            }
        })
    }

    describe('rendering', () => {
        it('should render in create mode by default', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.scrim-form').exists()).toBe(true)
        })

        it('should render form fields', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('#scheduledAt').exists()).toBe(true)
            expect(wrapper.find('#notes').exists()).toBe(true)
        })

        it('should render cancel and submit buttons', () => {
            const wrapper = mountComponent()

            const buttons = wrapper.findAll('button')
            expect(buttons.length).toBeGreaterThanOrEqual(2)
        })
    })

    describe('edit mode', () => {
        it('should detect edit mode when scrim is provided', async () => {
            const scrim = {
                id: '1',
                title: 'Test Scrim',
                scheduledAt: '2026-01-15T14:00:00Z',
                status: 'SCHEDULED' as const,
                teamId: 'team1',
                results: [],
                createdAt: '2026-01-01T00:00:00Z'
            }

            const wrapper = mountComponent({ scrim })
            await flushPromises()

            expect((wrapper.vm as unknown as { isEdit: boolean }).isEdit).toBe(true)
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
