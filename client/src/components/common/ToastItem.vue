<template>
  <div :class="['toast-item', `toast-${toast.type}`]">
    <component :is="iconComponent" class="toast-icon" :size="20" />
    <span class="toast-message">{{ toast.message }}</span>
    <button class="toast-close" @click="$emit('close')" aria-label="닫기">
      <X :size="16" />
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { CheckCircle, XCircle, AlertTriangle, Info, X } from 'lucide-vue-next'
import type { Toast } from '@/composables/useToast'

interface Props {
  toast: Toast
}

const props = defineProps<Props>()

defineEmits<{
  close: []
}>()

const iconComponent = computed(() => {
  const icons = {
    success: CheckCircle,
    error: XCircle,
    warning: AlertTriangle,
    info: Info
  }
  return icons[props.toast.type]
})
</script>

<style scoped>
.toast-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  border-radius: 8px;
  background: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  pointer-events: auto;
}

.toast-icon {
  flex-shrink: 0;
}

.toast-message {
  flex: 1;
  font-size: 0.9rem;
  font-weight: 500;
}

.toast-close {
  flex-shrink: 0;
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.toast-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #333;
}

/* Types */
.toast-success {
  border-left: 4px solid #10b981;
}

.toast-success .toast-icon {
  color: #10b981;
}

.toast-error {
  border-left: 4px solid #ef4444;
}

.toast-error .toast-icon {
  color: #ef4444;
}

.toast-warning {
  border-left: 4px solid #f59e0b;
}

.toast-warning .toast-icon {
  color: #f59e0b;
}

.toast-info {
  border-left: 4px solid #3b82f6;
}

.toast-info .toast-icon {
  color: #3b82f6;
}
</style>
