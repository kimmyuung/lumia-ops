<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <router-link to="/" class="auth-logo">
          <Map :size="32" />
          <span>Lumia Ops</span>
        </router-link>
        <h1>회원가입</h1>
        <p>새로운 계정을 만들어 시작하세요</p>
      </div>

      <!-- 가입 완료 안내 -->
      <div v-if="registrationComplete" class="success-message">
        <CheckCircle :size="48" class="success-icon" />
        <h2>이메일을 확인해주세요!</h2>
        <p>{{ form.email }}로 인증 메일을 발송했습니다.</p>
        <p class="hint">이메일의 인증 링크를 클릭하여 가입을 완료해주세요.</p>

        <div class="resend-section">
          <p>이메일을 받지 못하셨나요?</p>
          <Button variant="secondary" :loading="isResending" @click="handleResend">
            <Mail :size="18" />
            <span>인증 이메일 재발송</span>
          </Button>
        </div>

        <router-link to="/login" class="auth-link">
          <LogIn :size="18" />
          <span>로그인 페이지로 이동</span>
        </router-link>
      </div>

      <!-- 가입 폼 -->
      <form v-else class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-field">
          <label for="email">이메일 (아이디로 사용됩니다)</label>
          <Input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="이메일을 입력하세요"
            :error="errors.email"
            :disabled="isLoading"
          />
        </div>

        <div class="form-field">
          <label for="password">비밀번호</label>
          <Input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="비밀번호를 입력하세요 (8자 이상)"
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
          <UserPlus :size="20" />
          <span>가입하기</span>
        </Button>
      </form>

      <div v-if="!registrationComplete" class="auth-footer">
        <p>이미 계정이 있으신가요?</p>
        <router-link to="/login" class="auth-link">
          <LogIn :size="18" />
          <span>로그인</span>
        </router-link>
      </div>

      <div v-if="!registrationComplete" class="auth-divider">
        <span>또는</span>
      </div>

      <Button v-if="!registrationComplete" variant="secondary" disabled class="social-btn">
        <MessageCircle :size="20" />
        <span>Discord로 계속하기</span>
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { Map, LogIn, UserPlus, MessageCircle, CheckCircle, Mail } from 'lucide-vue-next'
import { Button, Input } from '@/components/common'
import { useAuth } from '@/composables/useAuth'

const { register, resendVerification, isLoading } = useAuth()

const registrationComplete = ref(false)
const isResending = ref(false)

const form = reactive({
  email: '',
  password: '',
  confirmPassword: ''
})

const errors = reactive({
  email: '',
  password: '',
  confirmPassword: ''
})

const isFormValid = computed(() => {
  return (
    form.email.trim() !== '' && form.password.trim() !== '' && form.confirmPassword.trim() !== ''
  )
})

function validateForm(): boolean {
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''
  let valid = true

  if (!form.email.trim()) {
    errors.email = '이메일을 입력해 주세요.'
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '올바른 이메일 형식이 아닙니다.'
    valid = false
  }

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

  const success = await register({
    email: form.email,
    password: form.password
  })

  if (success) {
    registrationComplete.value = true
  }
}

async function handleResend() {
  isResending.value = true
  await resendVerification(form.email)
  isResending.value = false
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

.auth-footer {
  margin-top: 1.5rem;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.auth-footer p {
  color: var(--text-muted);
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

.auth-divider {
  display: flex;
  align-items: center;
  margin: 1.5rem 0;
  gap: 1rem;
}

.auth-divider::before,
.auth-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border-color);
}

.auth-divider span {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.social-btn {
  width: 100%;
  justify-content: center;
}

/* 가입 완료 메시지 */
.success-message {
  text-align: center;
  padding: 1rem 0;
}

.success-icon {
  color: var(--success-color, #22c55e);
  margin-bottom: 1rem;
}

.success-message h2 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
  color: var(--text-color);
}

.success-message p {
  color: var(--text-muted);
  margin-bottom: 0.5rem;
}

.success-message .hint {
  font-size: 0.9rem;
  margin-bottom: 2rem;
}

.resend-section {
  padding: 1.5rem;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  margin-bottom: 1.5rem;
}

.resend-section p {
  margin-bottom: 1rem;
}
</style>
