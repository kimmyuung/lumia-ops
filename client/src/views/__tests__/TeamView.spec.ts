import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import TeamView from '../TeamView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [{ path: '/', component: TeamView }]
})

describe('TeamView', () => {
    const mountComponent = () => {
        return mount(TeamView, {
            global: {
                plugins: [router],
                stubs: {
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
            expect(wrapper.text()).toContain('팀 관리')
        })

        it('should display page description', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.page-description').exists()).toBe(true)
        })
    })

    describe('empty state', () => {
        it('should render empty state', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.empty-state').exists()).toBe(true)
        })

        it('should display empty state message', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('아직 팀이 없습니다')
        })

        it('should have create team button', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('팀 생성')
        })

        it('should have join team option', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('초대 코드')
        })
    })

    describe('actions', () => {
        it('should have action buttons in empty state', () => {
            const wrapper = mountComponent()

            const buttons = wrapper.findAll('.btn')
            expect(buttons.length).toBeGreaterThanOrEqual(2)
        })
    })
})
