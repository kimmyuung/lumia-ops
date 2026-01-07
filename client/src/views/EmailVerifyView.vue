<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <router-link to="/" class="auth-logo">
          <Map :size="32" />
          <span>Lumia Ops</span>
        </router-link>
        <h1>이메일 인증</h1>
      </div>

      <!-- 로딩 -->
      <div v-if="isLoading" class="status-box loading">
        <Loader2 :size="48" class="spinner" />
        <p>이메일을 인증하는 중...</p>
      </div>

      <!-- 성공 -->
      <div v-else-if="isSuccess" class="status-box success">
        <CheckCircle :size="48" class="icon success-icon" />
        <h2>인증 완료!</h2>
        <p>이메일 인증이 완료되었습니다.</p>
        <p class="hint">이제 로그인하여 닉네임을 설정해주세요.</p>
        <router-link to="/login">
          <Button variant="primary" class="action-btn">
            <LogIn :size="20" />
            <span>로그인하기</span>
          </Button>
        </router-link>
      </div>

      <!-- 실패 -->
      <div v-else class="status-box error">
        <XCircle :size="48" class="icon error-icon" />
        <h2>인증 실패</h2>
        <p>{{ errorMessage }}</p>
        <div class="actions">
          <router-link to="/login">
            <Button variant="secondary">
              <LogIn :size="18" />
              <span>로그인 페이지로</span>
            </Button>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Map, LogIn, CheckCircle, XCircle, Loader2 } from 'lucide-vue-next'
import { Button } from '@/components/common'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const { verifyEmail } = useAuth()

const isLoading = ref(true)
const isSuccess = ref(false)
const errorMessage = ref('')

onMounted(async () => {
  const token = route.query.token as string

  if (!token) {
    isLoading.value = false
    errorMessage.value = '인증 토큰이 없습니다.'
    return
  }

  const success = await verifyEmail(token)
  isLoading.value = false
  isSuccess.value = success

  if (!success) {
    errorMessage.value = '유효하지 않거나 만료된 인증 토큰입니다.'
  }
})
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
  color: var(--text-color);
}

.status-box {
  text-align: center;
  padding: 2rem 0;
}

.status-box h2 {
  font-size: 1.5rem;
  margin: 1rem 0;
  color: var(--text-color);
}

.status-box p {
  color: var(--text-muted);
  margin-bottom: 0.5rem;
}

.status-box .hint {
  font-size: 0.9rem;
  margin-bottom: 2rem;
}

.icon {
  margin-bottom: 0.5rem;
}

.success-icon {
  color: var(--success-color, #22c55e);
}

.error-icon {
  color: var(--error-color, #ef4444);
}

.spinner {
  color: var(--primary-color);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.action-btn {
  margin-top: 1rem;
}

.actions {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
  gap: 1rem;
}
</style>
