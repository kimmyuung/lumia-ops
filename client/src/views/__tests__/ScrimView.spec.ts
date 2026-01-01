import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import ScrimView from '../ScrimView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [{ path: '/', component: ScrimView }]
})

describe('ScrimView', () => {
    const mountComponent = () => {
        return mount(ScrimView, {
            global: {
                plugins: [router],
                stubs: {
                    Card: {
                        template: '<div class="card"><slot /></div>',
                        props: ['class']
                    },
                    Button: {
                        template: '<button class="btn"><slot /></button>',
                        props: ['variant']
                    }
                }
            }
        })
    }

    describe('rendering', () => {
        it('should render page header', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.page-header').exists()).toBe(true)
        })

        it('should display page title', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('h1').exists()).toBe(true)
            expect(wrapper.text()).toContain('스크림 관리')
        })

        it('should display page description', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.page-description').exists()).toBe(true)
        })
    })

    describe('stats section', () => {
        it('should render stats section', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.stats-section').exists()).toBe(true)
        })

        it('should display scheduled scrims stat', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('예정된 스크림')
        })

        it('should display completed scrims stat', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('완료된 스크림')
        })

        it('should display average rank stat', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('평균 순위')
        })
    })

    describe('empty state', () => {
        it('should render empty state', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.empty-state').exists()).toBe(true)
        })

        it('should display empty state message', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('스크림 기록이 없습니다')
        })

        it('should have create scrim button', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('스크림 생성')
        })
    })

    describe('actions', () => {
        it('should have create scrim button in header', () => {
            const wrapper = mountComponent()

            const headerButton = wrapper.find('.page-header .btn')
            expect(headerButton.exists()).toBe(true)
        })
    })
})
