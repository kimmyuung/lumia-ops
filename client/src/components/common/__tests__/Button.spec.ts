import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Button from '../Button.vue'

describe('Button', () => {
    describe('rendering', () => {
        it('should render slot content', () => {
            const wrapper = mount(Button, {
                slots: {
                    default: 'Click me'
                }
            })

            expect(wrapper.text()).toContain('Click me')
        })

        it('should apply default classes', () => {
            const wrapper = mount(Button)

            expect(wrapper.classes()).toContain('btn')
            expect(wrapper.classes()).toContain('btn-primary')
            expect(wrapper.classes()).toContain('btn-md')
        })
    })

    describe('variants', () => {
        it('should apply primary variant', () => {
            const wrapper = mount(Button, {
                props: { variant: 'primary' }
            })

            expect(wrapper.classes()).toContain('btn-primary')
        })

        it('should apply secondary variant', () => {
            const wrapper = mount(Button, {
                props: { variant: 'secondary' }
            })

            expect(wrapper.classes()).toContain('btn-secondary')
        })

        it('should apply ghost variant', () => {
            const wrapper = mount(Button, {
                props: { variant: 'ghost' }
            })

            expect(wrapper.classes()).toContain('btn-ghost')
        })

        it('should apply danger variant', () => {
            const wrapper = mount(Button, {
                props: { variant: 'danger' }
            })

            expect(wrapper.classes()).toContain('btn-danger')
        })
    })

    describe('sizes', () => {
        it('should apply sm size', () => {
            const wrapper = mount(Button, {
                props: { size: 'sm' }
            })

            expect(wrapper.classes()).toContain('btn-sm')
        })

        it('should apply lg size', () => {
            const wrapper = mount(Button, {
                props: { size: 'lg' }
            })

            expect(wrapper.classes()).toContain('btn-lg')
        })
    })

    describe('states', () => {
        it('should be disabled when disabled prop is true', () => {
            const wrapper = mount(Button, {
                props: { disabled: true }
            })

            expect(wrapper.attributes('disabled')).toBeDefined()
        })

        it('should be disabled when loading', () => {
            const wrapper = mount(Button, {
                props: { loading: true }
            })

            expect(wrapper.attributes('disabled')).toBeDefined()
            expect(wrapper.classes()).toContain('btn-loading')
        })

        it('should show spinner when loading', () => {
            const wrapper = mount(Button, {
                props: { loading: true }
            })

            expect(wrapper.find('.spinner').exists()).toBe(true)
        })
    })

    describe('events', () => {
        it('should emit click event', async () => {
            const wrapper = mount(Button)

            await wrapper.trigger('click')

            expect(wrapper.emitted('click')).toBeTruthy()
        })

        it('should not emit click when disabled', async () => {
            const wrapper = mount(Button, {
                props: { disabled: true }
            })

            await wrapper.trigger('click')

            // disabled 버튼은 클릭 이벤트를 발생시키지 않음
            expect(wrapper.emitted('click')).toBeFalsy()
        })
    })
})
