import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi, type LoginRequest, type RegisterRequest } from '@/api/auth'
import { userApi } from '@/api/user'
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
     * 회원가입 (이메일/비밀번호만)
     * 가입 후 이메일 인증 필요
     */
    async function register(data: RegisterRequest) {
        isLoading.value = true
        error.value = null

        try {
            const response = await authApi.register(data)
            if (response.success) {
                toast.success('회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.')
                router.push('/login')
                return true
            } else {
                error.value = response.message
                toast.error(response.message)
                return false
            }
        } catch (err: unknown) {
            const apiError = err as { response?: { data?: { message?: string } }, message?: string }
            error.value = apiError.response?.data?.message || apiError.message || '회원가입에 실패했습니다.'
            toast.error(error.value)
            return false
        } finally {
            isLoading.value = false
        }
    }

    /**
     * 로그인
     * 다양한 상태 처리: 닉네임 필요, 잠금, 휴면
     */
    async function login(data: LoginRequest) {
        isLoading.value = true
        error.value = null

        try {
            const response = await authApi.login(data)

            // 토큰 저장 (닉네임 설정 필요 여부와 관계없이)
            userStore.setToken(response.token, response.refreshToken)

            // 닉네임 설정 필요
            if (response.needsNickname) {
                userStore.setTempUser({
                    id: response.userId,
                    email: response.email,
                    status: response.status
                })
                toast.info('닉네임을 설정해주세요.')
                router.push('/auth/set-nickname')
                return true
            }

            // 로그인 성공
            userStore.setUser({
                id: String(response.userId),
                email: response.email,
                nickname: response.nickname || '',
                status: response.status
            })
            toast.success('로그인되었습니다.')
            router.push('/')
            return true
        } catch (err: unknown) {
            const apiError = err as { response?: { data?: { message?: string }, status?: number }, message?: string }
            const status = apiError.response?.status
            const message = apiError.response?.data?.message || apiError.message || '로그인에 실패했습니다.'

            error.value = message

            // 계정 잠금 (5회 실패)
            if (status === 403) {
                toast.error(message)
            } else {
                toast.error(message)
            }

            return false
        } finally {
            isLoading.value = false
        }
    }

    /**
     * 이메일 인증
     */
    async function verifyEmail(token: string) {
        isLoading.value = true
        error.value = null

        try {
            const response = await authApi.verifyEmail(token)
            if (response.success) {
                toast.success(response.message)
                return true
            } else {
                error.value = response.message
                toast.error(response.message)
                return false
            }
        } catch (err: unknown) {
            const apiError = err as { response?: { data?: { message?: string } }, message?: string }
            error.value = apiError.response?.data?.message || apiError.message || '이메일 인증에 실패했습니다.'
            toast.error(error.value)
            return false
        } finally {
            isLoading.value = false
        }
    }

    /**
     * 닉네임 설정 (첫 로그인 시)
     */
    async function setNickname(nickname: string) {
        isLoading.value = true
        error.value = null

        try {
            const response = await userApi.setInitialNickname(nickname)
            userStore.setUser({
                id: String(response.id),
                email: response.email,
                nickname: response.nickname || '',
                status: response.status
            })
            toast.success('닉네임이 설정되었습니다.')
            router.push('/')
            return true
        } catch (err: unknown) {
            const apiError = err as { response?: { data?: { message?: string } }, message?: string }
            error.value = apiError.response?.data?.message || apiError.message || '닉네임 설정에 실패했습니다.'
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
            const userData = await userApi.getMyInfo()
            userStore.setUser({
                id: String(userData.id),
                email: userData.email,
                nickname: userData.nickname || '',
                status: userData.status
            })
        } catch {
            userStore.logout()
        }
    }

    /**
     * 인증 이메일 재발송
     */
    async function resendVerification(email: string) {
        isLoading.value = true
        error.value = null

        try {
            const response = await authApi.resendVerification(email)
            if (response.success) {
                toast.success(response.message)
                return true
            } else {
                error.value = response.message
                toast.error(response.message)
                return false
            }
        } catch (err: unknown) {
            const apiError = err as { response?: { data?: { message?: string } }, message?: string }
            error.value = apiError.response?.data?.message || apiError.message || '이메일 발송에 실패했습니다.'
            toast.error(error.value)
            return false
        } finally {
            isLoading.value = false
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
        fetchUser,
        verifyEmail,
        setNickname,
        resendVerification
    }
}
