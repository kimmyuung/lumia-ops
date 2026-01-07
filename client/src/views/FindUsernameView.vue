<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <router-link to="/" class="auth-logo">
          <Map :size="32" />
          <span>Lumia Ops</span>
        </router-link>
        <h1>아이디 찾기</h1>
        <p>가입한 이메일 주소를 입력해주세요</p>
      </div>

      <!-- 결과 표시 -->
      <div v-if="searchComplete" class="result-box">
        <div v-if="foundEmail" class="success-result">
          <CheckCircle :size="48" class="success-icon" />
          <h2>아이디를 찾았습니다!</h2>
          <div class="email-display">
            <Mail :size="20" />
            <span>{{ foundEmail }}</span>
          </div>
          <p class="hint">위 이메일로 로그인해주세요.</p>
        </div>

        <div v-else class="not-found-result">
          <XCircle :size="48" class="error-icon" />
          <h2>등록되지 않은 이메일</h2>
          <p>입력하신 이메일로 등록된 계정이 없습니다.</p>
        </div>

        <div class="actions">
          <Button variant="secondary" @click="resetSearch">
            <Search :size="18" />
            <span>다시 찾기</span>
          </Button>
          <router-link to="/login">
            <Button variant="primary">
              <LogIn :size="18" />
              <span>로그인</span>
            </Button>
          </router-link>
        </div>
      </div>

      <!-- 이메일 입력 폼 -->
      <form v-else class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-field">
          <label for="email">이메일</label>
          <Input
            id="email"
            v-model="email"
            type="email"
            placeholder="가입한 이메일을 입력하세요"
            :error="error"
            :disabled="isLoading"
          />
        </div>

        <Button
          type="submit"
          variant="primary"
          :loading="isLoading"
          :disabled="!isFormValid"
          class="submit-btn"
        >
          <Search :size="20" />
          <span>아이디 찾기</span>
        </Button>
      </form>

      <div v-if="!searchComplete" class="auth-footer">
        <router-link to="/login" class="auth-link">
          <ArrowLeft :size="18" />
          <span>로그인으로 돌아가기</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Map, Search, LogIn, Mail, ArrowLeft, CheckCircle, XCircle } from 'lucide-vue-next'
import { Button, Input } from '@/components/common'
import { authApi } from '@/api/auth'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const email = ref('')
const error = ref('')
const isLoading = ref(false)
const searchComplete = ref(false)
const foundEmail = ref<string | null>(null)

const isFormValid = computed(() => {
  return email.value.trim() !== '' && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)
})

async function handleSubmit() {
  if (!isFormValid.value) return

  isLoading.value = true
  error.value = ''

  try {
    const response = await authApi.findUsername(email.value)
    searchComplete.value = true
    foundEmail.value = response.exists ? response.email : null
  } catch (err: unknown) {
    const apiError = err as { response?: { data?: { message?: string } }; message?: string }
    error.value =
      apiError.response?.data?.message || apiError.message || '아이디 찾기에 실패했습니다.'
    toast.error(error.value)
  } finally {
    isLoading.value = false
  }
}

function resetSearch() {
  searchComplete.value = false
  foundEmail.value = null
  email.value = ''
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

.submit-btn {
  width: 100%;
  justify-content: center;
}

.auth-footer {
  margin-top: 2rem;
  text-align: center;
}

.auth-link {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--primary-color);
  text-decoration: none;
  font-weight: 600;
  transition: opacity var(--transition-fast);
}

.auth-link:hover {
  opacity: 0.8;
}

/* 결과 박스 */
.result-box {
  text-align: center;
  padding: 1rem 0;
}

.success-result,
.not-found-result {
  margin-bottom: 2rem;
}

.success-icon {
  color: var(--success-color, #22c55e);
  margin-bottom: 1rem;
}

.error-icon {
  color: var(--error-color, #ef4444);
  margin-bottom: 1rem;
}

.result-box h2 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: var(--text-color);
}

.result-box p {
  color: var(--text-muted);
}

.email-display {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 1.5rem;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 1rem;
}

.hint {
  font-size: 0.9rem;
  color: var(--text-muted);
}

.actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}
</style>
