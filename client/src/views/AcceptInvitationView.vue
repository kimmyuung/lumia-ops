<template>
  <div class="accept-invitation-view">
    <div class="container">
      <!-- 로딩 -->
      <template v-if="isLoading">
        <div class="loading-state">
          <Loading size="lg" />
          <p>초대 정보를 확인하는 중...</p>
        </div>
      </template>

      <!-- 에러 -->
      <template v-else-if="error">
        <div class="error-state">
          <div class="error-icon">
            <AlertTriangle :size="64" />
          </div>
          <h2>{{ error }}</h2>
          <p>링크가 만료되었거나 잘못되었을 수 있습니다.</p>
          <Button variant="primary" @click="goHome">
            <Home :size="18" />
            <span>홈으로</span>
          </Button>
        </div>
      </template>

      <!-- 성공 -->
      <template v-else-if="accepted">
        <div class="success-state">
          <div class="success-icon">
            <CheckCircle :size="64" />
          </div>
          <h2>환영합니다!</h2>
          <p>{{ invitation?.teamName }} 팀에 성공적으로 참여했습니다.</p>
          <Button variant="primary" @click="goToTeam">
            <Users :size="18" />
            <span>팀 페이지로 이동</span>
          </Button>
        </div>
      </template>

      <!-- 초대 정보 -->
      <template v-else-if="invitation">
        <Card class="invitation-card">
          <div class="invitation-header">
            <div class="team-avatar">
              <Users :size="32" />
            </div>
            <div class="invitation-title">
              <h1>팀 초대</h1>
              <p>{{ invitation.inviterName }}님이 팀에 초대했습니다</p>
            </div>
          </div>

          <div class="team-info">
            <h2>{{ invitation.teamName }}</h2>
            <span class="role-badge">{{ getRoleLabel(invitation.role) }}로 초대됨</span>
          </div>

          <div class="invitation-meta">
            <div class="meta-item">
              <Clock :size="16" />
              <span>만료: {{ formatDate(invitation.expiresAt) }}</span>
            </div>
          </div>

          <div class="action-buttons">
            <Button variant="primary" @click="accept" :loading="isProcessing" class="accept-btn">
              <Check :size="20" />
              <span>초대 수락하기</span>
            </Button>
            <Button variant="secondary" @click="decline" :disabled="isProcessing">
              <X :size="20" />
              <span>거절</span>
            </Button>
          </div>
        </Card>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Users, Check, X, Clock, Home, CheckCircle, AlertTriangle } from 'lucide-vue-next'
import { Card, Button, Loading } from '@/components/common'
import { invitationApi } from '@/api/invitation'
import type { TeamInvitation } from '@/types/invitation'
import { formatDate } from '@/utils/formatters'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const toast = useToast()

const invitation = ref<TeamInvitation | null>(null)
const isLoading = ref(true)
const isProcessing = ref(false)
const error = ref<string | null>(null)
const accepted = ref(false)

const token = route.params.token as string

onMounted(() => {
  fetchInvitation()
})

async function fetchInvitation() {
  isLoading.value = true
  try {
    invitation.value = await invitationApi.getInvitationByToken(token)

    if (invitation.value.status !== 'PENDING') {
      error.value = '이미 처리된 초대입니다.'
    }
  } catch (err: unknown) {
    error.value = '유효하지 않은 초대 링크입니다.'
  } finally {
    isLoading.value = false
  }
}

function getRoleLabel(role: string): string {
  return role === 'ADMIN' ? '관리자' : '멤버'
}

async function accept() {
  isProcessing.value = true
  try {
    await invitationApi.acceptInvitation(token)
    accepted.value = true
    toast.success('팀에 성공적으로 참여했습니다!')
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '초대 수락에 실패했습니다.')
  } finally {
    isProcessing.value = false
  }
}

async function decline() {
  if (!confirm('정말로 초대를 거절하시겠습니까?')) return

  isProcessing.value = true
  try {
    await invitationApi.declineInvitation(token)
    toast.info('초대를 거절했습니다.')
    router.push('/')
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '초대 거절에 실패했습니다.')
  } finally {
    isProcessing.value = false
  }
}

function goHome() {
  router.push('/')
}

function goToTeam() {
  router.push('/team')
}
</script>

<style scoped>
.accept-invitation-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.container {
  max-width: 480px;
  width: 100%;
}

.loading-state,
.error-state,
.success-state {
  text-align: center;
  padding: 3rem 2rem;
  background: var(--card-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
}

.loading-state p {
  margin-top: 1rem;
  color: var(--text-muted);
}

.error-icon,
.success-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.error-icon {
  background: rgba(239, 68, 68, 0.1);
  color: var(--error-color);
}

.success-icon {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
}

.error-state h2,
.success-state h2 {
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.error-state p,
.success-state p {
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.invitation-card {
  padding: 2rem;
}

.invitation-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
}

.team-avatar {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: var(--radius-lg);
  color: white;
}

.invitation-title h1 {
  font-size: 1.25rem;
  margin-bottom: 0.25rem;
  color: var(--text-color);
}

.invitation-title p {
  color: var(--text-muted);
  font-size: 0.9rem;
  margin: 0;
}

.team-info {
  text-align: center;
  padding: 1.5rem;
  background: var(--bg-color-alt);
  border-radius: var(--radius-lg);
  margin-bottom: 1.5rem;
}

.team-info h2 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.role-badge {
  display: inline-block;
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary-color);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  font-weight: 500;
}

.invitation-meta {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.accept-btn {
  flex: 2;
  justify-content: center;
}

.action-buttons button:last-child {
  flex: 1;
  justify-content: center;
}
</style>
