import { describe, it, expect, vi, beforeEach } from 'vitest'
import { passwordApi } from '../password'
import apiClient from '../client'

vi.mock('../client', () => ({
  default: {
    post: vi.fn(),
    put: vi.fn()
  }
}))

describe('passwordApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('requestPasswordReset', () => {
    it('should request password reset', async () => {
      const mockResponse = { data: { success: true, message: '이메일이 발송되었습니다.' } }
      vi.mocked(apiClient.post).mockResolvedValue(mockResponse)

      const result = await passwordApi.requestPasswordReset('test@example.com')

      expect(apiClient.post).toHaveBeenCalledWith('/auth/password/forgot', {
        email: 'test@example.com'
      })
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle password reset request error', async () => {
      vi.mocked(apiClient.post).mockRejectedValue(new Error('User not found'))

      await expect(passwordApi.requestPasswordReset('nonexistent@example.com')).rejects.toThrow(
        'User not found'
      )
    })
  })

  describe('resetPassword', () => {
    it('should reset password with token', async () => {
      const mockResponse = { data: { success: true, message: '비밀번호가 재설정되었습니다.' } }
      vi.mocked(apiClient.post).mockResolvedValue(mockResponse)

      const result = await passwordApi.resetPassword('valid-token', 'newPassword123')

      expect(apiClient.post).toHaveBeenCalledWith('/auth/password/reset', {
        token: 'valid-token',
        newPassword: 'newPassword123'
      })
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle invalid token error', async () => {
      vi.mocked(apiClient.post).mockRejectedValue(new Error('Invalid token'))

      await expect(passwordApi.resetPassword('invalid-token', 'newPassword123')).rejects.toThrow(
        'Invalid token'
      )
    })
  })

  describe('changePassword', () => {
    it('should change password successfully', async () => {
      const mockResponse = { data: { success: true, message: '비밀번호가 변경되었습니다.' } }
      vi.mocked(apiClient.put).mockResolvedValue(mockResponse)

      const result = await passwordApi.changePassword('oldPassword123', 'newPassword456')

      expect(apiClient.put).toHaveBeenCalledWith('/auth/password/change', {
        oldPassword: 'oldPassword123',
        newPassword: 'newPassword456'
      })
      expect(result).toEqual(mockResponse.data)
    })

    it('should handle wrong old password error', async () => {
      vi.mocked(apiClient.put).mockRejectedValue(new Error('Current password is incorrect'))

      await expect(passwordApi.changePassword('wrongPassword', 'newPassword456')).rejects.toThrow(
        'Current password is incorrect'
      )
    })
  })
})
