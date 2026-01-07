import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuth } from '../useAuth'

// Mock dependencies
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

const mockShowToast = vi.fn()
const mockSuccess = vi.fn()
const mockError = vi.fn()
const mockInfo = vi.fn()
vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    show: mockShowToast,
    success: mockSuccess,
    error: mockError,
    info: mockInfo
  })
}))

const mockLogin = vi.fn()
const mockRegister = vi.fn()
const mockVerifyEmail = vi.fn()
const mockResendVerification = vi.fn()
const mockLogout = vi.fn()
vi.mock('@/api/auth', () => ({
  authApi: {
    login: (...args: unknown[]) => mockLogin(...args),
    register: (...args: unknown[]) => mockRegister(...args),
    verifyEmail: (...args: unknown[]) => mockVerifyEmail(...args),
    resendVerification: (...args: unknown[]) => mockResendVerification(...args),
    logout: () => mockLogout()
  }
}))

const mockSetInitialNickname = vi.fn()
const mockGetMyInfo = vi.fn()
vi.mock('@/api/user', () => ({
  userApi: {
    setInitialNickname: (...args: unknown[]) => mockSetInitialNickname(...args),
    getMyInfo: () => mockGetMyInfo()
  }
}))

describe('useAuth', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('initial state', () => {
    it('should have isLoading as false', () => {
      const { isLoading } = useAuth()
      expect(isLoading.value).toBe(false)
    })

    it('should have error as null', () => {
      const { error } = useAuth()
      expect(error.value).toBeNull()
    })

    it('should have isLoggedIn as false initially', () => {
      const { isLoggedIn } = useAuth()
      expect(isLoggedIn.value).toBe(false)
    })
  })

  describe('register', () => {
    it('should call authApi.register with correct data', async () => {
      mockRegister.mockResolvedValue({ success: true, message: '성공' })
      const { register } = useAuth()

      await register({ email: 'test@example.com', password: 'password123' })

      expect(mockRegister).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123'
      })
    })

    it('should show success toast on successful registration', async () => {
      mockRegister.mockResolvedValue({ success: true, message: '성공' })
      const { register } = useAuth()

      await register({ email: 'test@example.com', password: 'password123' })

      expect(mockSuccess).toHaveBeenCalled()
    })

    it('should return true on success', async () => {
      mockRegister.mockResolvedValue({ success: true, message: '성공' })
      const { register } = useAuth()

      const result = await register({ email: 'test@example.com', password: 'password123' })

      expect(result).toBe(true)
    })

    it('should return false on failure', async () => {
      mockRegister.mockResolvedValue({ success: false, message: '실패' })
      const { register } = useAuth()

      const result = await register({ email: 'test@example.com', password: 'password123' })

      expect(result).toBe(false)
    })

    it('should show error toast on failure', async () => {
      mockRegister.mockResolvedValue({ success: false, message: '이미 존재하는 이메일' })
      const { register } = useAuth()

      await register({ email: 'test@example.com', password: 'password123' })

      expect(mockError).toHaveBeenCalledWith('이미 존재하는 이메일')
    })
  })

  describe('login', () => {
    it('should call authApi.login with correct data', async () => {
      mockLogin.mockResolvedValue({
        token: 'token123',
        refreshToken: 'refresh123',
        userId: 1,
        email: 'test@example.com',
        nickname: 'TestUser',
        status: 'ACTIVE',
        needsNickname: false
      })
      const { login } = useAuth()

      await login({ email: 'test@example.com', password: 'password123' })

      expect(mockLogin).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123'
      })
    })

    it('should redirect to set-nickname when needsNickname is true', async () => {
      mockLogin.mockResolvedValue({
        token: 'token123',
        refreshToken: 'refresh123',
        userId: 1,
        email: 'test@example.com',
        status: 'PENDING_NICKNAME',
        needsNickname: true
      })
      const { login } = useAuth()

      await login({ email: 'test@example.com', password: 'password123' })

      expect(mockPush).toHaveBeenCalledWith('/auth/set-nickname')
    })

    it('should redirect to home on successful login', async () => {
      mockLogin.mockResolvedValue({
        token: 'token123',
        refreshToken: 'refresh123',
        userId: 1,
        email: 'test@example.com',
        nickname: 'TestUser',
        status: 'ACTIVE',
        needsNickname: false
      })
      const { login } = useAuth()

      await login({ email: 'test@example.com', password: 'password123' })

      expect(mockPush).toHaveBeenCalledWith('/')
    })

    it('should return false on error', async () => {
      mockLogin.mockRejectedValue({
        response: { data: { message: '로그인 실패' } }
      })
      const { login } = useAuth()

      const result = await login({ email: 'test@example.com', password: 'wrong' })

      expect(result).toBe(false)
    })
  })

  describe('logout', () => {
    it('should call authApi.logout', async () => {
      mockLogout.mockResolvedValue({})
      const { logout } = useAuth()

      await logout()

      expect(mockLogout).toHaveBeenCalled()
    })

    it('should redirect to login page', async () => {
      mockLogout.mockResolvedValue({})
      const { logout } = useAuth()

      await logout()

      expect(mockPush).toHaveBeenCalledWith('/login')
    })

    it('should show info toast', async () => {
      mockLogout.mockResolvedValue({})
      const { logout } = useAuth()

      await logout()

      expect(mockInfo).toHaveBeenCalledWith('로그아웃되었습니다.')
    })
  })

  describe('verifyEmail', () => {
    it('should call authApi.verifyEmail with token', async () => {
      mockVerifyEmail.mockResolvedValue({ success: true, message: '인증 완료' })
      const { verifyEmail } = useAuth()

      await verifyEmail('verification-token')

      expect(mockVerifyEmail).toHaveBeenCalledWith('verification-token')
    })

    it('should return true on success', async () => {
      mockVerifyEmail.mockResolvedValue({ success: true, message: '인증 완료' })
      const { verifyEmail } = useAuth()

      const result = await verifyEmail('verification-token')

      expect(result).toBe(true)
    })

    it('should return false on failure', async () => {
      mockVerifyEmail.mockResolvedValue({ success: false, message: '만료된 토큰' })
      const { verifyEmail } = useAuth()

      const result = await verifyEmail('expired-token')

      expect(result).toBe(false)
    })
  })

  describe('setNickname', () => {
    it('should call userApi.setInitialNickname', async () => {
      mockSetInitialNickname.mockResolvedValue({
        id: 1,
        email: 'test@example.com',
        nickname: 'NewNick',
        status: 'ACTIVE'
      })
      const { setNickname } = useAuth()

      await setNickname('NewNick')

      expect(mockSetInitialNickname).toHaveBeenCalledWith('NewNick')
    })

    it('should redirect to home on success', async () => {
      mockSetInitialNickname.mockResolvedValue({
        id: 1,
        email: 'test@example.com',
        nickname: 'NewNick',
        status: 'ACTIVE'
      })
      const { setNickname } = useAuth()

      await setNickname('NewNick')

      expect(mockPush).toHaveBeenCalledWith('/')
    })
  })

  describe('resendVerification', () => {
    it('should call authApi.resendVerification', async () => {
      mockResendVerification.mockResolvedValue({ success: true, message: '발송 완료' })
      const { resendVerification } = useAuth()

      await resendVerification('test@example.com')

      expect(mockResendVerification).toHaveBeenCalledWith('test@example.com')
    })

    it('should return true on success', async () => {
      mockResendVerification.mockResolvedValue({ success: true, message: '발송 완료' })
      const { resendVerification } = useAuth()

      const result = await resendVerification('test@example.com')

      expect(result).toBe(true)
    })
  })
})
