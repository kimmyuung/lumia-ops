import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Card from '../Card.vue'

describe('Card', () => {
    describe('rendering', () => {
        it('should render default slot content', () => {
            const wrapper = mount(Card, {
                slots: {
                    default: 'Card content'
                }
            })

            expect(wrapper.text()).toContain('Card content')
        })

        it('should have card class', () => {
            const wrapper = mount(Card)

            expect(wrapper.classes()).toContain('card')
        })
    })

    describe('slots', () => {
        it('should render header slot', () => {
            const wrapper = mount(Card, {
                slots: {
                    header: 'Header content',
                    default: 'Body content'
                }
            })

            expect(wrapper.find('.card-header').exists()).toBe(true)
            expect(wrapper.find('.card-header').text()).toContain('Header content')
        })

        it('should render footer slot', () => {
            const wrapper = mount(Card, {
                slots: {
                    default: 'Body content',
                    footer: 'Footer content'
                }
            })

            expect(wrapper.find('.card-footer').exists()).toBe(true)
            expect(wrapper.find('.card-footer').text()).toContain('Footer content')
        })

        it('should not render header if slot is not provided', () => {
            const wrapper = mount(Card, {
                slots: {
                    default: 'Body content'
                }
            })

            expect(wrapper.find('.card-header').exists()).toBe(false)
        })

        it('should not render footer if slot is not provided', () => {
            const wrapper = mount(Card, {
                slots: {
                    default: 'Body content'
                }
            })

            expect(wrapper.find('.card-footer').exists()).toBe(false)
        })
    })

    describe('hoverable', () => {
        it('should apply hoverable class when prop is true', () => {
            const wrapper = mount(Card, {
                props: { hoverable: true }
            })

            expect(wrapper.classes()).toContain('card-hoverable')
        })

        it('should not apply hoverable class by default', () => {
            const wrapper = mount(Card)

            expect(wrapper.classes()).not.toContain('card-hoverable')
        })
    })
})
