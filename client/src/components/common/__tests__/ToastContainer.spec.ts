import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ToastContainer from '../ToastContainer.vue'

// useToast 모킹
const mockToasts = [
    { id: 1, type: 'success', message: 'Success message', duration: 3000 },
    { id: 2, type: 'error', message: 'Error message', duration: 3000 }
]
const mockRemove = vi.fn()

vi.mock('@/composables/useToast', () => ({
    useToast: () => ({
        toasts: mockToasts,
        remove: mockRemove
    })
}))

describe('ToastContainer', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    const mountComponent = () => {
        return mount(ToastContainer, {
            global: {
                stubs: {
                    Teleport: true,
                    TransitionGroup: {
                        template: '<div><slot /></div>'
                    },
                    ToastItem: {
                        template: '<div class="toast-item" @click="$emit(\'close\')">{{ toast.message }}</div>',
                        props: ['toast'],
                        emits: ['close']
                    }
                }
            }
        })
    }

    describe('rendering', () => {
        it('should render toast container', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.toast-container').exists()).toBe(true)
        })

        it('should render multiple toasts', () => {
            const wrapper = mountComponent()

            const toastItems = wrapper.findAll('.toast-item')
            expect(toastItems).toHaveLength(2)
        })

        it('should display toast messages', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('Success message')
            expect(wrapper.text()).toContain('Error message')
        })
    })

    describe('interactions', () => {
        it('should call remove when toast is closed', async () => {
            const wrapper = mountComponent()

            const firstToast = wrapper.find('.toast-item')
            await firstToast.trigger('click')

            expect(mockRemove).toHaveBeenCalledWith(1)
        })
    })
})
