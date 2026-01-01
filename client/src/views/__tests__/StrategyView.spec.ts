import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import StrategyView from '../StrategyView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [{ path: '/', component: StrategyView }]
})

describe('StrategyView', () => {
    const mountComponent = () => {
        return mount(StrategyView, {
            global: {
                plugins: [router],
                stubs: {
                    Card: {
                        template: '<div class="card"><slot /></div>',
                        props: ['hoverable']
                    },
                    Button: {
                        template: '<button class="btn"><slot /></button>',
                        props: ['variant', 'disabled']
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
            expect(wrapper.text()).toContain('전략 수립 보드')
        })

        it('should display page description', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.page-description').exists()).toBe(true)
        })
    })

    describe('tactical map', () => {
        it('should render map section', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.map-section').exists()).toBe(true)
        })

        it('should render tactical map container', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.tactical-map').exists()).toBe(true)
        })

        it('should display map placeholder', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.map-placeholder').exists()).toBe(true)
            expect(wrapper.text()).toContain('택티컬 맵')
        })
    })

    describe('tools section', () => {
        it('should render tools section', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.tools-section').exists()).toBe(true)
        })

        it('should display strategy tools', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('전략 도구')
        })

        it('should have marker tool', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('마커')
        })

        it('should have route tool', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('경로')
        })

        it('should have memo tool', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('메모')
        })
    })
})
