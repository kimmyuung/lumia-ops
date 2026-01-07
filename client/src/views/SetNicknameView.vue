<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <router-link to="/" class="auth-logo">
          <Map :size="32" />
          <span>Lumia Ops</span>
        </router-link>
        <h1>닉네임 설정</h1>
        <p>다른 사용자에게 보여질 닉네임을 설정해주세요</p>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-field">
          <label for="nickname">닉네임</label>
          <Input
            id="nickname"
            v-model="nickname"
            type="text"
            placeholder="닉네임을 입력하세요 (2-20자)"
            :error="error || ''"
            :disabled="isLoading"
          />
          <span class="field-hint">닉네임은 30일에 한 번만 변경할 수 있습니다</span>
        </div>

        <Button
          type="submit"
          variant="primary"
          :loading="isLoading"
          :disabled="!isFormValid"
          class="submit-btn"
        >
          <Check :size="20" />
          <span>설정 완료</span>
        </Button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Map, Check } from 'lucide-vue-next'
import { Button, Input } from '@/components/common'
import { useAuth } from '@/composables/useAuth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { setNickname, isLoading, error } = useAuth()

const nickname = ref('')

const isFormValid = computed(() => {
  const trimmed = nickname.value.trim()
  return trimmed.length >= 2 && trimmed.length <= 20
})

// 임시 사용자 정보가 없으면 로그인 페이지로
if (!userStore.tempUser) {
  router.push('/login')
}

async function handleSubmit() {
  if (!isFormValid.value) return
  await setNickname(nickname.value.trim())
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.auth-container {
  width: 100%;
  max-width: 420px;
  background: var(--card-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 2.5rem;
  box-shadow: var(--shadow-lg);
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.auth-logo {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-color);
  text-decoration: none;
  margin-bottom: 1.5rem;
}

.auth-header h1 {
  font-size: 1.75rem;
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.auth-header p {
  color: var(--text-muted);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
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

.field-hint {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.submit-btn {
  width: 100%;
  justify-content: center;
}
</style>
