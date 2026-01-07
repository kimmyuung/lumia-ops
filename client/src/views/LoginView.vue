<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <router-link to="/" class="auth-logo">
          <Map :size="32" />
          <span>Lumia Ops</span>
        </router-link>
        <h1>로그인</h1>
        <p>팀 전략 관리를 시작하세요</p>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-field">
          <label for="email">이메일</label>
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
            placeholder="비밀번호를 입력하세요"
            :error="errors.password"
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
          <LogIn :size="20" />
          <span>로그인</span>
        </Button>
      </form>

      <!-- 헬프 링크 -->
      <div class="help-links">
        <router-link to="/auth/forgot-password" class="help-link">
          <Key :size="16" />
          <span>비밀번호 찾기</span>
        </router-link>
        <span class="separator">|</span>
        <router-link to="/auth/find-username" class="help-link">
          <Search :size="16" />
          <span>아이디 찾기</span>
        </router-link>
      </div>

      <div class="auth-footer">
        <p>계정이 없으신가요?</p>
        <router-link to="/register" class="auth-link">
          <UserPlus :size="18" />
          <span>회원가입</span>
        </router-link>
      </div>

      <div class="auth-divider">
        <span>또는</span>
      </div>

      <Button variant="secondary" disabled class="social-btn">
        <MessageCircle :size="20" />
        <span>Discord로 계속하기</span>
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { Map, LogIn, UserPlus, MessageCircle, Key, Search } from 'lucide-vue-next'
import { Button, Input } from '@/components/common'
import { useAuth } from '@/composables/useAuth'

const { login, isLoading } = useAuth()

const form = reactive({
  email: '',
  password: ''
})

const errors = reactive({
  email: '',
  password: ''
})

const isFormValid = computed(() => {
  return form.email.trim() !== '' && form.password.trim() !== ''
})

function validateForm(): boolean {
  errors.email = ''
  errors.password = ''
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
  }

  return valid
}

async function handleSubmit() {
  if (!validateForm()) return
  await login({ email: form.email, password: form.password })
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
  gap: 1.25rem;
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
  margin-top: 0.5rem;
  width: 100%;
  justify-content: center;
}

/* 헬프 링크 */
.help-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.75rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.help-link {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.875rem;
  transition: color var(--transition-fast);
}

.help-link:hover {
  color: var(--primary-color);
}

.separator {
  color: var(--border-color);
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
</style>
