import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Modal from '../Modal.vue'

describe('Modal', () => {
  const mountComponent = (props = {}, slots = {}) => {
    return mount(Modal, {
      props: {
        modelValue: true,
        title: '테스트 모달',
        ...props
      },
      slots,
      global: {
        stubs: {
          Teleport: true,
          Transition: {
            template: '<div><slot /></div>'
          }
        }
      }
    })
  }

  describe('rendering', () => {
    it('should render when modelValue is true', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.modal-overlay').exists()).toBe(true)
      expect(wrapper.find('.modal').exists()).toBe(true)
    })

    it('should not render when modelValue is false', () => {
      const wrapper = mountComponent({ modelValue: false })

      expect(wrapper.find('.modal-overlay').exists()).toBe(false)
    })

    it('should render title', () => {
      const wrapper = mountComponent({ title: '제목' })

      expect(wrapper.find('.modal-header h3').text()).toBe('제목')
    })

    it('should render default slot content', () => {
      const wrapper = mountComponent(
        {},
        {
          default: '<p>모달 내용</p>'
        }
      )

      expect(wrapper.find('.modal-body').text()).toContain('모달 내용')
    })

    it('should render footer slot when provided', () => {
      const wrapper = mountComponent(
        {},
        {
          footer: '<button>확인</button>'
        }
      )

      expect(wrapper.find('.modal-footer').exists()).toBe(true)
      expect(wrapper.find('.modal-footer').text()).toContain('확인')
    })

    it('should not render footer when slot is empty', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.modal-footer').exists()).toBe(false)
    })
  })

  describe('styling', () => {
    it('should apply custom maxWidth', () => {
      const wrapper = mountComponent({ maxWidth: '600px' })

      const modal = wrapper.find('.modal')
      expect(modal.attributes('style')).toContain('max-width: 600px')
    })

    it('should use default maxWidth of 500px', () => {
      const wrapper = mountComponent()

      const modal = wrapper.find('.modal')
      expect(modal.attributes('style')).toContain('max-width: 500px')
    })
  })

  describe('close functionality', () => {
    it('should emit update:modelValue on close button click', async () => {
      const wrapper = mountComponent()

      await wrapper.find('.modal-close').trigger('click')

      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')![0]).toEqual([false])
    })

    it('should close on overlay click when closeOnOverlay is true', async () => {
      const wrapper = mountComponent({ closeOnOverlay: true })

      await wrapper.find('.modal-overlay').trigger('click')

      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    })

    it('should not close on overlay click when closeOnOverlay is false', async () => {
      const wrapper = mountComponent({ closeOnOverlay: false })

      await wrapper.find('.modal-overlay').trigger('click')

      expect(wrapper.emitted('update:modelValue')).toBeFalsy()
    })
  })
})
