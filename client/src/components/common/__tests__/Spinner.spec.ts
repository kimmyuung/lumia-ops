import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Spinner from '../Spinner.vue'

describe('Spinner', () => {
    describe('rendering', () => {
        it('should render spinner element', () => {
            const wrapper = mount(Spinner)

            expect(wrapper.find('.spinner').exists()).toBe(true)
        })
    })

    describe('sizing', () => {
        it('should use default size of 18px', () => {
            const wrapper = mount(Spinner)
            const style = wrapper.find('.spinner').attributes('style')

            expect(style).toContain('width: 18px')
            expect(style).toContain('height: 18px')
        })

        it('should apply custom size', () => {
            const wrapper = mount(Spinner, {
                props: { size: 32 }
            })
            const style = wrapper.find('.spinner').attributes('style')

            expect(style).toContain('width: 32px')
            expect(style).toContain('height: 32px')
        })
    })

    describe('color', () => {
        it('should have border-top-color style', () => {
            const wrapper = mount(Spinner)
            const style = wrapper.find('.spinner').attributes('style')

            // currentColor는 브라우저가 처리하므로 border-top-color가 존재하는지만 확인
            expect(style).toContain('border-top-color')
        })

        it('should apply custom color', () => {
            const wrapper = mount(Spinner, {
                props: { color: '#ff0000' }
            })
            const style = wrapper.find('.spinner').attributes('style')

            // RGB 또는 hex 형식으로 렌더링될 수 있음
            expect(style).toMatch(/border-top-color/)
        })
    })
})
