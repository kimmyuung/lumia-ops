import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../HomeView.vue'

// 라우터 모킹
const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', component: HomeView },
        { path: '/team', component: { template: '<div>Team</div>' } },
        { path: '/strategy', component: { template: '<div>Strategy</div>' } }
    ]
})

describe('HomeView', () => {
    const mountComponent = () => {
        return mount(HomeView, {
            global: {
                plugins: [router],
                stubs: {
                    Card: {
                        template: '<div class="card"><slot /></div>',
                        props: ['hoverable']
                    }
                }
            }
        })
    }

    describe('rendering', () => {
        it('should render hero section', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.hero').exists()).toBe(true)
        })

        it('should display main title', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('h1').exists()).toBe(true)
            expect(wrapper.text()).toContain('Lumia Ops')
        })

        it('should display tagline', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.tagline').exists()).toBe(true)
            expect(wrapper.text()).toContain('이터널 리턴')
        })
    })

    describe('navigation', () => {
        it('should have CTA buttons', () => {
            const wrapper = mountComponent()

            const ctaButtons = wrapper.find('.cta-buttons')
            expect(ctaButtons.exists()).toBe(true)
        })

        it('should have link to team page', () => {
            const wrapper = mountComponent()

            // Button 컴포넌트는 to prop으로 router-link를 렌더링함
            const ctaButtons = wrapper.find('.cta-buttons')
            expect(ctaButtons.text()).toContain('팀 관리')
        })

        it('should have link to strategy page', () => {
            const wrapper = mountComponent()

            // Button 컴포넌트는 to prop으로 router-link를 렌더링함
            const ctaButtons = wrapper.find('.cta-buttons')
            expect(ctaButtons.text()).toContain('전략 보드')
        })
    })

    describe('features section', () => {
        it('should render features section', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.features').exists()).toBe(true)
        })

        it('should display feature cards', () => {
            const wrapper = mountComponent()

            const cards = wrapper.findAll('.card')
            expect(cards.length).toBeGreaterThanOrEqual(3)
        })

        it('should display team management feature', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('팀 관리')
        })

        it('should display strategy feature', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('전략 수립')
        })

        it('should display scrim feature', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('스크림 기록')
        })
    })
})
