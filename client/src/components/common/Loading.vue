<template>
  <div :class="['loading', `loading-${size}`]">
    <div class="loading-spinner"></div>
    <span v-if="text" class="loading-text">{{ text }}</span>
  </div>
</template>

<script setup lang="ts">
interface Props {
  size?: 'sm' | 'md' | 'lg'
  text?: string
}

withDefaults(defineProps<Props>(), {
  size: 'md',
  text: ''
})
</script>

<style scoped>
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
}

.loading-spinner {
  border: 3px solid #f0f0f0;
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.loading-sm .loading-spinner {
  width: 20px;
  height: 20px;
}

.loading-md .loading-spinner {
  width: 32px;
  height: 32px;
}

.loading-lg .loading-spinner {
  width: 48px;
  height: 48px;
}

.loading-text {
  color: #666;
  font-size: 0.875rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
