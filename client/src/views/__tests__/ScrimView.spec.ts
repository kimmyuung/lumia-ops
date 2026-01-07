import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia, setActivePinia } from 'pinia'
import { ref } from 'vue'
import ScrimView from '../ScrimView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', component: ScrimView }]
})

// Mock useScrimStore
vi.mock('@/stores/scrim', () => ({
  useScrimStore: () => ({
    scrims: ref([]),
    filteredScrims: ref([]),
    scheduledScrims: ref([]),
    completedScrims: ref([]),
    scrimStats: ref({ count: 0, avgPlacement: '-', totalKills: 0 }),
    currentScrim: ref(null),
    isLoading: ref(false),
    error: ref(null),
    statusFilter: ref('ALL'),
    fetchScrims: vi.fn(),
    fetchScrim: vi.fn(),
    createScrim: vi.fn(),
    updateScrim: vi.fn(),
    deleteScrim: vi.fn(),
    setStatusFilter: vi.fn()
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

describe('ScrimView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const mountComponent = () => {
    return mount(ScrimView, {
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
          Skeleton: {
            template: '<div class="skeleton" />'
          },
          SkeletonCard: {
            template: '<div class="skeleton-card" />'
          },
          ScrimFormModal: {
            template: '<div class="scrim-form-modal" />'
          },
          Swords: { template: '<span class="icon" />' },
          Plus: { template: '<span class="icon" />' },
          Calendar: { template: '<span class="icon" />' },
          Trophy: { template: '<span class="icon" />' },
          TrendingUp: { template: '<span class="icon" />' },
          Edit: { template: '<span class="icon" />' },
          Trash2: { template: '<span class="icon" />' }
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
      expect(wrapper.text()).toContain('스크림 관리')
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
