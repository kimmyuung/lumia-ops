/**
 * 에러 유틸리티 - API 에러 타입 가드 및 추출 함수
 */

import type { ApiError } from '@/api/client'

/**
 * 필드별 Validation 에러 타입
 */
export interface FieldError {
  field: string
  message: string
}

/**
 * 상세 에러 정보 타입 (백엔드 ErrorResponse와 매핑)
 */
export interface DetailedApiError extends ApiError {
  details?: FieldError[]
  path?: string
  timestamp?: string
}

/**
 * ApiError 타입 가드
 */
export function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'status' in error &&
    'message' in error &&
    typeof (error as ApiError).status === 'number' &&
    typeof (error as ApiError).message === 'string'
  )
}

/**
 * DetailedApiError 타입 가드
 */
export function isDetailedApiError(error: unknown): error is DetailedApiError {
  return isApiError(error) && ('details' in error || 'path' in error)
}

/**
 * 에러에서 메시지 추출
 * ApiError, Error, unknown 모두 처리
 */
export function getErrorMessage(
  error: unknown,
  fallback = '알 수 없는 오류가 발생했습니다.'
): string {
  if (isApiError(error)) {
    return error.message
  }
  if (error instanceof Error) {
    return error.message
  }
  if (typeof error === 'string') {
    return error
  }
  return fallback
}

/**
 * 에러에서 상태 코드 추출
 */
export function getErrorStatus(error: unknown): number {
  if (isApiError(error)) {
    return error.status
  }
  return 0
}

/**
 * 에러에서 코드 추출
 */
export function getErrorCode(error: unknown): string | undefined {
  if (isApiError(error)) {
    return error.code
  }
  return undefined
}

/**
 * 에러에서 필드별 에러 추출
 */
export function getFieldErrors(error: unknown): FieldError[] {
  if (isDetailedApiError(error) && error.details) {
    return error.details
  }
  return []
}

/**
 * 특정 필드의 에러 메시지 추출
 */
export function getFieldError(error: unknown, fieldName: string): string | undefined {
  const fieldErrors = getFieldErrors(error)
  return fieldErrors.find(e => e.field === fieldName)?.message
}

/**
 * 에러 코드별 사용자 친화적 메시지
 */
export function getUserFriendlyMessage(error: unknown): string {
  const code = getErrorCode(error)
  const status = getErrorStatus(error)

  // 코드 기반 메시지
  switch (code) {
    case 'VALIDATION_ERROR':
      return '입력값을 확인해 주세요.'
    case 'TOKEN_EXPIRED':
      return '세션이 만료되었습니다. 다시 로그인해 주세요.'
    case 'DUPLICATE_EMAIL':
      return '이미 사용 중인 이메일입니다.'
    case 'DUPLICATE_NICKNAME':
      return '이미 사용 중인 닉네임입니다.'
    case 'TEAM_NOT_FOUND':
      return '팀을 찾을 수 없습니다.'
    case 'PERMISSION_DENIED':
      return '권한이 없습니다.'
    case 'NETWORK_ERROR':
      return '네트워크 연결을 확인해 주세요.'
    case 'TIMEOUT':
      return '요청 시간이 초과되었습니다. 다시 시도해 주세요.'
  }

  // 상태 코드 기반 메시지
  switch (status) {
    case 400:
      return '잘못된 요청입니다.'
    case 401:
      return '로그인이 필요합니다.'
    case 403:
      return '접근 권한이 없습니다.'
    case 404:
      return '요청한 항목을 찾을 수 없습니다.'
    case 409:
      return '이미 존재하는 항목입니다.'
    case 422:
      return '입력값을 확인해 주세요.'
    case 429:
      return '요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.'
    case 500:
    case 502:
    case 503:
      return '서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.'
  }

  return getErrorMessage(error)
}
