<template>
  <div
    class="spinner"
    :style="{
      width: `${size}px`,
      height: `${size}px`,
      borderTopColor: color
    }"
  ></div>
</template>

<script setup lang="ts">
interface Props {
  size?: number
  color?: string
}

withDefaults(defineProps<Props>(), {
  size: 18,
  color: 'currentColor'
})
</script>

<style scoped>
.spinner {
  border: 2px solid transparent;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
