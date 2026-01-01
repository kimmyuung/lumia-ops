import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { useToast } from '../useToast'

describe('useToast', () => {
    let toast: ReturnType<typeof useToast>

    beforeEach(() => {
        vi.useFakeTimers()
        toast = useToast()
        // 이전 테스트에서 남은 토스트 제거
        while (toast.toasts.value.length > 0) {
            toast.remove(toast.toasts.value[0].id)
        }
    })

    afterEach(() => {
        vi.useRealTimers()
    })

    describe('show', () => {
        it('should add a toast to the list', () => {
            toast.show('Test message', 'info', 0)

            expect(toast.toasts.value.length).toBe(1)
            expect(toast.toasts.value[0].message).toBe('Test message')
            expect(toast.toasts.value[0].type).toBe('info')
        })

        it('should return toast id', () => {
            const id = toast.show('Test message', 'info', 0)

            expect(typeof id).toBe('number')
        })
    })

    describe('helper methods', () => {
        it('should add success toast', () => {
            toast.success('Success!', 0)

            const lastToast = toast.toasts.value[toast.toasts.value.length - 1]
            expect(lastToast.type).toBe('success')
            expect(lastToast.message).toBe('Success!')
        })

        it('should add error toast', () => {
            toast.error('Error!', 0)

            const lastToast = toast.toasts.value[toast.toasts.value.length - 1]
            expect(lastToast.type).toBe('error')
        })

        it('should add warning toast', () => {
            toast.warning('Warning!', 0)

            const lastToast = toast.toasts.value[toast.toasts.value.length - 1]
            expect(lastToast.type).toBe('warning')
        })

        it('should add info toast', () => {
            toast.info('Info!', 0)

            const lastToast = toast.toasts.value[toast.toasts.value.length - 1]
            expect(lastToast.type).toBe('info')
        })
    })

    describe('remove', () => {
        it('should remove toast by id', () => {
            const id = toast.show('Test', 'info', 0)
            const initialLength = toast.toasts.value.length

            toast.remove(id)

            expect(toast.toasts.value.length).toBe(initialLength - 1)
        })
    })

    describe('auto-remove', () => {
        it('should auto-remove toast after duration', () => {
            const initialLength = toast.toasts.value.length
            toast.show('Test', 'info', 3000)

            expect(toast.toasts.value.length).toBe(initialLength + 1)

            vi.advanceTimersByTime(3000)

            expect(toast.toasts.value.length).toBe(initialLength)
        })
    })
})
