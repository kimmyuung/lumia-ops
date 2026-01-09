<template>
  <div class="team-settings-view">
    <header class="page-header">
      <router-link to="/team" class="back-link">← 팀으로 돌아가기</router-link>
      <h1>⚙️ 팀 설정</h1>
    </header>

    <div v-if="isLoading" class="loading-state">
      <Spinner />
      <p>설정을 불러오는 중...</p>
    </div>

    <template v-else-if="team">
      <!-- 기본 정보 -->
      <section class="settings-section">
        <h2>팀 정보</h2>

        <div class="form-group">
          <label>팀 이름</label>
          <input v-model="teamName" type="text" placeholder="팀 이름" />
        </div>

        <div class="form-group">
          <label>팀 설명</label>
          <textarea v-model="teamDescription" placeholder="팀 설명 (선택사항)" rows="3"></textarea>
        </div>

        <button class="primary-btn" :disabled="isSaving" @click="handleSaveTeamInfo">
          {{ isSaving ? '저장 중...' : '팀 정보 저장' }}
        </button>
      </section>

      <!-- 디스코드 웹훅 -->
      <section class="settings-section">
        <h2>🔗 디스코드 연동</h2>
        <p class="section-desc">
          디스코드 웹훅 URL을 설정하면 스크림 시작/종료, 새 멤버 가입 등의 알림을 디스코드 채널로
          받을 수 있습니다.
        </p>

        <div class="webhook-guide">
          <h4>웹훅 URL 얻는 방법:</h4>
          <ol>
            <li>디스코드 서버 설정 → 연동 → 웹후크</li>
            <li>새 웹후크 만들기</li>
            <li>웹후크 URL 복사</li>
          </ol>
        </div>

        <div class="form-group">
          <label>웹훅 URL</label>
          <input
            v-model="discordWebhookUrl"
            type="url"
            placeholder="https://discord.com/api/webhooks/..."
          />
        </div>

        <div class="button-group">
          <button class="primary-btn" :disabled="isSaving" @click="handleSaveWebhook">
            {{ isSaving ? '저장 중...' : '웹훅 저장' }}
          </button>
          <button
            class="secondary-btn"
            :disabled="!discordWebhookUrl || isTesting"
            @click="handleTestWebhook"
          >
            {{ isTesting ? '테스트 중...' : '테스트 전송' }}
          </button>
        </div>
      </section>

      <!-- 위험 영역 -->
      <section class="settings-section danger-zone">
        <h2>⚠️ 위험 영역</h2>

        <div class="danger-item">
          <div class="danger-info">
            <h4>팀 삭제</h4>
            <p>팀을 삭제하면 모든 데이터가 영구적으로 삭제됩니다.</p>
          </div>
          <button class="danger-btn" @click="handleDeleteTeam">팀 삭제</button>
        </div>
      </section>
    </template>

    <div v-else class="error-state">
      <p>팀 정보를 불러올 수 없습니다.</p>
      <router-link to="/team" class="primary-btn">팀 페이지로 이동</router-link>
    </div>

    <!-- 메시지 -->
    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="success" class="message success">{{ success }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTeamStore } from '@/stores/team'
import Spinner from '@/components/common/Spinner.vue'
import apiClient from '@/api/client'
import { getErrorMessage } from '@/utils/error'

const router = useRouter()
const teamStore = useTeamStore()

const team = computed(() => teamStore.currentTeam)
const isLoading = ref(true)
const isSaving = ref(false)
const isTesting = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const teamName = ref('')
const teamDescription = ref('')
const discordWebhookUrl = ref('')

async function handleSaveTeamInfo() {
  if (!team.value) return

  isSaving.value = true
  error.value = null
  success.value = null

  try {
    await apiClient.put(`/teams/${team.value.id}`, {
      name: teamName.value.trim(),
      description: teamDescription.value.trim() || null
    })
    success.value = '팀 정보가 저장되었습니다.'
    await teamStore.fetchMyTeam()
  } catch (err) {
    error.value = getErrorMessage(err, '저장에 실패했습니다.')
  } finally {
    isSaving.value = false
  }
}

