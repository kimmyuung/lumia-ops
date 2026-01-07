<template>
  <Modal v-model="isOpen" title="초대 코드로 팀 참여" max-width="420px">
    <form class="join-form" @submit.prevent="handleSubmit">
      <p class="description">팀에서 받은 초대 코드 또는 초대 링크를 입력하세요.</p>

      <div class="form-field">
        <label for="inviteCode">초대 코드 또는 링크</label>
        <Input
          id="inviteCode"
          v-model="inviteCode"
          placeholder="초대 코드 또는 링크를 입력하세요"
          :error="error"
          :disabled="isLoading"
        />
      </div>

      <div v-if="invitationInfo" class="invitation-preview">
        <div class="preview-header">
          <Users :size="20" />
          <span>초대 정보</span>
        </div>
        <div class="preview-content">
          <div class="preview-item">
            <span class="label">팀 이름</span>
            <span class="value">{{ invitationInfo.teamName }}</span>
          </div>
          <div v-if="invitationInfo.inviterName" class="preview-item">
            <span class="label">초대자</span>
            <span class="value">{{ invitationInfo.inviterName }}</span>
          </div>
        </div>
      </div>
    </form>

    <template #footer>
      <Button variant="secondary" @click="close" :disabled="isLoading"> 취소 </Button>
      <Button
        variant="primary"
        @click="handleSubmit"
        :loading="isLoading"
        :disabled="!inviteCode.trim()"
      >
        <UserPlus :size="18" />
        <span>팀 참여</span>
      </Button>
    </template>
  </Modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Users, UserPlus } from 'lucide-vue-next'
import { Modal, Button, Input } from '@/components/common'
import { invitationApi } from '@/api/invitation'
import { useTeamStore } from '@/stores/team'
import { useToast } from '@/composables/useToast'
import { useRouter } from 'vue-router'

interface InvitationPreview {
  teamName: string
  inviterName?: string
}

const emit = defineEmits<{
  close: []
  joined: [teamId: string]
}>()

const router = useRouter()
const teamStore = useTeamStore()
const toast = useToast()

const isOpen = ref(true)
const inviteCode = ref('')
const error = ref('')
const isLoading = ref(false)
const invitationInfo = ref<InvitationPreview | null>(null)

// 초대 코드에서 토큰 추출
function extractToken(input: string): string {
  const trimmed = input.trim()

  // URL 형태인 경우 토큰 추출
  if (trimmed.includes('/invite/')) {
    const match = trimmed.match(/\/invite\/([a-zA-Z0-9_-]+)/)
    if (match && match[1]) {
      return match[1]
    }
  }

  // 그 외의 경우 입력값 그대로 반환
  return trimmed
}

// 초대 코드 입력 시 미리보기 로드
let debounceTimer: ReturnType<typeof setTimeout> | null = null

watch(inviteCode, newValue => {
  error.value = ''
  invitationInfo.value = null

  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  const token = extractToken(newValue)
  if (token.length >= 10) {
    debounceTimer = setTimeout(async () => {
      await loadInvitationPreview(token)
    }, 500)
  }
})

async function loadInvitationPreview(token: string) {
  try {
    const invitation = await invitationApi.getInvitationByToken(token)
    invitationInfo.value = {
      teamName: invitation.teamName,
      inviterName: invitation.inviterName
    }
  } catch {
    // 미리보기 로드 실패는 무시 (입력 중일 수 있음)
  }
}

async function handleSubmit() {
  const token = extractToken(inviteCode.value)

  if (!token) {
    error.value = '초대 코드를 입력해 주세요.'
    return
  }

  isLoading.value = true
  error.value = ''

  try {
    const result = await invitationApi.acceptInvitation(token)

    toast.success('팀에 성공적으로 참여했습니다!')

    // 팀 정보 새로고침
    await teamStore.fetchMyTeam()

    emit('joined', result.teamId)
    close()

    // 팀 페이지로 이동
    router.push('/team')
  } catch (err: unknown) {
    const apiError = err as { message?: string; code?: string }

    if (apiError.code === 'INVITATION_EXPIRED') {
      error.value = '초대가 만료되었습니다. 새로운 초대를 요청해 주세요.'
    } else if (apiError.code === 'INVITATION_NOT_FOUND') {
      error.value = '유효하지 않은 초대 코드입니다.'
    } else if (apiError.code === 'ALREADY_TEAM_MEMBER') {
      error.value = '이미 이 팀의 멤버입니다.'
    } else if (apiError.code === 'ALREADY_HAS_TEAM') {
      error.value = '이미 다른 팀에 소속되어 있습니다. 현재 팀을 먼저 탈퇴해 주세요.'
    } else {
      error.value = apiError.message || '팀 참여에 실패했습니다.'
    }
  } finally {
    isLoading.value = false
  }
}

function close() {
  isOpen.value = false
  emit('close')
}
</script>

<style scoped>
.join-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.description {
  color: var(--text-muted);
  font-size: 0.9rem;
  margin: 0;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-field label {
  font-weight: 500;
  color: var(--text-color);
  font-size: 0.9rem;
}

.invitation-preview {
  background: var(--bg-color-alt);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: var(--primary-color);
  font-weight: 600;
  font-size: 0.875rem;
  border-bottom: 1px solid var(--border-color);
}

.preview-content {
  padding: 0.75rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-item .label {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.preview-item .value {
  color: var(--text-color);
  font-weight: 500;
}
</style>
