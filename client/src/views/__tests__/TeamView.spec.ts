import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import TeamView from '../TeamView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [{ path: '/', component: TeamView }]
})

// Mock useTeamStore
vi.mock('@/stores/team', () => ({
    useTeamStore: () => ({
        currentTeam: ref(null),
        teams: ref([]),
        isLoading: ref(false),
        error: ref(null),
        hasTeam: ref(false),
        isOwner: ref(false),
        isAdmin: ref(false),
        memberCount: ref(0),
        fetchMyTeam: vi.fn(),
        fetchTeams: vi.fn(),
        createTeam: vi.fn(),
        updateTeam: vi.fn(),
        deleteTeam: vi.fn(),
        leaveTeam: vi.fn()
    })
}))

// Mock storeToRefs to return the same refs
vi.mock('pinia', async () => {
    const actual = await vi.importActual('pinia')
    return {
        ...actual,
        storeToRefs: (store: Record<string, unknown>) => store
    }
})

describe('TeamView', () => {
    beforeEach(() => {
        setActivePinia(createPinia())
    })

    const mountComponent = () => {
        return mount(TeamView, {
            global: {
                plugins: [router, createPinia()],
                stubs: {
                    PageHeader: {
                        template: '<div class="page-header"><h1>{{ title }}</h1><slot name="actions" /></div>',
                        props: ['title', 'description', 'icon']
                    },
                    Card: {
                        template: '<div class="card"><slot /></div>',
                        props: ['class', 'hoverable']
                    },
                    Button: {
                        template: '<button class="btn"><slot /></button>',
                        props: ['variant', 'size', 'loading', 'disabled']
                    },
                    SkeletonCard: {
                        template: '<div class="skeleton-card" />'
                    },
                    TeamFormModal: {
                        template: '<div class="team-form-modal" />'
                    },
                    Users: { template: '<span class="icon" />' },
                    Plus: { template: '<span class="icon" />' },
                    UserPlus: { template: '<span class="icon" />' },
                    Edit: { template: '<span class="icon" />' },
                    Trash2: { template: '<span class="icon" />' },
                    User: { template: '<span class="icon" />' },
                    LogOut: { template: '<span class="icon" />' }
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
            expect(wrapper.text()).toContain('팀 관리')
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
