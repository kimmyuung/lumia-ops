import apiClient from './client'

// ==================== 타입 정의 ====================

export type CommentTargetType = 'STRATEGY' | 'SCRIM' | 'MATCH'

export interface CommentResponse {
  id: number
  targetType: string
  targetId: number
  userId: number
  userName: string
  content: string
  createdAt: string
  updatedAt: string
}

// ==================== API 함수 ====================

export const commentApi = {
  /**
   * 코멘트 목록 조회
   */
  async getComments(
    targetType: CommentTargetType,
    targetId: number,
    page = 0,
    size = 20
  ): Promise<CommentResponse[]> {
    const response = await apiClient.get<CommentResponse[]>(`/comments/${targetType}/${targetId}`, {
      params: { page, size }
    })
    return response.data
  },

  /**
   * 코멘트 개수 조회
   */
  async getCommentCount(targetType: CommentTargetType, targetId: number): Promise<number> {
    const response = await apiClient.get<{ count: number }>(
      `/comments/${targetType}/${targetId}/count`
    )
    return response.data.count
  },

  /**
   * 코멘트 작성
   */
  async createComment(
    targetType: CommentTargetType,
    targetId: number,
    content: string
  ): Promise<CommentResponse> {
    const response = await apiClient.post<CommentResponse>(`/comments/${targetType}/${targetId}`, {
      content
    })
    return response.data
  },

  /**
   * 코멘트 수정
   */
  async updateComment(commentId: number, content: string): Promise<CommentResponse> {
    const response = await apiClient.put<CommentResponse>(`/comments/${commentId}`, { content })
    return response.data
  },

  /**
   * 코멘트 삭제
   */
  async deleteComment(commentId: number): Promise<void> {
    await apiClient.delete(`/comments/${commentId}`)
  }
}

export default commentApi
