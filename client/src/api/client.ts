import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios'
import { isTokenExpired } from '@/utils/token'

// 에러 타입 정의
export interface ApiError {
  status: number
  message: string
  code?: string
}

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor - 토큰 자동 첨부 및 만료 체크
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')

    if (token) {
      // 토큰 만료 체크 (60초 버퍼)
      if (isTokenExpired(token, 60)) {
        console.warn('[API] 토큰이 만료되었습니다.')
        localStorage.removeItem('token')
        window.dispatchEvent(new CustomEvent('auth:token-expired'))

        return Promise.reject({
          status: 401,
          message: '세션이 만료되었습니다. 다시 로그인해 주세요.',
          code: 'TOKEN_EXPIRED'
        } as ApiError)
      }

      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('[API Request Error]', error)
    return Promise.reject(error)
  }
)

// Response interceptor - 상세 에러 핸들링
apiClient.interceptors.response.use(
  (response) => response,
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
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
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
      code: (data?.code as string) || `HTTP_${status}`
    } as ApiError)
  }
)

// 상태 코드별 기본 에러 메시지
function getDefaultErrorMessage(status?: number): string {
  switch (status) {
    case 400: return '잘못된 요청입니다.'
    case 401: return '로그인이 필요합니다.'
    case 403: return '접근 권한이 없습니다.'
    case 404: return '요청한 리소스를 찾을 수 없습니다.'
    case 422: return '입력값을 확인해 주세요.'
    case 429: return '요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.'
    case 500: return '서버 오류가 발생했습니다.'
    case 502: return '서버가 응답하지 않습니다.'
    case 503: return '서비스를 일시적으로 사용할 수 없습니다.'
    default: return '알 수 없는 오류가 발생했습니다.'
  }
}

export default apiClient
