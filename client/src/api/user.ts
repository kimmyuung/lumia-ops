import apiClient from './client'
import type { User } from './auth'

// ==================== 타입 정의 ====================

/** 닉네임 설정/변경 요청 */
export interface UpdateNicknameRequest {
  nickname: string
}

/** 닉네임 변경 남은 일수 응답 */
export interface NicknameRemainingDaysResponse {
  daysRemaining: number
}

// ==================== API 함수 ====================

export const userApi = {
  /**
   * 내 정보 조회
   */
  async getMyInfo(): Promise<User> {
    const response = await apiClient.get<User>('/users/me')
    return response.data
  },

  /**
   * 닉네임 설정 (첫 설정 - 이메일 인증 후)
   */
  async setInitialNickname(nickname: string): Promise<User> {
    const response = await apiClient.post<User>('/users/me/nickname', { nickname })
    return response.data
  },

  /**
   * 닉네임 변경 (30일 제한)
   */
  async updateNickname(nickname: string): Promise<User> {
    const response = await apiClient.put<User>('/users/me/nickname', { nickname })
    return response.data
  },

  /**
   * 닉네임 변경까지 남은 일수 조회
   */
  async getNicknameRemainingDays(): Promise<NicknameRemainingDaysResponse> {
    const response = await apiClient.get<NicknameRemainingDaysResponse>(
      '/users/me/nickname/remaining-days'
    )
    return response.data
  }
}

export default userApi
