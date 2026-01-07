<template>
  <div class="pending-invitations">
    <div class="section-header">
      <h4>
        <Clock :size="18" />
        <span>대기 중인 초대 ({{ invitations?.length ?? 0 }})</span>
      </h4>
      <Button v-if="!isLoading" variant="ghost" size="sm" @click="refresh">
        <RefreshCw :size="16" />
      </Button>
    </div>

    <template v-if="isLoading">
      <Skeleton class="skeleton-item" v-for="i in 2" :key="i" />
    </template>

    <template v-else-if="invitations?.length > 0">
      <div class="invitation-list">
        <div v-for="invitation in invitations" :key="invitation.id" class="invitation-item">
          <div class="invitation-info">
            <div class="invitation-email">
              <Mail :size="16" />
              <span>{{ invitation.invitedEmail }}</span>
            </div>
            <div class="invitation-meta">
              <span class="invitation-role">{{ getRoleLabel(invitation.role) }}</span>
              <span class="invitation-date">{{ formatRelativeTime(invitation.createdAt) }}</span>
            </div>
          </div>
          <div class="invitation-actions">
            <Button
              variant="ghost"
              size="sm"
              :loading="resendingId === invitation.id"
              @click="resend(invitation)"
              title="재발송"
            >
              <Send :size="14" />
            </Button>
            <Button variant="ghost" size="sm" @click="cancel(invitation)" title="취소">
              <X :size="14" />
            </Button>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <p class="empty-message">대기 중인 초대가 없습니다.</p>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Clock, Mail, Send, X, RefreshCw } from 'lucide-vue-next'
import { Button, Skeleton } from '@/components/common'
import { invitationApi } from '@/api/invitation'
import type { TeamInvitation } from '@/types/invitation'
import { formatRelativeTime } from '@/utils/formatters'
import { useToast } from '@/composables/useToast'

interface Props {
  teamId: string
}

const props = defineProps<Props>()

const toast = useToast()

const invitations = ref<TeamInvitation[]>([])
const isLoading = ref(false)
const resendingId = ref<string | null>(null)

onMounted(() => {
  fetchInvitations()
})

async function fetchInvitations() {
  isLoading.value = true
  try {
    invitations.value = await invitationApi.getTeamInvitations(props.teamId)
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '초대 목록을 불러오지 못했습니다.')
  } finally {
    isLoading.value = false
  }
}

function refresh() {
  fetchInvitations()
}

function getRoleLabel(role: string): string {
  return role === 'ADMIN' ? '관리자' : '멤버'
}

async function resend(invitation: TeamInvitation) {
  resendingId.value = invitation.id
  try {
    await invitationApi.resendInvitation(props.teamId, invitation.id)
    toast.success('초대 이메일을 재발송했습니다.')
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '재발송에 실패했습니다.')
  } finally {
    resendingId.value = null
  }
}

async function cancel(invitation: TeamInvitation) {
  if (!confirm(`${invitation.invitedEmail}에 대한 초대를 취소하시겠습니까?`)) return

  try {
    await invitationApi.cancelInvitation(props.teamId, invitation.id)
    invitations.value = invitations.value.filter(i => i.id !== invitation.id)
    toast.success('초대를 취소했습니다.')
  } catch (err: unknown) {
    const e = err as { message?: string }
    toast.error(e.message || '취소에 실패했습니다.')
  }
}

// 외부에서 새로고침 가능
defineExpose({ refresh })
</script>

<style scoped>
.pending-invitations {
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
  padding: 1rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h4 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-color);
}

.skeleton-item {
  height: 48px;
  margin-bottom: 0.5rem;
  border-radius: var(--radius-md);
}

.invitation-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.invitation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  background: var(--card-bg-solid);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.invitation-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.invitation-email {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 500;
  color: var(--text-color);
}

.invitation-meta {
  display: flex;
  gap: 0.75rem;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.invitation-role {
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary-color);
  padding: 0.125rem 0.375rem;
  border-radius: var(--radius-sm);
}

.invitation-actions {
  display: flex;
  gap: 0.25rem;
}

.empty-message {
  text-align: center;
  color: var(--text-muted);
  font-size: 0.875rem;
  padding: 1rem 0;
}
</style>
