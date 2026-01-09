import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationApi, type NotificationResponse } from '@/api/notification'
import { useStompClient } from '@/composables/useStompClient'
import { getErrorMessage } from '@/utils/error'

export const useNotificationStore = defineStore('notification', () => {
  // 상태
  const notifications = ref<NotificationResponse[]>([])
  const unreadCount = ref(0)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // STOMP 클라이언트 (실시간 알림)
  const { isConnected, subscribe, connect, disconnect } = useStompClient({
    autoConnect: false
  })

  // 게터
  const hasUnread = computed(() => unreadCount.value > 0)
  const recentNotifications = computed(() => notifications.value.slice(0, 5))

  // 액션
  async function fetchNotifications(page = 0, size = 20) {
    isLoading.value = true
    error.value = null
    try {
      notifications.value = await notificationApi.getNotifications(page, size)
    } catch (err) {
      error.value = getErrorMessage(err, '알림을 불러오지 못했습니다.')
    } finally {
      isLoading.value = false
    }
  }

  async function fetchUnreadCount() {
    try {
      unreadCount.value = await notificationApi.getUnreadCount()
    } catch {
      // 조용히 실패
    }
  }

  async function markAsRead(id: number) {
    try {
      const success = await notificationApi.markAsRead(id)
      if (success) {
        const notification = notifications.value.find(n => n.id === id)
        if (notification && !notification.isRead) {
          notification.isRead = true
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
      }
      return success
    } catch (err) {
      error.value = getErrorMessage(err, '읽음 처리에 실패했습니다.')
      return false
    }
  }

  async function markAllAsRead() {
    try {
      const count = await notificationApi.markAllAsRead()
      notifications.value.forEach(n => {
        n.isRead = true
      })
      unreadCount.value = 0
      return count
    } catch (err) {
      error.value = getErrorMessage(err, '읽음 처리에 실패했습니다.')
      return 0
    }
  }

  // WebSocket 실시간 알림 구독
  function subscribeToNotifications(userId: number) {
    if (!isConnected.value) {
      connect()
    }

    // 연결 후 구독
    const checkConnection = setInterval(() => {
      if (isConnected.value) {
        clearInterval(checkConnection)
        subscribe<NotificationResponse>(`/user/${userId}/queue/notifications`, message => {
          // 새 알림 추가
          notifications.value.unshift(message.body)
          unreadCount.value++

          // 브라우저 알림 (권한이 있는 경우)
          if (Notification.permission === 'granted') {
            new Notification(message.body.title, {
              body: message.body.message,
              icon: '/favicon.ico'
            })
          }
        })
      }
    }, 100)

    // 10초 후 타임아웃
    setTimeout(() => clearInterval(checkConnection), 10000)
  }

  function disconnectNotifications() {
    disconnect()
  }

  // 브라우저 알림 권한 요청
  async function requestNotificationPermission() {
    if ('Notification' in window && Notification.permission === 'default') {
      const permission = await Notification.requestPermission()
      return permission === 'granted'
    }
    return Notification.permission === 'granted'
  }

  function clearError() {
    error.value = null
  }

  return {
    // 상태
    notifications,
    unreadCount,
    isLoading,
    error,
    isConnected,
    // 게터
    hasUnread,
    recentNotifications,
    // 액션
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllAsRead,
    subscribeToNotifications,
    disconnectNotifications,
    requestNotificationPermission,
    clearError
  }
})
