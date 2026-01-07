import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Skeleton from '../Skeleton.vue'

describe('Skeleton', () => {
  describe('rendering', () => {
    it('should render skeleton element', () => {
      const wrapper = mount(Skeleton)

      expect(wrapper.find('.skeleton').exists()).toBe(true)
    })

    it('should apply text variant class by default', () => {
      const wrapper = mount(Skeleton)

      expect(wrapper.find('.skeleton').classes()).toContain('skeleton-text')
    })

    it('should apply circular variant class', () => {
      const wrapper = mount(Skeleton, {
        props: { variant: 'circular' }
      })

      expect(wrapper.find('.skeleton').classes()).toContain('skeleton-circular')
    })

    it('should apply rectangular variant class', () => {
      const wrapper = mount(Skeleton, {
        props: { variant: 'rectangular' }
      })

      expect(wrapper.find('.skeleton').classes()).toContain('skeleton-rectangular')
    })
  })

  describe('sizing', () => {
    it('should apply default width of 100%', () => {
      const wrapper = mount(Skeleton)
      const style = wrapper.find('.skeleton').attributes('style')

      expect(style).toContain('width: 100%')
    })

    it('should apply custom width', () => {
      const wrapper = mount(Skeleton, {
        props: { width: '200px' }
      })
      const style = wrapper.find('.skeleton').attributes('style')

      expect(style).toContain('width: 200px')
    })

    it('should apply custom height', () => {
      const wrapper = mount(Skeleton, {
        props: { height: '50px' }
      })
      const style = wrapper.find('.skeleton').attributes('style')

      expect(style).toContain('height: 50px')
    })

    it('should apply 1em height for text variant when no height specified', () => {
      const wrapper = mount(Skeleton, {
        props: { variant: 'text' }
      })
      const style = wrapper.find('.skeleton').attributes('style')

      expect(style).toContain('height: 1em')
    })
  })
})
