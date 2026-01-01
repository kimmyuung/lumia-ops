import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi, type LoginRequest, type RegisterRequest } from '@/api/auth'
import { useToast } from '@/composables/useToast'

/**
 * 인증 관련 훅
 */
export function useAuth() {
    const router = useRouter()
    const userStore = useUserStore()
    const toast = useToast()

    const isLoading = ref(false)
    const error = ref<string | null>(null)

    const isLoggedIn = computed(() => userStore.isLoggedIn)
    const user = computed(() => userStore.user)

    /**
     * 로그인
     */
    async function login(data: LoginRequest) {
        isLoading.value = true
        error.value = null

        try {
            const response = await authApi.login(data)
            userStore.setToken(response.token)
            userStore.setUser(response.user)
            toast.success('로그인되었습니다.')
            router.push('/')
            return true
        } catch (err: unknown) {
            const apiError = err as { message?: string }
            error.value = apiError.message || '로그인에 실패했습니다.'
            toast.error(error.value)
            return false
        } finally {
            isLoading.value = false
        }
    }

    /**
     * 회원가입
     */
    async function register(data: RegisterRequest) {
        isLoading.value = true
        error.value = null

        try {
            const response = await authApi.register(data)
            userStore.setToken(response.token)
            userStore.setUser(response.user)
            toast.success('회원가입이 완료되었습니다.')
            router.push('/')
            return true
        } catch (err: unknown) {
            const apiError = err as { message?: string }
            error.value = apiError.message || '회원가입에 실패했습니다.'
            toast.error(error.value)
            return false
        } finally {
            isLoading.value = false
        }
    }

    /**
     * 로그아웃
     */
    async function logout() {
        try {
            await authApi.logout()
        } catch {
            // 서버 로그아웃 실패해도 클라이언트 로그아웃 진행
        }
        userStore.logout()
        toast.info('로그아웃되었습니다.')
        router.push('/login')
    }

    /**
     * 현재 사용자 정보 조회
     */
    async function fetchUser() {
        if (!userStore.token) return

        try {
            const userData = await authApi.me()
            userStore.setUser(userData)
        } catch {
            userStore.logout()
        }
    }

    return {
        isLoading,
        error,
        isLoggedIn,
        user,
        login,
        register,
        logout,
        fetchUser
    }
}
