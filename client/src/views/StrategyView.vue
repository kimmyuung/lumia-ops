<template>
  <div class="strategy-view">
    <h1>🗺️ 전략 수립 보드</h1>
    <p>실시간 동기화 택티컬 맵으로 팀 전략을 수립하세요.</p>
    <div class="tactical-map-placeholder">
      <!-- TODO: Tactical Map Canvas -->
      <p>📍 택티컬 맵 영역 (개발 예정)</p>
    </div>
  </div>
</template>

<script setup lang="ts">
// Strategy view logic
</script>

<style scoped>
.strategy-view {
  padding: 2rem;
}

.strategy-view h1 {
  margin-bottom: 1rem;
}

.tactical-map-placeholder {
  margin-top: 2rem;
  height: 500px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  font-size: 1.25rem;
}
</style>
