<template>
  <div class="page-loader">
    <div class="page-loader-content">
      <div class="page-loader-spinner"></div>
      <p v-if="text">{{ text }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  text?: string
}

defineProps<Props>()
</script>

<style scoped>
.page-loader {
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9998;
}

.page-loader-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.page-loader-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid #f0f0f0;
  border-top-color: var(--primary-color, #667eea);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.page-loader-content p {
  color: #666;
  font-size: 0.9rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
