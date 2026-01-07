<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <router-link to="/" class="auth-logo">
          <Map :size="32" />
          <span>Lumia Ops</span>
        </router-link>
        <h1>비밀번호 찾기</h1>
        <p>가입한 이메일 주소를 입력해주세요</p>
      </div>

      <!-- 이메일 발송 완료 -->
      <div v-if="emailSent" class="success-message">
        <Mail :size="48" class="success-icon" />
        <h2>이메일을 확인하세요</h2>
        <p>{{ form.email }}로 비밀번호 재설정 링크를 발송했습니다.</p>
        <p class="hint">이메일의 링크를 클릭하여 새 비밀번호를 설정해주세요.</p>

        <router-link to="/login" class="auth-link">
          <LogIn :size="18" />
          <span>로그인 페이지로 이동</span>
        </router-link>
      </div>

      <!-- 이메일 입력 폼 -->
      <form v-else class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-field">
          <label for="email">이메일</label>
          <Input
            id="email"
            v-model="form.email"
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
          <Mail :size="20" />
          <span>재설정 링크 발송</span>
        </Button>
      </form>

      <div v-if="!emailSent" class="auth-footer">
        <router-link to="/login" class="auth-link">
          <ArrowLeft :size="18" />
          <span>로그인으로 돌아가기</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Map, Mail, LogIn, ArrowLeft } from 'lucide-vue-next'
import { Button, Input } from '@/components/common'
import { passwordApi } from '@/api/password'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const isLoading = ref(false)
const error = ref('')
const emailSent = ref(false)

const form = reactive({
  email: ''
})

const isFormValid = computed(() => {
  return form.email.trim() !== '' && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)
})

async function handleSubmit() {
  if (!isFormValid.value) return

  isLoading.value = true
  error.value = ''

  try {
    const response = await passwordApi.requestPasswordReset(form.email)
    emailSent.value = true
    toast.success(response.message)
  } catch (err: unknown) {
    // 보안상 존재하지 않는 이메일도 동일한 메시지 표시
    emailSent.value = true
    toast.info('등록된 이메일이라면 비밀번호 재설정 링크가 발송됩니다.')
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

/* 성공 메시지 */
.success-message {
  text-align: center;
  padding: 1rem 0;
}

.success-icon {
  color: var(--primary-color);
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
</style>
