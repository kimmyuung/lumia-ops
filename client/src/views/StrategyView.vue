<template>
  <div class="strategy-view">
    <div class="container">
      <PageHeader
        title="전략 수립 보드"
        description="실시간 동기화 택티컬 맵으로 팀 전략을 수립하세요."
        :icon="Target"
      />

      <section class="map-section">
        <div class="tactical-map">
          <div class="map-placeholder">
            <Map :size="64" class="map-icon" />
            <h3>택티컬 맵</h3>
            <p>루미아 섬 지도에서 전략을 수립하세요</p>
            <Button variant="primary" disabled>
              <Sparkles :size="20" />
              <span>곧 출시 예정</span>
            </Button>
          </div>
        </div>
      </section>

      <section class="tools-section">
        <h2>전략 도구</h2>
        <div class="tools-grid">
          <Card hoverable class="tool-card">
            <div class="tool-icon">
              <MapPin :size="24" />
            </div>
            <h4>마커</h4>
            <p>맵에 마커 배치</p>
          </Card>
          <Card hoverable class="tool-card">
            <div class="tool-icon">
              <Route :size="24" />
            </div>
            <h4>경로</h4>
            <p>이동 경로 그리기</p>
          </Card>
          <Card hoverable class="tool-card">
            <div class="tool-icon">
              <MessageSquare :size="24" />
            </div>
            <h4>메모</h4>
            <p>전략 메모 추가</p>
          </Card>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Target,
  Map,
  MapPin,
  Route,
  MessageSquare,
  Sparkles
} from 'lucide-vue-next'
import { Card, Button, PageHeader } from '@/components/common'
</script>

<style scoped>
.strategy-view {
  min-height: 100%;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--page-padding);
}

.map-section {
  margin-bottom: var(--section-gap);
}

.tactical-map {
  background: linear-gradient(135deg, #0f0f1a 0%, #1a1a2e 100%);
  border-radius: var(--radius-lg);
  overflow: hidden;
  aspect-ratio: 16 / 9;
  max-height: 600px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-lg);
}

.map-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  color: rgba(255, 255, 255, 0.6);
  text-align: center;
  padding: 2rem;
}

.map-icon {
  opacity: 0.5;
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.05); }
}

.map-placeholder h3 {
  color: white;
  font-size: 1.5rem;
}

.map-placeholder p {
  max-width: 300px;
}

.tools-section h2 {
  margin-bottom: 1.5rem;
  color: var(--text-color);
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
}

.tool-card {
  text-align: center;
  padding: 1.5rem;
}

.tool-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.15) 0%,
    rgba(118, 75, 162, 0.15) 100%
  );
  border-radius: 50%;
  color: var(--primary-color);
  transition: all var(--transition-normal);
}

.tool-card:hover .tool-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: var(--shadow-glow);
}

.tools-grid h4 {
  margin-bottom: 0.25rem;
  color: var(--text-color);
}

.tools-grid p {
  font-size: 0.875rem;
  color: var(--text-muted);
}

/* 반응형 */
@media (max-width: 768px) {
  .tactical-map {
    aspect-ratio: 4 / 3;
  }
}
</style>
