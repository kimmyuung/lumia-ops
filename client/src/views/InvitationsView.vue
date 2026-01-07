<template>
  <div class="invitations-view">
    <div class="container">
      <PageHeader title="받은 초대" description="팀 초대 목록" :icon="Mail" />

      <template v-if="isLoading">
        <div class="invitation-grid">
          <SkeletonCard v-for="i in 3" :key="i" />
        </div>
      </template>

      <template v-else-if="invitations.length > 0">
        <div class="invitation-grid">
          <Card v-for="invitation in invitations" :key="invitation.id" class="invitation-card">
            <div class="invitation-header">
              <div class="team-avatar">
                <Users :size="24" />
              </div>
              <div class="invitation-info">
                <h3>{{ invitation.teamName }}</h3>
                <p>{{ invitation.inviterName }}님이 초대했습니다</p>
              </div>
            </div>

            <div class="invitation-details">
              <div class="detail-item">
                <span class="label">역할</span>
                <span class="value role-badge">{{ getRoleLabel(invitation.role) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">초대일</span>
                <span class="value">{{ formatDate(invitation.createdAt) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">만료일</span>
                <span
                  class="value"
                  :class="{ 'expiring-soon': isExpiringSoon(invitation.expiresAt) }"
                >
                  {{ formatDate(invitation.expiresAt) }}
                </span>
              </div>
            </div>

            <div class="invitation-actions">
              <Button
                variant="primary"
                @click="accept(invitation)"
                :loading="processingId === invitation.id"
              >
                <Check :size="18" />
                <span>수락</span>
              </Button>
              <Button
                variant="secondary"
                @click="decline(invitation)"
                :disabled="processingId === invitation.id"
              >
                <X :size="18" />
                <span>거절</span>
              </Button>
            </div>
          </Card>
        </div>
      </template>

      <template v-else>
        <div class="empty-state">
          <div class="empty-icon">
            <Mail :size="64" />
          </div>
          <h2>받은 초대가 없습니다</h2>
          <p>팀장이 초대를 보내면 여기에 표시됩니다.</p>
          <Button variant="secondary" @click="goHome">
            <Home :size="18" />
            <span>홈으로</span>
          </Button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Mail, Users, Check, X, Home } from 'lucide-vue-next'
import { Card, Button, PageHeader, SkeletonCard } from '@/components/common'
import { invitationApi } from '@/api/invitation'
import type { TeamInvitation } from '@/types/invitation'
import { formatDate } from '@/utils/formatters'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()

const invitations = ref<TeamInvitation[]>([])
const isLoading = ref(false)
const processingId = ref<string | null>(null)

onMounted(() => {
  fetchInvitations()
})

async function fetchInvitations() {
  isLoading.value = true
  try {
    invitations.value = await invitationApi.getMyInvitations()
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '초대 목록을 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

function getRoleLabel(role: string): string {
  return role === 'ADMIN' ? '관리자' : '멤버'
}

function isExpiringSoon(expiresAt: string): boolean {
  const expiry = new Date(expiresAt)
  const now = new Date()
  const diffDays = (expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
  return diffDays <= 2
}

async function accept(invitation: TeamInvitation) {
  processingId.value = invitation.id
  try {
    await invitationApi.acceptInvitation(invitation.token)
    toast.success(`${invitation.teamName} 팀에 참여했습니다!`)
    router.push(`/team`)
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '초대 수락에 실패했습니다.')
  } finally {
    processingId.value = null
  }
}

async function decline(invitation: TeamInvitation) {
  if (!confirm(`${invitation.teamName} 팀의 초대를 거절하시겠습니까?`)) return

  processingId.value = invitation.id
  try {
    await invitationApi.declineInvitation(invitation.token)
    invitations.value = invitations.value.filter(i => i.id !== invitation.id)
    toast.info('초대를 거절했습니다.')
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '초대 거절에 실패했습니다.')
  } finally {
    processingId.value = null
  }
}

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.invitations-view {
  min-height: 100%;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--page-padding);
}

.invitation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.invitation-card {
  padding: 1.5rem;
}

.invitation-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.team-avatar {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: var(--radius-md);
  color: white;
}

.invitation-info h3 {
  margin: 0 0 0.25rem;
  color: var(--text-color);
}

.invitation-info p {
  margin: 0;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.invitation-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1.25rem;
  padding: 1rem;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
}

.detail-item .label {
  color: var(--text-muted);
}

.detail-item .value {
  color: var(--text-color);
  font-weight: 500;
}

.role-badge {
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary-color);
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm);
}

.expiring-soon {
  color: var(--warning-color) !important;
}

.invitation-actions {
  display: flex;
  gap: 0.75rem;
}

.invitation-actions .btn {
  flex: 1;
  justify-content: center;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--card-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border-radius: 50%;
  color: var(--primary-color);
}

.empty-state h2 {
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: 2rem;
}
</style>
