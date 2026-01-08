import apiClient from './client'

// ==================== 타입 정의 ====================

export interface NotificationResponse {
    id: number
    type: NotificationType
    title: string
    message: string
    relatedId: number | null
    relatedType: string | null
    isRead: boolean
    createdAt: string
}

export type NotificationType =
    | 'TEAM_INVITE'
    | 'TEAM_JOIN'
    | 'TEAM_LEAVE'
    | 'SCRIM_SCHEDULED'
    | 'SCRIM_STARTED'
    | 'SCRIM_FINISHED'
    | 'MATCH_RESULT'
    | 'STRATEGY_SHARED'
    | 'COMMENT_ADDED'
    | 'GENERAL'

// ==================== API 함수 ====================

export const notificationApi = {
    /**
     * 알림 목록 조회
     */
    async getNotifications(page = 0, size = 20): Promise<NotificationResponse[]> {
        const response = await apiClient.get<NotificationResponse[]>('/notifications', {
            params: { page, size }
        })
        return response.data
    },

    /**
     * 읽지 않은 알림 조회
     */
    async getUnreadNotifications(): Promise<NotificationResponse[]> {
        const response = await apiClient.get<NotificationResponse[]>('/notifications/unread')
        return response.data
    },

    /**
     * 읽지 않은 알림 개수
     */
    async getUnreadCount(): Promise<number> {
        const response = await apiClient.get<{ count: number }>('/notifications/unread/count')
        return response.data.count
    },

    /**
     * 알림 읽음 처리
     */
    async markAsRead(id: number): Promise<boolean> {
        const response = await apiClient.patch<{ success: boolean }>(`/notifications/${id}/read`)
        return response.data.success
    },

    /**
     * 모든 알림 읽음 처리
     */
    async markAllAsRead(): Promise<number> {
        const response = await apiClient.patch<{ updated: number }>('/notifications/read-all')
        return response.data.updated
    }
}

export default notificationApi
