import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ToastItem from '../ToastItem.vue'

describe('ToastItem', () => {
    const createToast = (type: 'success' | 'error' | 'warning' | 'info', message: string) => ({
        id: 1,
        type,
        message,
        duration: 3000
    })

    const mountComponent = (toast: ReturnType<typeof createToast>) => {
        return mount(ToastItem, {
            props: { toast },
            global: {
                stubs: {
                    CheckCircle: { template: '<span class="icon-check"></span>' },
                    XCircle: { template: '<span class="icon-x-circle"></span>' },
                    AlertTriangle: { template: '<span class="icon-alert"></span>' },
                    Info: { template: '<span class="icon-info"></span>' },
                    X: { template: '<span class="icon-close"></span>' }
                }
            }
        })
    }

    describe('rendering', () => {
        it('should render toast message', () => {
            const toast = createToast('success', '성공 메시지')
            const wrapper = mountComponent(toast)

            expect(wrapper.text()).toContain('성공 메시지')
        })

        it('should apply success class for success type', () => {
            const toast = createToast('success', '성공')
            const wrapper = mountComponent(toast)

            expect(wrapper.find('.toast-item').classes()).toContain('toast-success')
        })

        it('should apply error class for error type', () => {
            const toast = createToast('error', '에러')
            const wrapper = mountComponent(toast)

            expect(wrapper.find('.toast-item').classes()).toContain('toast-error')
        })

        it('should apply warning class for warning type', () => {
            const toast = createToast('warning', '경고')
            const wrapper = mountComponent(toast)

            expect(wrapper.find('.toast-item').classes()).toContain('toast-warning')
        })

        it('should apply info class for info type', () => {
            const toast = createToast('info', '정보')
            const wrapper = mountComponent(toast)

            expect(wrapper.find('.toast-item').classes()).toContain('toast-info')
        })

        it('should render close button', () => {
            const toast = createToast('success', '메시지')
            const wrapper = mountComponent(toast)

            expect(wrapper.find('.toast-close').exists()).toBe(true)
        })
    })

    describe('interactions', () => {
        it('should emit close event when close button is clicked', async () => {
            const toast = createToast('success', '메시지')
            const wrapper = mountComponent(toast)

            await wrapper.find('.toast-close').trigger('click')

            expect(wrapper.emitted('close')).toBeTruthy()
        })
    })
})
