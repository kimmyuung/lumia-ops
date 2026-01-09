<template>
  <div class="profile-view">
    <header class="page-header">
      <h1>👤 프로필</h1>
    </header>

    <div v-if="isLoading" class="loading-state">
      <Spinner />
      <p>프로필을 불러오는 중...</p>
    </div>

    <template v-else-if="user">
      <!-- 기본 정보 -->
      <section class="profile-section">
        <h2>기본 정보</h2>
        <div class="info-card">
          <div class="avatar">
            {{ user.nickname?.charAt(0).toUpperCase() || '?' }}
          </div>
          <div class="info-content">
            <div class="info-row">
              <span class="label">닉네임</span>
              <span class="value">{{ user.nickname || '미설정' }}</span>
            </div>
            <div class="info-row">
              <span class="label">이메일</span>
              <span class="value">{{ user.email || '없음' }}</span>
            </div>
            <div class="info-row">
              <span class="label">가입일</span>
              <span class="value">{{ formatDate(user.createdAt) }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 닉네임 변경 -->
      <section class="profile-section">
        <h2>닉네임 변경</h2>
        <p class="section-desc">닉네임은 30일에 한 번 변경할 수 있습니다.</p>

        <div class="form-group">
          <label>새 닉네임</label>
          <input
            v-model="newNickname"
            type="text"
            placeholder="새 닉네임 입력"
            :disabled="!canChangeNickname"
          />
          <span v-if="!canChangeNickname" class="help-text">
            {{ daysUntilChange }}일 후 변경 가능
          </span>
        </div>

        <button
          class="primary-btn"
          :disabled="!canChangeNickname || !newNickname.trim() || isSaving"
          @click="handleNicknameChange"
        >
          {{ isSaving ? '저장 중...' : '닉네임 변경' }}
        </button>
      </section>

      <!-- 게임 닉네임 -->
      <section class="profile-section">
        <h2>이터널 리턴 닉네임</h2>
        <p class="section-desc">게임 내 닉네임을 설정하면 전적 조회가 가능합니다.</p>

        <div class="form-group">
          <label>게임 닉네임</label>
          <input v-model="gameNickname" type="text" placeholder="인게임 닉네임" />
        </div>

        <button class="primary-btn" :disabled="isSaving" @click="handleGameNicknameChange">
          {{ isSaving ? '저장 중...' : '게임 닉네임 저장' }}
        </button>
      </section>

      <!-- 연동 계정 -->
      <section class="profile-section">
        <h2>연동 계정</h2>
        <div class="linked-accounts">
          <div class="account-item">
            <span class="account-icon">🎮</span>
            <span class="account-name">Steam</span>
            <span :class="['account-status', { connected: user.steamId }]">
              {{ user.steamId ? '연동됨' : '미연동' }}
            </span>
          </div>
          <div class="account-item">
            <span class="account-icon">💬</span>
            <span class="account-name">Kakao</span>
            <span :class="['account-status', { connected: user.kakaoId }]">
              {{ user.kakaoId ? '연동됨' : '미연동' }}
            </span>
          </div>
        </div>
      </section>
    </template>

    <!-- 에러/성공 메시지 -->
    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="success" class="message success">{{ success }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import Spinner from '@/components/common/Spinner.vue'
import apiClient from '@/api/client'
import { getErrorMessage } from '@/utils/error'

const userStore = useUserStore()

const user = computed(() => userStore.user)
const isLoading = ref(false)
const isSaving = ref(false)
const error = ref<string | null>(null)
const success = ref<string | null>(null)

const newNickname = ref('')
const gameNickname = ref('')

// 닉네임 변경 가능 여부 (30일 규칙)
const canChangeNickname = computed(() => {
  if (!user.value?.nicknameChangedAt) return true
  const changed = new Date(user.value.nicknameChangedAt)
  const now = new Date()
  const diff = now.getTime() - changed.getTime()
  return diff >= 30 * 24 * 60 * 60 * 1000
})

const daysUntilChange = computed(() => {
  if (!user.value?.nicknameChangedAt) return 0
  const changed = new Date(user.value.nicknameChangedAt)
  const now = new Date()
  const diff = 30 - Math.floor((now.getTime() - changed.getTime()) / (24 * 60 * 60 * 1000))
  return Math.max(0, diff)
})

async function handleNicknameChange() {
  if (!newNickname.value.trim()) return

  isSaving.value = true
  error.value = null
  success.value = null

  try {
    await apiClient.put('/users/me/nickname', { nickname: newNickname.value.trim() })
    success.value = '닉네임이 변경되었습니다.'
    newNickname.value = ''
    await userStore.fetchUser()
  } catch (err) {
    error.value = getErrorMessage(err, '닉네임 변경에 실패했습니다.')
  } finally {
    isSaving.value = false
  }
}

async function handleGameNicknameChange() {
  isSaving.value = true
  error.value = null
  success.value = null

  try {
    await apiClient.put('/users/me/game-nickname', { gameNickname: gameNickname.value.trim() })
    success.value = '게임 닉네임이 저장되었습니다.'
    await userStore.fetchUser()
  } catch (err) {
    error.value = getErrorMessage(err, '저장에 실패했습니다.')
  } finally {
    isSaving.value = false
  }
}

function formatDate(dateString?: string): string {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleDateString('ko-KR')
}

onMounted(async () => {
  isLoading.value = true
  await userStore.fetchUser()
  if (user.value?.gameNickname) {
    gameNickname.value = user.value.gameNickname
  }
  isLoading.value = false
})
</script>

<style scoped>
.profile-view {
  max-width: 700px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
}

.page-header h1 {
  margin: 0;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4rem;
  color: var(--text-muted);
}

.profile-section {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.profile-section h2 {
  margin: 0 0 0.5rem;
  font-size: 1.125rem;
}

.section-desc {
  margin: 0 0 1rem;
  color: var(--text-muted);
  font-size: 0.875rem;
}

.info-card {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--primary-color), var(--success-color));
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 600;
  color: white;
}

.info-content {
  flex: 1;
}

.info-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.info-row .label {
  width: 80px;
  color: var(--text-muted);
  font-size: 0.875rem;
}

.info-row .value {
  font-weight: 500;
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

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-size: 1rem;
}

.form-group input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.form-group input:disabled {
  background: var(--bg-color-alt);
  cursor: not-allowed;
}

.help-text {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: var(--text-muted);
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

.linked-accounts {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.account-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
}

.account-icon {
  font-size: 1.5rem;
}

.account-name {
  flex: 1;
  font-weight: 500;
}

.account-status {
  font-size: 0.875rem;
  color: var(--text-muted);
}

.account-status.connected {
  color: var(--success-color);
}

.message {
  margin-top: 1rem;
  padding: 1rem;
  border-radius: var(--radius-md);
}

.message.error {
  background: rgba(var(--error-rgb), 0.1);
  color: var(--error-color);
}

.message.success {
  background: rgba(var(--success-rgb), 0.1);
  color: var(--success-color);
}
</style>