async function handleSaveWebhook() {
  if (!team.value) return

  isSaving.value = true
  error.value = null
  success.value = null

  try {
    await apiClient.put(`/teams/${team.value.id}/webhook`, {
      discordWebhookUrl: discordWebhookUrl.value.trim() || null
    })
    success.value = '디스코드 웹훅이 저장되었습니다.'
    await teamStore.fetchMyTeam()
  } catch (err) {
    error.value = getErrorMessage(err, '저장에 실패했습니다.')
  } finally {
    isSaving.value = false
  }
}

async function handleTestWebhook() {
  if (!team.value || !discordWebhookUrl.value) return

  isTesting.value = true
  error.value = null
  success.value = null

  try {
    await apiClient.post(`/teams/${team.value.id}/webhook/test`)
    success.value = '테스트 메시지가 전송되었습니다. 디스코드를 확인하세요!'
  } catch (err) {
    error.value = getErrorMessage(err, '테스트 전송에 실패했습니다.')
  } finally {
    isTesting.value = false
  }
}

async function handleDeleteTeam() {
  if (!team.value) return

  const confirmed = confirm(
    `정말로 "${team.value.name}" 팀을 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`
  )

  if (!confirmed) return

  try {
    await apiClient.delete(`/teams/${team.value.id}`)
    await teamStore.fetchMyTeam()
    router.push('/team')
  } catch (err) {
    error.value = getErrorMessage(err, '팀 삭제에 실패했습니다.')
  }
}

onMounted(async () => {
  isLoading.value = true
  await teamStore.fetchMyTeam()

  if (team.value) {
    teamName.value = team.value.name || ''
    teamDescription.value = team.value.description || ''
    discordWebhookUrl.value = team.value.discordWebhookUrl || ''
  }

  isLoading.value = false
})
</script>

<style scoped>
.team-settings-view {
  max-width: 700px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.back-link {
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.875rem;
}

.back-link:hover {
  color: var(--primary-color);
}

.page-header h1 {
  margin: 0.5rem 0 0;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4rem;
  color: var(--text-muted);
}

.settings-section {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.settings-section h2 {
  margin: 0 0 0.5rem;
  font-size: 1.125rem;
}

.section-desc {
  margin: 0 0 1rem;
  color: var(--text-muted);
  font-size: 0.875rem;
}

.webhook-guide {
  background: var(--bg-color-alt);
  padding: 1rem;
  border-radius: var(--radius-md);
  margin-bottom: 1rem;
}

.webhook-guide h4 {
  margin: 0 0 0.5rem;
  font-size: 0.875rem;
}

.webhook-guide ol {
  margin: 0;
  padding-left: 1.25rem;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  font-size: 0.875rem;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: var(--primary-color);
}

.button-group {
  display: flex;
  gap: 0.75rem;
}

.primary-btn {
  padding: 0.75rem 1.5rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-weight: 500;
  cursor: pointer;
}

.primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.secondary-btn {
  padding: 0.75rem 1.5rem;
  background: var(--bg-color-alt);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-weight: 500;
  cursor: pointer;
}

.secondary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.danger-zone {
  border-color: rgba(var(--error-rgb), 0.3);
}

.danger-zone h2 {
  color: var(--error-color);
}

.danger-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.danger-info h4 {
  margin: 0 0 0.25rem;
}

.danger-info p {
  margin: 0;
  color: var(--text-muted);
  font-size: 0.875rem;
}

.danger-btn {
  padding: 0.5rem 1rem;
  background: var(--error-color);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  white-space: nowrap;
}

.message {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  padding: 1rem 1.5rem;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.message.error {
  background: var(--error-color);
  color: white;
}

.message.success {
  background: var(--success-color);
  color: white;
}
</style>
