import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import TeamJoinModal from '../TeamJoinModal.vue'

// 모킹
vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    success: vi.fn(),
    error: vi.fn()
  })
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

vi.mock('@/api/invitation', () => ({
  invitationApi: {
    getInvitationByToken: vi.fn(),
    acceptInvitation: vi.fn()
  }
}))

vi.mock('@/stores/team', () => ({
  useTeamStore: () => ({
    fetchMyTeam: vi.fn()
  })
}))

describe('TeamJoinModal', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  const mountComponent = () => {
    return mount(TeamJoinModal, {
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
          Users: true,
          UserPlus: true
        }
      }
    })
  }

  describe('rendering', () => {
    it('should render join form', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.join-form').exists()).toBe(true)
    })

    it('should render invite code input', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('#inviteCode').exists()).toBe(true)
    })

    it('should render description text', () => {
      const wrapper = mountComponent()

      expect(wrapper.find('.description').exists()).toBe(true)
    })

    it('should have submit button disabled when no input', () => {
      const wrapper = mountComponent()

      const buttons = wrapper.findAll('button')
      const submitButton = buttons.find(b => b.text().includes('팀 참여'))

      expect(submitButton?.attributes('disabled')).toBeDefined()
    })
  })

  describe('extractToken', () => {
    it('should extract token from URL', async () => {
      const wrapper = mountComponent()
      const vm = wrapper.vm as unknown as { extractToken: (input: string) => string }

      const result = vm.extractToken('https://example.com/invite/abc123_token')

      expect(result).toBe('abc123_token')
    })

    it('should return input as-is if not URL', async () => {
      const wrapper = mountComponent()
      const vm = wrapper.vm as unknown as { extractToken: (input: string) => string }

      const result = vm.extractToken('simple-code-123')

      expect(result).toBe('simple-code-123')
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
