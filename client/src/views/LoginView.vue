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

      <div class="oauth-buttons">
        <Button variant="secondary" class="social-btn steam-btn" @click="handleSteamLogin">
          <svg class="steam-icon" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12c0 4.84 3.44 8.87 8 9.8V15H8v-3h2V9.5C10 7.57 11.57 6 13.5 6H16v3h-2c-.55 0-1 .45-1 1v2h3v3h-3v6.95c5.05-.5 9-4.76 9-9.95 0-5.52-4.48-10-10-10z"/>
          </svg>
          <span>Steam으로 계속하기</span>
        </Button>

        <Button variant="secondary" class="social-btn kakao-btn" @click="handleKakaoLogin">
          <svg class="kakao-icon" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M12 3C6.48 3 2 6.58 2 10.94c0 2.8 1.86 5.26 4.66 6.65-.15.54-.96 3.47-1 3.64 0 .05.02.1.05.14.05.05.12.07.19.07.09 0 .18-.05.23-.08.69-.48 2.83-1.93 4.02-2.74.58.08 1.18.12 1.79.12 5.52 0 10-3.58 10-7.94S17.52 3 12 3z"/>
          </svg>
          <span>Kakao로 계속하기</span>
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { Map, LogIn, UserPlus, Key, Search } from 'lucide-vue-next'
import { Button, Input } from '@/components/common'
import { useAuth } from '@/composables/useAuth'
import { oauth2Api } from '@/api/auth'

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

// Steam 로그인
function handleSteamLogin() {
  window.location.href = oauth2Api.getSteamLoginUrl()
}

// Kakao 로그인
function handleKakaoLogin() {
  // TODO: Kakao REST API Key를 환경변수로 관리
  const kakaoClientId = import.meta.env.VITE_KAKAO_CLIENT_ID || ''
  if (!kakaoClientId) {
    alert('Kakao 로그인이 설정되지 않았습니다.')
    return
  }
  window.location.href = oauth2Api.getKakaoLoginUrl(kakaoClientId)
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

.oauth-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.social-btn {
  width: 100%;
  justify-content: center;
}

.steam-btn {
  background: #1b2838;
  border-color: #1b2838;
  color: white;
}

.steam-btn:hover {
  background: #2a475e;
  border-color: #2a475e;
}

.kakao-btn {
  background: #fee500;
  border-color: #fee500;
  color: #3c1e1e;
}

.kakao-btn:hover {
  background: #fdd835;
  border-color: #fdd835;
}

.steam-icon,
.kakao-icon {
  flex-shrink: 0;
}
</style>
