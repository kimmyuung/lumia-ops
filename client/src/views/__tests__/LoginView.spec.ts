import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../LoginView.vue'

// useAuth 모킹
const mockLogin = vi.fn()
const mockIsLoading = { value: false }

vi.mock('@/composables/useAuth', () => ({
    useAuth: () => ({
        login: mockLogin,
        isLoading: mockIsLoading
    })
}))

// 라우터 설정
const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', component: { template: '<div>Home</div>' } },
        { path: '/login', component: LoginView },
        { path: '/register', component: { template: '<div>Register</div>' } },
        { path: '/auth/forgot-password', component: { template: '<div>Forgot</div>' } },
        { path: '/auth/find-username', component: { template: '<div>Find</div>' } }
    ]
})

describe('LoginView', () => {
    const mountComponent = () => {
        return mount(LoginView, {
            global: {
                plugins: [router],
                stubs: {
                    // lucide 아이콘 stub
                    Map: true,
                    LogIn: true,
                    UserPlus: true,
                    MessageCircle: true,
                    Key: true,
                    Search: true,
                    // 컴포넌트 stub - 간단한 버전
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
        mockLogin.mockResolvedValue(true)
    })

    describe('rendering', () => {
        it('should render login form', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.auth-page').exists()).toBe(true)
            expect(wrapper.find('h1').text()).toBe('로그인')
        })

        it('should render email and password inputs', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('#email').exists()).toBe(true)
            expect(wrapper.find('#password').exists()).toBe(true)
        })

        it('should render Lumia Ops logo', () => {
            const wrapper = mountComponent()

            expect(wrapper.find('.auth-logo').exists()).toBe(true)
            expect(wrapper.text()).toContain('Lumia Ops')
        })

        it('should render help links', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('비밀번호 찾기')
            expect(wrapper.text()).toContain('아이디 찾기')
        })

        it('should render register link', () => {
            const wrapper = mountComponent()

            expect(wrapper.text()).toContain('계정이 없으신가요?')
            expect(wrapper.text()).toContain('회원가입')
        })
    })

    describe('form submission', () => {
        it('should call login with form data on valid submit', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')
            await wrapper.find('#password').setValue('password123')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockLogin).toHaveBeenCalledWith({
                email: 'test@example.com',
                password: 'password123'
            })
        })

        it('should not call login when email is empty', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#password').setValue('password123')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockLogin).not.toHaveBeenCalled()
        })

        it('should not call login when password is empty', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('test@example.com')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockLogin).not.toHaveBeenCalled()
        })

        it('should not call login with invalid email format', async () => {
            const wrapper = mountComponent()

            await wrapper.find('#email').setValue('invalid-email')
            await wrapper.find('#password').setValue('password123')

            await wrapper.find('form').trigger('submit')
            await flushPromises()

            expect(mockLogin).not.toHaveBeenCalled()
        })
    })
})
