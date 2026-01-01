<template>
  <div class="skeleton-card">
    <Skeleton variant="rectangular" height="120px" />
    <div class="skeleton-card-content">
      <Skeleton variant="text" width="60%" height="1.5em" />
      <Skeleton variant="text" />
      <Skeleton variant="text" width="80%" />
    </div>
  </div>
</template>

<script setup lang="ts">
import Skeleton from './Skeleton.vue'
</script>

<style scoped>
.skeleton-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.skeleton-card-content {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
</style>
