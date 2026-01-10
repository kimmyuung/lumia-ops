import apiClient from '@/api/client'

/**
 * 서버로 전송할 에러 정보
 */
interface ErrorLogData {
  message: string
  stack?: string
  url: string
  userAgent: string
  timestamp: string
  context?: Record<string, unknown>
}

/**
 * 최근 로깅한 에러를 추적하여 중복 방지
 * key: 에러 식별자, value: 타임스탬프
 */
const recentErrors = new Map<string, number>()

/**
 * 에러를 서버에 로깅
 * 개발 환경에서는 콘솔에만 출력하고 서버로 전송하지 않음
 */
export async function logErrorToServer(error: Error, context?: Record<string, unknown>): Promise<void> {
  // 개발 환경에서는 콘솔만 사용
  if (import.meta.env.DEV) {
    console.error('[Dev Error]', error, context)
    return
  }

  // 에러 중복 체크 (같은 에러가 1분 내 반복되면 무시)
  const errorKey = `${error.message}_${error.stack?.slice(0, 100)}`
  const now = Date.now()
  const lastLogged = recentErrors.get(errorKey)

  if (lastLogged && now - lastLogged < 60000) {
    // 1분 이내에 같은 에러가 이미 로깅됨
    return
  }

  recentErrors.set(errorKey, now)

  // 1분 후 자동 삭제
  setTimeout(() => recentErrors.delete(errorKey), 60000)

  try {
    const errorData: ErrorLogData = {
      message: error.message || 'Unknown error',
      stack: error.stack,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString(),
      context
    }

    await apiClient.post('/logs/client-error', errorData)
  } catch (e) {
    // 로깅 실패해도 앱은 계속 작동
    // 재귀 방지를 위해 console.error만 사용
    console.error('Failed to log error to server:', e)
  }
}

/**
 * 여러 에러를 배치로 서버에 로깅
 * 주기적으로 모아서 전송할 때 사용
 */
export async function logErrorBatch(errors: Error[]): Promise<void> {
  if (import.meta.env.DEV || errors.length === 0) {
    return
  }

  try {
    const errorDataList = errors.map(error => ({
      message: error.message || 'Unknown error',
      stack: error.stack,
      url: window.location.href,
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString()
    }))

    await apiClient.post('/logs/client-errors', errorDataList)
  } catch (e) {
    console.error('Failed to log error batch to server:', e)
  }
}

/**
 * 에러 메시지가 심각한 에러 패턴과 일치하는지 확인
 */
export function isCriticalError(message: string): boolean {
  const criticalPatterns = [
    'Cannot read property',
    'undefined is not',
    'null is not',
    'Maximum call stack',
    'Out of memory',
    'script error'
  ]
  return criticalPatterns.some(pattern => message.toLowerCase().includes(pattern.toLowerCase()))
}
