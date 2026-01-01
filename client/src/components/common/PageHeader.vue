<template>
  <header class="page-header">
    <div class="header-content">
      <h1>
        <component :is="icon" :size="32" class="header-icon" />
        <span>{{ title }}</span>
      </h1>
      <p v-if="description" class="page-description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="header-actions">
      <slot name="actions" />
    </div>
  </header>
</template>

<script setup lang="ts">
import type { Component } from 'vue'

interface Props {
  title: string
  description?: string
  icon: Component
}

defineProps<Props>()
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.header-content h1 {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
  color: var(--text-color);
  font-size: clamp(1.5rem, 4vw, 2rem);
}

.header-icon {
  color: var(--primary-color);
  flex-shrink: 0;
}

.page-description {
  color: var(--text-muted);
  font-size: 1rem;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

/* 반응형 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
  }

  .header-actions :deep(.btn) {
    flex: 1;
    justify-content: center;
  }
}
</style>
