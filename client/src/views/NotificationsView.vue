<template>
  <div class="notifications-view">
    <header class="page-header">
      <h1>🔔 알림</h1>
      <button v-if="hasUnread" class="mark-all-btn" @click="handleMarkAllAsRead">
        모두 읽음 처리
      </button>
    </header>

    <div v-if="isLoading" class="loading-state">
      <Spinner />
      <p>알림을 불러오는 중...</p>
    </div>

    <div v-else-if="notifications.length === 0" class="empty-state">
      <BellOff :size="64" />
      <h3>알림이 없습니다</h3>
      <p>새로운 알림이 오면 여기에 표시됩니다.</p>
    </div>

    <div v-else class="notification-list">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item"
        :class="{ unread: !notification.isRead }"
        @click="handleNotificationClick(notification)"
      >
        <div class="notification-icon">
          <component :is="getIcon(notification.type)" :size="24" />
        </div>

        <div class="notification-content">
          <h4>{{ notification.title }}</h4>
          <p>{{ notification.message }}</p>
          <span class="time">{{ formatTime(notification.createdAt) }}</span>
        </div>

        <button
          v-if="!notification.isRead"
          class="read-btn"
          @click.stop="markAsRead(notification.id)"
        >
          읽음
        </button>
      </div>
    </div>

    <div v-if="error" class="error-banner">
      {{ error }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { BellOff, Users, Swords, MapPin, MessageCircle, Info } from 'lucide-vue-next'
import { useNotificationStore } from '@/stores/notification'
import Spinner from '@/components/common/Spinner.vue'
import type { NotificationResponse, NotificationType } from '@/api/notification'

const router = useRouter()
const notificationStore = useNotificationStore()

const notifications = computed(() => notificationStore.notifications)
const isLoading = computed(() => notificationStore.isLoading)
const hasUnread = computed(() => notificationStore.hasUnread)
const error = computed(() => notificationStore.error)

async function handleMarkAllAsRead() {
  await notificationStore.markAllAsRead()
}

async function handleNotificationClick(notification: NotificationResponse) {
  if (!notification.isRead) {
    await notificationStore.markAsRead(notification.id)
  }

  if (notification.relatedType && notification.relatedId) {
    const route = getRelatedRoute(notification.relatedType)
    if (route) router.push(route)
  }
}

async function markAsRead(id: number) {
  await notificationStore.markAsRead(id)
}

function getRelatedRoute(type: string): string | null {
  switch (type) {
    case 'TEAM':
      return '/team'
    case 'SCRIM':
      return '/scrim'
    case 'STRATEGY':
      return '/strategy'
    default:
      return null
  }
}

function getIcon(type: NotificationType) {
  switch (type) {
    case 'TEAM_INVITE':
    case 'TEAM_JOIN':
    case 'TEAM_LEAVE':
      return Users
    case 'SCRIM_SCHEDULED':
    case 'SCRIM_STARTED':
    case 'SCRIM_FINISHED':
    case 'MATCH_RESULT':
      return Swords
    case 'STRATEGY_SHARED':
      return MapPin
    case 'COMMENT_ADDED':
      return MessageCircle
    default:
      return Info
  }
}

function formatTime(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`
  return date.toLocaleDateString('ko-KR')
}

onMounted(() => {
  notificationStore.fetchNotifications()
})
</script>

<style scoped>
.notifications-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0;
}

.mark-all-btn {
  padding: 0.5rem 1rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4rem;
  color: var(--text-muted);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4rem;
  text-align: center;
  color: var(--text-muted);
}

.empty-state h3 {
  margin: 1rem 0 0.5rem;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.2s;
}

.notification-item:hover {
  background: var(--bg-color-alt);
}

.notification-item.unread {
  background: rgba(var(--primary-rgb), 0.05);
  border-color: rgba(var(--primary-rgb), 0.2);
}

.notification-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color-alt);
  border-radius: var(--radius-full);
  color: var(--primary-color);
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-content h4 {
  margin: 0 0 0.25rem;
  font-size: 1rem;
}

.notification-content p {
  margin: 0 0 0.5rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.notification-content .time {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.read-btn {
  padding: 0.25rem 0.75rem;
  background: var(--bg-color-alt);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 0.75rem;
  cursor: pointer;
}

.read-btn:hover {
  background: var(--primary-color);
  color: white;
}

.error-banner {
  margin-top: 1rem;
  padding: 1rem;
  background: rgba(var(--error-rgb), 0.1);
  color: var(--error-color);
  border-radius: var(--radius-md);
}
</style>
