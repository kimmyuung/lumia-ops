import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import TacticalMap from '../TacticalMap.vue'

// Mock canvas context
const mockContext = {
  clearRect: vi.fn(),
  fillRect: vi.fn(),
  beginPath: vi.fn(),
  moveTo: vi.fn(),
  lineTo: vi.fn(),
  stroke: vi.fn(),
  arc: vi.fn(),
  fill: vi.fn(),
  fillText: vi.fn(),
  strokeText: vi.fn(),
  measureText: vi.fn(() => ({ width: 50 })),
  setLineDash: vi.fn(),
  save: vi.fn(),
  restore: vi.fn()
}

// Mock HTMLCanvasElement.getContext
// eslint-disable-next-line @typescript-eslint/no-explicit-any
HTMLCanvasElement.prototype.getContext = vi.fn(() => mockContext) as any

describe('TacticalMap', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('렌더링', () => {
    it('should render tactical map editor', () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [],
            paths: [],
            notes: []
          }
        }
      })

      expect(wrapper.find('.tactical-map-editor').exists()).toBe(true)
      expect(wrapper.find('.canvas-container').exists()).toBe(true)
      expect(wrapper.find('canvas').exists()).toBe(true)
    })

    it('should render toolbar when not readonly', () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [],
            paths: [],
            notes: []
          },
          readonly: false
        }
      })

      expect(wrapper.find('.toolbar').exists()).toBe(true)
    })

    it('should hide toolbar when readonly', () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [],
            paths: [],
            notes: []
          },
          readonly: true
        }
      })

      expect(wrapper.find('.toolbar').exists()).toBe(false)
    })
  })

  describe('도구 선택', () => {
    it('should have tool buttons', () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [],
            paths: [],
            notes: []
          }
        }
      })

      const toolButtons = wrapper.findAll('.tool-btn')
      expect(toolButtons.length).toBeGreaterThan(0)
    })

    it('should have clear all button', () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [],
            paths: [],
            notes: []
          }
        }
      })

      const dangerButton = wrapper.find('.tool-btn.danger')
      expect(dangerButton.exists()).toBe(true)
    })
  })

  describe('마커 아이콘', () => {
    it('should have marker icon options', () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [],
            paths: [],
            notes: []
          }
        }
      })

      // 마커 도구 그룹 확인
      const toolGroups = wrapper.findAll('.tool-group')
      expect(toolGroups.length).toBeGreaterThan(0)
    })
  })

  describe('색상 선택', () => {
    it('should have color palette', () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [],
            paths: [],
            notes: []
          }
        }
      })

      const colorButtons = wrapper.findAll('.color-btn')
      expect(colorButtons.length).toBeGreaterThan(0)
    })
  })

  describe('모델 값 변경', () => {
    it('should have clear all button that can be clicked', async () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [{ id: '1', x: 100, y: 100, icon: 'player', label: '', color: '#ff0000' }],
            paths: [],
            notes: []
          }
        }
      })

      const clearButton = wrapper.find('.tool-btn.danger')
      expect(clearButton.exists()).toBe(true)

      // 버튼 클릭이 가능한지 확인
      await clearButton.trigger('click')

      // 클릭 후에도 컴포넌트가 정상 동작하는지 확인
      expect(wrapper.find('.tactical-map-editor').exists()).toBe(true)
    })
  })

  describe('Props', () => {
    it('should accept modelValue prop', () => {
      const mapData = {
        markers: [{ id: '1', x: 50, y: 50, icon: 'player' as const, color: '#ff0000', label: '' }],
        paths: [],
        notes: []
      }

      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: mapData
        }
      })

      expect(wrapper.props('modelValue')).toEqual(mapData)
    })

    it('should accept backgroundImage prop', () => {
      const wrapper = mount(TacticalMap, {
        props: {
          modelValue: {
            markers: [],
            paths: [],
            notes: []
          },
          backgroundImage: '/map.png'
        }
      })

      expect(wrapper.props('backgroundImage')).toBe('/map.png')
    })
  })
})
