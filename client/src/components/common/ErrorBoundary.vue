<template>
  <slot v-if="!hasError" />
  <div v-else class="error-boundary">
    <div class="error-content">
      <div class="error-icon">
        <AlertTriangle :size="64" />
      </div>
      <h2>오류가 발생했습니다</h2>
      <p class="error-message">{{ errorMessage }}</p>
      <div class="error-actions">
        <Button variant="primary" @click="reset">
          <RefreshCw :size="18" />
          <span>다시 시도</span>
        </Button>
        <Button variant="secondary" @click="goHome">
          <Home :size="18" />
          <span>홈으로</span>
        </Button>
      </div>
      <details v-if="showDetails && error" class="error-details">
        <summary>오류 상세 정보</summary>
        <pre>{{ errorDetails }}</pre>
      </details>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onErrorCaptured, computed } from 'vue'
import { useRouter } from 'vue-router'
import { AlertTriangle, RefreshCw, Home } from 'lucide-vue-next'
import { Button } from '@/components/common'

interface Props {
  fallbackMessage?: string
  showDetails?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  fallbackMessage: '예상치 못한 오류가 발생했습니다.',
  showDetails: import.meta.env.DEV
})

const router = useRouter()
const hasError = ref(false)
const error = ref<Error | null>(null)
const errorInfo = ref<string>('')

const errorMessage = computed(() => {
  return error.value?.message || props.fallbackMessage
})

const errorDetails = computed(() => {
  if (!error.value) return ''
  return `${error.value.name}: ${error.value.message}\n\n${error.value.stack || ''}`
})

// Vue 에러 캡처
onErrorCaptured((err: Error, _instance: unknown, info: string) => {
  hasError.value = true
  error.value = err
  errorInfo.value = info

  // eslint-disable-next-line no-console
  if (import.meta.env.DEV) console.error('[ErrorBoundary] Captured error:', err, info)

  // 에러를 상위로 전파하지 않음
  return false
})

function reset() {
  hasError.value = false
  error.value = null
  errorInfo.value = ''
}

function goHome() {
  reset()
  router.push('/')
}

// 외부에서 리셋 가능하도록 노출
defineExpose({ reset, hasError })
</script>

<style scoped>
.error-boundary {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.error-content {
  text-align: center;
  max-width: 500px;
}

.error-icon {
  width: 100px;
  height: 100px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.2) 100%);
  border-radius: 50%;
  color: var(--error-color);
}

.error-content h2 {
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.error-message {
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.error-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.error-details {
  margin-top: 2rem;
  text-align: left;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
  padding: 1rem;
}

.error-details summary {
  cursor: pointer;
  color: var(--text-muted);
  font-size: 0.875rem;
  margin-bottom: 0.5rem;
}

.error-details pre {
  font-size: 0.75rem;
  color: var(--error-color);
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
