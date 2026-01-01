<template>
  <div :class="['skeleton', `skeleton-${variant}`]" :style="style"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'text' | 'circular' | 'rectangular'
  width?: string
  height?: string
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'text',
  width: '100%',
  height: undefined
})

const style = computed(() => ({
  width: props.width,
  height: props.height || (props.variant === 'text' ? '1em' : undefined)
}))
</script>

<style scoped>
.skeleton {
  background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

.skeleton-text {
  height: 1em;
  border-radius: 4px;
}

.skeleton-circular {
  border-radius: 50%;
}

.skeleton-rectangular {
  border-radius: 8px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
