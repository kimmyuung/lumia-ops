import apiClient from './client'
import type { MessageResponse } from './auth'

// ==================== 타입 정의 ====================

/** 비밀번호 찾기 요청 */
export interface PasswordResetRequest {
  email: string
}

/** 비밀번호 재설정 요청 */
export interface ResetPasswordRequest {
  token: string
  newPassword: string
}

/** 비밀번호 변경 요청 (로그인 상태) */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

// ==================== API 함수 ====================

export const passwordApi = {
  /**
   * 비밀번호 찾기 요청 (이메일 발송)
   */
  async requestPasswordReset(email: string): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>('/auth/password/forgot', { email })
    return response.data
  },

  /**
   * 비밀번호 재설정 (토큰으로)
   */
  async resetPassword(token: string, newPassword: string): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>('/auth/password/reset', {
      token,
      newPassword
    })
    return response.data
  },

  /**
   * 비밀번호 변경 (로그인 상태에서)
   */
  async changePassword(oldPassword: string, newPassword: string): Promise<MessageResponse> {
    const response = await apiClient.put<MessageResponse>('/auth/password/change', {
      oldPassword,
      newPassword
    })
    return response.data
  }
}

export default passwordApi
