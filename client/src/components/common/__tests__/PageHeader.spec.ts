import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PageHeader from '../PageHeader.vue'
import { defineComponent, h } from 'vue'

// 아이콘 stub 컴포넌트
const IconStub = defineComponent({
    props: ['size'],
    render() {
        return h('span', { class: 'icon-stub' })
    }
})

describe('PageHeader', () => {
    const mountComponent = (props = {}, slots = {}) => {
        return mount(PageHeader, {
            props: {
                title: '테스트 제목',
                icon: IconStub,
                ...props
            },
            slots
        })
    }

    describe('rendering', () => {
        it('should render title', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('h1').text()).toContain('테스트 제목')
        })

        it('should render icon', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.icon-stub').exists()).toBe(true)
        })

        it('should render description when provided', () => {
            const wrapper = mountComponent({ description: '페이지 설명' })

            expect(wrapper.find('.page-description').text()).toBe('페이지 설명')
        })

        it('should not render description when not provided', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.page-description').exists()).toBe(false)
        })
    })

    describe('slots', () => {
        it('should render actions slot', () => {
            const wrapper = mountComponent({}, {
                actions: '<button>액션 버튼</button>'
            })

            expect(wrapper.find('.header-actions').exists()).toBe(true)
            expect(wrapper.text()).toContain('액션 버튼')
        })

        it('should not render actions container when slot is empty', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.header-actions').exists()).toBe(false)
        })
    })
})
