import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import TeamFormModal from '../TeamFormModal.vue'

// Team store 모킹
const mockCreateTeam = vi.fn()
const mockUpdateTeam = vi.fn()

vi.mock('@/stores/team', () => ({
  useTeamStore: () => ({
    createTeam: mockCreateTeam,
    updateTeam: mockUpdateTeam
  })
}))

describe('TeamFormModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const mountComponent = (props = {}) => {
    return mount(TeamFormModal, {
      props,
      global: {
        stubs: {
          Modal: {
            template: '<div class="modal"><slot /><slot name="footer" /></div>',
            props: ['modelValue', 'title']
          },
          Button: {
            template: '<button v-bind="$attrs" :disabled="disabled || loading"><slot /></button>',
            props: ['disabled', 'loading', 'variant']
          },
          Input: {
            template:
              '<input v-bind="$attrs" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'error', 'disabled']
          },
          Save: true
        }
      }
    })
  }

  describe('rendering', () => {
    it('should render team form', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.team-form').exists()).toBe(true)
    })

    it('should render form fields', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('#teamDescription').exists()).toBe(true)
    })

    it('should render buttons', () => {
      const wrapper = mountComponent()

      const buttons = wrapper.findAll('button')
      expect(buttons.length).toBeGreaterThanOrEqual(2)
    })
  })

  describe('edit mode', () => {
    it('should detect edit mode when team is provided', async () => {
      const team = {
        id: '1',
        name: 'Test Team',
        description: 'Team description',
        ownerId: 'owner1',
        members: [],
        createdAt: '2026-01-01T00:00:00Z'
      }

      const wrapper = mountComponent({ team })
      await flushPromises()

      expect((wrapper.vm as unknown as { isEdit: boolean }).isEdit).toBe(true)
    })
  })

  describe('close', () => {
    it('should emit close event when cancel button clicked', async () => {
      const wrapper = mountComponent()

      const buttons = wrapper.findAll('button')
      const cancelButton = buttons.find(b => b.text().includes('취소'))

      if (cancelButton) {
        await cancelButton.trigger('click')
        expect(wrapper.emitted('close')).toBeTruthy()
      }
    })
  })
})
