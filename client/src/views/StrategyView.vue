<template>
  <div class="strategy-view">
    <div class="container">
      <header class="page-header">
        <div class="header-content">
          <h1>
            <Target :size="32" class="header-icon" />
            <span>전략 수립 보드</span>
          </h1>
          <p class="page-description">
            실시간 동기화 택티컬 맵으로 팀 전략을 수립하세요.
          </p>
        </div>
      </header>

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
          <Card hoverable>
            <div class="tool-icon">
              <MapPin :size="24" />
            </div>
            <h4>마커</h4>
            <p>맵에 마커 배치</p>
          </Card>
          <Card hoverable>
            <div class="tool-icon">
              <Route :size="24" />
            </div>
            <h4>경로</h4>
            <p>이동 경로 그리기</p>
          </Card>
          <Card hoverable>
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
import { Card, Button } from '@/components/common'
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

.page-header {
  margin-bottom: 2rem;
}

.header-content h1 {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.header-icon {
  color: var(--primary-color);
}

.page-description {
  color: var(--text-muted);
}

.map-section {
  margin-bottom: var(--section-gap);
}

.tactical-map {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: var(--radius-lg);
  overflow: hidden;
  aspect-ratio: 16 / 9;
  max-height: 600px;
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
  opacity: 0.4;
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
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
}

.tools-grid .card {
  text-align: center;
  padding: 1.5rem;
}

.tool-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 50%;
  color: var(--primary-color);
}

.tools-grid h4 {
  margin-bottom: 0.25rem;
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
