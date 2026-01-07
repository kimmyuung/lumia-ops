<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <router-link to="/" class="auth-logo">
          <Map :size="32" />
          <span>Lumia Ops</span>
        </router-link>
        <h1>비밀번호 재설정</h1>
        <p>새로운 비밀번호를 입력해주세요</p>
      </div>

      <!-- 토큰 없음 -->
      <div v-if="!token" class="error-message">
        <XCircle :size="48" class="error-icon" />
        <h2>잘못된 접근</h2>
        <p>유효하지 않은 링크입니다.</p>
        <router-link to="/auth/forgot-password">
          <Button variant="secondary">
            <Mail :size="18" />
            <span>비밀번호 찾기로 이동</span>
          </Button>
        </router-link>
      </div>

      <!-- 재설정 완료 -->
      <div v-else-if="resetComplete" class="success-message">
        <CheckCircle :size="48" class="success-icon" />
        <h2>재설정 완료!</h2>
        <p>비밀번호가 성공적으로 변경되었습니다.</p>
        <router-link to="/login">
          <Button variant="primary" class="action-btn">
            <LogIn :size="20" />
            <span>로그인하기</span>
          </Button>
        </router-link>
      </div>

      <!-- 비밀번호 입력 폼 -->
      <form v-else class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-field">
          <label for="password">새 비밀번호</label>
          <Input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="새 비밀번호를 입력하세요 (8자 이상)"
            :error="errors.password"
            :disabled="isLoading"
          />
          <span class="field-hint">영문, 숫자 포함 8자 이상</span>
        </div>

        <div class="form-field">
          <label for="confirmPassword">비밀번호 확인</label>
          <Input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            placeholder="비밀번호를 다시 입력하세요"
            :error="errors.confirmPassword"
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
          <Key :size="20" />
          <span>비밀번호 변경</span>
        </Button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Map, LogIn, Key, Mail, CheckCircle, XCircle } from 'lucide-vue-next'
import { Button, Input } from '@/components/common'
import { passwordApi } from '@/api/password'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const toast = useToast()

const token = route.query.token as string
const isLoading = ref(false)
const resetComplete = ref(false)

const form = reactive({
  password: '',
  confirmPassword: ''
})

const errors = reactive({
  password: '',
  confirmPassword: ''
})

const isFormValid = computed(() => {
  return form.password.trim() !== '' && form.confirmPassword.trim() !== ''
})

function validateForm(): boolean {
  errors.password = ''
  errors.confirmPassword = ''
  let valid = true

  if (!form.password.trim()) {
    errors.password = '비밀번호를 입력해 주세요.'
    valid = false
  } else if (form.password.length < 8) {
    errors.password = '비밀번호는 8자 이상이어야 합니다.'
    valid = false
  } else if (!/\d/.test(form.password)) {
    errors.password = '비밀번호에 숫자가 포함되어야 합니다.'
    valid = false
  } else if (!/[a-zA-Z]/.test(form.password)) {
    errors.password = '비밀번호에 영문자가 포함되어야 합니다.'
    valid = false
  }

  if (!form.confirmPassword.trim()) {
    errors.confirmPassword = '비밀번호 확인을 입력해 주세요.'
    valid = false
  } else if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '비밀번호가 일치하지 않습니다.'
    valid = false
  }

  return valid
}

async function handleSubmit() {
  if (!validateForm()) return

  isLoading.value = true

  try {
    const response = await passwordApi.resetPassword(token, form.password)
    if (response.success) {
      resetComplete.value = true
      toast.success(response.message)
    } else {
      toast.error(response.message)
    }
  } catch (err: unknown) {
    const apiError = err as { response?: { data?: { message?: string } }; message?: string }
    toast.error(
      apiError.response?.data?.message || apiError.message || '비밀번호 재설정에 실패했습니다.'
    )
  } finally {
    isLoading.value = false
  }
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
  gap: 1rem;
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
  margin-top: 0.5rem;
  width: 100%;
  justify-content: center;
}

/* 에러/성공 메시지 */
.error-message,
.success-message {
  text-align: center;
  padding: 1rem 0;
}

.success-icon {
  color: var(--success-color, #22c55e);
  margin-bottom: 1rem;
}

.error-icon {
  color: var(--error-color, #ef4444);
  margin-bottom: 1rem;
}

.error-message h2,
.success-message h2 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: var(--text-color);
}

.error-message p,
.success-message p {
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.action-btn {
  margin-top: 1rem;
}
</style>
