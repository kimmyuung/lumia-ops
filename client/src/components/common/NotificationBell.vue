<template>
  <div class="notification-bell">
    <button class="bell-button" :class="{ 'has-unread': hasUnread }" @click="toggleDropdown">
      <Bell :size="22" />
      <span v-if="hasUnread" class="badge">{{ displayCount }}</span>
    </button>

    <Transition name="dropdown">
      <div v-if="isOpen" ref="dropdownRef" class="dropdown">
        <div class="dropdown-header">
          <h4>알림</h4>
          <button v-if="hasUnread" class="mark-all-btn" @click="handleMarkAllAsRead">
            모두 읽음
          </button>
        </div>

        <div v-if="!isLoading" class="notification-list">
          <div
            v-for="notification in recentNotifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.isRead }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-icon">
              <component :is="getIcon(notification.type)" :size="18" />
            </div>
            <div class="notification-content">
              <p class="notification-title">{{ notification.title }}</p>
              <p class="notification-message">{{ notification.message }}</p>
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
          </div>

          <div v-if="recentNotifications.length === 0" class="empty-state">
            <BellOff :size="32" />
            <p>알림이 없습니다</p>
          </div>
        </div>

        <div v-else class="loading-state">
          <span>불러오는 중...</span>
        </div>

        <div v-if="recentNotifications.length > 0" class="dropdown-footer">
          <router-link to="/notifications" @click="isOpen = false"> 모든 알림 보기 </router-link>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, BellOff, Users, Swords, MapPin, MessageCircle, Info } from 'lucide-vue-next'
import { useNotificationStore } from '@/stores/notification'
import { useUserStore } from '@/stores/user'
import type { NotificationResponse, NotificationType } from '@/api/notification'

const router = useRouter()
const notificationStore = useNotificationStore()
const userStore = useUserStore()

const isOpen = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

const isLoading = computed(() => notificationStore.isLoading)
const hasUnread = computed(() => notificationStore.hasUnread)
const recentNotifications = computed(() => notificationStore.recentNotifications)
const displayCount = computed(() => {
  const count = notificationStore.unreadCount
  return count > 99 ? '99+' : count.toString()
})

function toggleDropdown() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    notificationStore.fetchNotifications()
  }
}

function handleClickOutside(event: MouseEvent) {
  if (dropdownRef.value && !dropdownRef.value.contains(event.target as Node)) {
    const button = (event.target as Element).closest('.bell-button')
    if (!button) {
      isOpen.value = false
    }
  }
}

async function handleMarkAllAsRead() {
  await notificationStore.markAllAsRead()
}

async function handleNotificationClick(notification: NotificationResponse) {
  if (!notification.isRead) {
    await notificationStore.markAsRead(notification.id)
  }

  // 관련 페이지로 이동
  if (notification.relatedType && notification.relatedId) {
    const route = getRelatedRoute(notification.relatedType, notification.relatedId)
    if (route) {
      isOpen.value = false
      router.push(route)
    }
  }
}

function getRelatedRoute(type: string, id: number): string | null {
  switch (type) {
    case 'TEAM':
      return `/team`
    case 'SCRIM':
      return `/scrim`
    case 'STRATEGY':
      return `/strategy`
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
  document.addEventListener('click', handleClickOutside)
  notificationStore.fetchUnreadCount()

  // 로그인 상태면 WebSocket 구독
  if (userStore.user?.id) {
    notificationStore.subscribeToNotifications(Number(userStore.user.id))
    notificationStore.requestNotificationPermission()
  }
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
}

.bell-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.bell-button:hover {
  background: var(--bg-color-alt);
  color: var(--text-color);
}

.bell-button.has-unread {
  color: var(--primary-color);
}

.badge {
  position: absolute;
  top: 4px;
  right: 4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--error-color);
  color: white;
  font-size: 0.65rem;
  font-weight: 600;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 360px;
  max-height: 480px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1000;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.dropdown-header h4 {
  margin: 0;
  font-size: 1rem;
}

.mark-all-btn {
  background: none;
  border: none;
  color: var(--primary-color);
  font-size: 0.875rem;
  cursor: pointer;
}

.mark-all-btn:hover {
  text-decoration: underline;
}

.notification-list {
  max-height: 360px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--border-color);
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background: var(--bg-color-alt);
}

.notification-item.unread {
  background: rgba(var(--primary-rgb), 0.05);
}

.notification-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
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

.notification-title {
  margin: 0 0 0.25rem;
  font-weight: 600;
  font-size: 0.875rem;
}

.notification-message {
  margin: 0;
  font-size: 0.8rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notification-time {
  font-size: 0.7rem;
  color: var(--text-muted);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  color: var(--text-muted);
}

.empty-state p {
  margin-top: 0.5rem;
}

.loading-state {
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
}

.dropdown-footer {
  padding: 0.75rem;
  border-top: 1px solid var(--border-color);
  text-align: center;
}

.dropdown-footer a {
  color: var(--primary-color);
  font-size: 0.875rem;
  text-decoration: none;
}

.dropdown-footer a:hover {
  text-decoration: underline;
}

/* Transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 480px) {
  .dropdown {
    width: calc(100vw - 2rem);
    right: -1rem;
  }
}
</style>
