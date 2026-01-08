import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios'
import { isTokenExpired } from '@/utils/token'
import router from '@/router'

// 에러 타입 정의
export interface FieldErrorItem {
  field: string
  message: string
}

export interface ApiError {
  status: number
  message: string
  code?: string
  details?: FieldErrorItem[]
}

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - 토큰 자동 첨부 및 만료 체크/갱신
let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb)
}

function onTokenRefreshed(token: string) {
  refreshSubscribers.forEach(cb => cb(token))
  refreshSubscribers = []
}

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = localStorage.getItem('refreshToken')
  if (!refreshToken) return null

  try {
    const response = await axios.post<{ token: string; refreshToken: string }>(
      `${import.meta.env.VITE_API_URL || 'http://localhost:8080/api'}/auth/refresh`,
      { refreshToken }
    )

    const { token, refreshToken: newRefreshToken } = response.data
    localStorage.setItem('token', token)
    localStorage.setItem('refreshToken', newRefreshToken)
    return token
  } catch {
    // Refresh 실패 시 로그아웃
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    return null
  }
}

apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    let token = localStorage.getItem('token')

    if (token) {
      // 토큰 만료 체크 (5분 버퍼 - 만료 5분 전에 갱신)
      if (isTokenExpired(token, 300)) {
        console.log('[API] 토큰 만료 임박, 갱신 시도...')

        if (!isRefreshing) {
          isRefreshing = true
          const newToken = await refreshAccessToken()
          isRefreshing = false

          if (newToken) {
            token = newToken
            onTokenRefreshed(newToken)
            console.log('[API] 토큰 갱신 성공')
          } else {
            console.warn('[API] 토큰 갱신 실패, 로그아웃')
            window.dispatchEvent(new CustomEvent('auth:token-expired'))
            return Promise.reject({
              status: 401,
              message: '세션이 만료되었습니다. 다시 로그인해 주세요.',
              code: 'TOKEN_EXPIRED'
            } as ApiError)
          }
        } else {
          // 다른 요청이 갱신 중이면 대기
          await new Promise<string>(resolve => subscribeTokenRefresh(resolve))
          token = localStorage.getItem('token')
        }
      }

      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('[API Request Error]', error)
    return Promise.reject(error)
  }
)

// Response interceptor - 상세 에러 핸들링
apiClient.interceptors.response.use(
  response => response,
  (error: AxiosError) => {
    const status = error.response?.status
    const data = error.response?.data as Record<string, unknown> | undefined

    // 네트워크 오류 (서버 응답 없음)
    if (!error.response) {
      if (error.code === 'ECONNABORTED') {
        console.error('[API Timeout]', error.message)
        return Promise.reject({
          status: 408,
          message: '요청 시간이 초과되었습니다. 다시 시도해 주세요.',
          code: 'TIMEOUT'
        } as ApiError)
      }

      console.error('[API Network Error]', error.message)
      return Promise.reject({
        status: 0,
        message: '네트워크 연결을 확인해 주세요.',
        code: 'NETWORK_ERROR'
      } as ApiError)
    }

    // HTTP 상태 코드별 처리
    switch (status) {
      case 400:
        console.error('[API Bad Request]', data)
        break
      case 401:
        console.error('[API Unauthorized]')
        localStorage.removeItem('token')
        // 로그인 페이지로 리다이렉트 (router가 준비되면 사용)
        if (router.currentRoute.value.name !== 'Login') {
          router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
        }
        break
      case 403:
        console.error('[API Forbidden]')
        break
      case 404:
        console.error('[API Not Found]')
        break
      case 422:
        console.error('[API Validation Error]', data)
        break
      case 429:
        console.error('[API Rate Limited]')
        break
      case 500:
      case 502:
      case 503:
        console.error('[API Server Error]', status)
        break
      default:
        console.error('[API Error]', status, data)
    }

    // 표준화된 에러 객체 반환
    return Promise.reject({
      status: status || 0,
      message: (data?.message as string) || getDefaultErrorMessage(status),
      code: (data?.code as string) || `HTTP_${status}`,
      details: (data?.details as FieldErrorItem[]) || undefined
    } as ApiError)
  }
)

// 상태 코드별 기본 에러 메시지
function getDefaultErrorMessage(status?: number): string {
  switch (status) {
    case 400:
      return '잘못된 요청입니다.'
    case 401:
      return '로그인이 필요합니다.'
    case 403:
      return '접근 권한이 없습니다.'
    case 404:
      return '요청한 리소스를 찾을 수 없습니다.'
    case 422:
      return '입력값을 확인해 주세요.'
    case 429:
      return '요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.'
    case 500:
      return '서버 오류가 발생했습니다.'
    case 502:
      return '서버가 응답하지 않습니다.'
    case 503:
      return '서비스를 일시적으로 사용할 수 없습니다.'
    default:
      return '알 수 없는 오류가 발생했습니다.'
  }
}

export default apiClient
