import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import RegisterView from '../RegisterView.vue'

// useAuth 모킹
const mockRegister = vi.fn()
const mockResendVerification = vi.fn()
const mockIsLoading = { value: false }

vi.mock('@/composables/useAuth', () => ({
    useAuth: () => ({
        register: mockRegister,
        resendVerification: mockResendVerification,
        isLoading: mockIsLoading
    })
}))

// 라우터 설정
const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', component: { template: '<div>Home</div>' } },
        { path: '/login', component: { template: '<div>Login</div>' } },
        { path: '/register', component: RegisterView }
    ]
})

describe('RegisterView', () => {
    const mountComponent = () => {
        return mount(RegisterView, {
            global: {
                plugins: [router],
                stubs: {
                    // lucide 아이콘 stub
                    Map: true,
                    LogIn: true,
                    UserPlus: true,
                    MessageCircle: true,
                    CheckCircle: true,
                    Mail: true,
                    // 컴포넌트 stub
                    Input: {
                        template: '<input v-bind="$attrs" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target?.value)" />',
                        props: ['modelValue', 'error', 'disabled']
                    },
                    Button: {
                        template: '<button v-bind="$attrs" :disabled="disabled || loading"><slot /></button>',
                        props: ['disabled', 'loading', 'variant', 'type']
                    },
                    Spinner: true
                }
            }
        })
    }

    beforeEach(() => {
        vi.clearAllMocks()
        mockIsLoading.value = false
        mockRegister.mockResolvedValue(true)
    })

    describe('rendering', () => {
        it('should render register form', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.auth-page').exists()).toBe(true)
            expect(wrapper.find('h1').text()).toBe('회원가입')
        })

        it('should render email, password, and confirm password inputs', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('#email').exists()).toBe(true)
            expect(wrapper.find('#password').exists()).toBe(true)
            expect(wrapper.find('#confirmPassword').exists()).toBe(true)
        })

        it('should render Lumia Ops logo', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.auth-logo').exists()).toBe(true)
            expect(wrapper.text()).toContain('Lumia Ops')
        })

        it('should render login link', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('이미 계정이 있으신가요?')
            expect(wrapper.text()).toContain('로그인')
        })

        it('should show password requirements hint', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('영문, 숫자 포함 8자 이상')
        })
    })

    describe('form submission', () => {
        it('should call register with valid form data', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')
            await wrapper.find('#password').setValue('password123')
            await wrapper.find('#confirmPassword').setValue('password123')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockRegister).toHaveBeenCalledWith({
                email: 'test@example.com',
                password: 'password123'
            })
        })

        it('should not call register when passwords do not match', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')
            await wrapper.find('#password').setValue('password123')
            await wrapper.find('#confirmPassword').setValue('different123')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockRegister).not.toHaveBeenCalled()
        })

        it('should not call register when password is too short', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')
            await wrapper.find('#password').setValue('short1')
            await wrapper.find('#confirmPassword').setValue('short1')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockRegister).not.toHaveBeenCalled()
        })

        it('should not call register when password has no number', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')
            await wrapper.find('#password').setValue('passwordonly')
            await wrapper.find('#confirmPassword').setValue('passwordonly')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockRegister).not.toHaveBeenCalled()
        })

        it('should not call register when password has no letter', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')
            await wrapper.find('#password').setValue('12345678')
            await wrapper.find('#confirmPassword').setValue('12345678')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockRegister).not.toHaveBeenCalled()
        })

        it('should validate email format', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('invalid-email')
            await wrapper.find('#password').setValue('password123')
            await wrapper.find('#confirmPassword').setValue('password123')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockRegister).not.toHaveBeenCalled()
        })
    })

    describe('registration success', () => {
        it('should show success message after registration', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')
            await wrapper.find('#password').setValue('password123')
            await wrapper.find('#confirmPassword').setValue('password123')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(wrapper.text()).toContain('이메일을 확인해주세요')
        })

        it('should hide form after registration success', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')
            await wrapper.find('#password').setValue('password123')
            await wrapper.find('#confirmPassword').setValue('password123')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(wrapper.find('form').exists()).toBe(false)
        })
    })
})
