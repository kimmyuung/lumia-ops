import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import StrategyView from '../StrategyView.vue'

// Mock useToast
vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    show: vi.fn()
  })
}))

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
          PageHeader: {
            template: '<div class="page-header"><slot /></div>',
            props: ['title', 'description', 'icon']
          },
          Card: {
            template: '<div class="card"><slot /></div>',
            props: ['hoverable']
          },
          Button: {
            template: '<button class="btn"><slot /></button>',
            props: ['variant', 'disabled']
          },
          TacticalMap: {
            template: '<div class="tactical-map"><div class="map-placeholder">택티컬 맵</div></div>'
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

    it('should display strategy view container', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.strategy-view').exists()).toBe(true)
    })

    it('should have controls section', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.controls-section').exists()).toBe(true)
    })
  })

  describe('tactical map', () => {
    it('should render map section', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.map-section').exists()).toBe(true)
    })

    it('should render tactical map component', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.tactical-map').exists()).toBe(true)
    })

    it('should display map placeholder', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.map-placeholder').exists()).toBe(true)
      expect(wrapper.text()).toContain('택티컬 맵')
    })
  })

  describe('controls section', () => {
    it('should render title input', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.title-input').exists()).toBe(true)
    })

    it('should render save button', () => {
      const wrapper = mountComponent()

      expect(wrapper.text()).toContain('저장')
    })

    it('should render load button', () => {
      const wrapper = mountComponent()

      expect(wrapper.text()).toContain('불러오기')
    })
  })

  describe('strategies section', () => {
    it('should render strategies section', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.strategies-section').exists()).toBe(true)
    })

    it('should display saved strategies header', () => {
      const wrapper = mountComponent()

      expect(wrapper.text()).toContain('저장된 전략')
    })

    it('should show empty state when no strategies', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.empty-state').exists()).toBe(true)
      expect(wrapper.text()).toContain('저장된 전략이 없습니다')
    })
  })
})
