<template>
  <Teleport to="body">
    <Transition name="confirm-modal">
      <div v-if="isOpen" class="confirm-overlay" @click.self="handleCancel">
        <div class="confirm-modal" :class="variant">
          <div class="confirm-icon" :class="variant">
            <AlertTriangle v-if="variant === 'danger'" :size="32" />
            <AlertCircle v-if="variant === 'warning'" :size="32" />
            <Info v-if="variant === 'info'" :size="32" />
            <CheckCircle v-if="variant === 'success'" :size="32" />
          </div>
          
          <div class="confirm-content">
            <h3 class="confirm-title">{{ title }}</h3>
            <p class="confirm-message">{{ message }}</p>
          </div>
          
          <div class="confirm-actions">
            <button 
              class="confirm-btn cancel" 
              @click="handleCancel"
              :disabled="isLoading"
            >
              {{ cancelText }}
            </button>
            <button 
              class="confirm-btn confirm" 
              :class="variant"
              @click="handleConfirm"
              :disabled="isLoading"
            >
              <span v-if="isLoading" class="btn-spinner"></span>
              <span>{{ confirmText }}</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { AlertTriangle, AlertCircle, Info, CheckCircle } from 'lucide-vue-next'

export type ConfirmVariant = 'danger' | 'warning' | 'info' | 'success'

interface Props {
  isOpen: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  variant?: ConfirmVariant
  isLoading?: boolean
}

withDefaults(defineProps<Props>(), {
  confirmText: '확인',
  cancelText: '취소',
  variant: 'danger',
  isLoading: false
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

function handleConfirm() {
  emit('confirm')
}

function handleCancel() {
  emit('cancel')
}
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
  padding: 1rem;
}

.confirm-modal {
  background: var(--card-bg-solid);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  text-align: center;
  box-shadow: var(--shadow-lg);
}

.confirm-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.confirm-icon.danger {
  background: rgba(239, 68, 68, 0.15);
  color: var(--error-color);
}

.confirm-icon.warning {
  background: rgba(245, 158, 11, 0.15);
  color: var(--warning-color);
}

.confirm-icon.info {
  background: rgba(59, 130, 246, 0.15);
  color: var(--info-color);
}

.confirm-icon.success {
  background: rgba(16, 185, 129, 0.15);
  color: var(--success-color);
}

.confirm-content {
  margin-bottom: 1.5rem;
}

.confirm-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 0.5rem;
}

.confirm-message {
  color: var(--text-muted);
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  gap: 0.75rem;
}

.confirm-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  font-weight: 600;
  transition: all var(--transition-normal);
  border: none;
  cursor: pointer;
}

.confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.confirm-btn.cancel {
  background: var(--bg-color-alt);
  color: var(--text-color);
  border: 1px solid var(--border-color);
}

.confirm-btn.cancel:hover:not(:disabled) {
  background: var(--card-bg);
}

.confirm-btn.confirm.danger {
  background: var(--error-color);
  color: white;
}

.confirm-btn.confirm.danger:hover:not(:disabled) {
  background: #dc2626;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.confirm-btn.confirm.warning {
  background: var(--warning-color);
  color: white;
}

.confirm-btn.confirm.warning:hover:not(:disabled) {
  background: #d97706;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.confirm-btn.confirm.info {
  background: var(--info-color);
  color: white;
}

.confirm-btn.confirm.info:hover:not(:disabled) {
  background: #2563eb;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.confirm-btn.confirm.success {
  background: var(--success-color);
  color: white;
}

.confirm-btn.confirm.success:hover:not(:disabled) {
  background: #059669;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

/* Transition */
.confirm-modal-enter-active,
.confirm-modal-leave-active {
  transition: opacity 0.2s ease;
}

.confirm-modal-enter-active .confirm-modal,
.confirm-modal-leave-active .confirm-modal {
  transition: transform 0.2s ease;
}

.confirm-modal-enter-from,
.confirm-modal-leave-to {
  opacity: 0;
}

.confirm-modal-enter-from .confirm-modal,
.confirm-modal-leave-to .confirm-modal {
  transform: scale(0.9);
}

@media (max-width: 480px) {
  .confirm-modal {
    padding: 1.5rem;
  }
  
  .confirm-actions {
    flex-direction: column-reverse;
  }
}

/* 버튼 내 스피너 */
.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
