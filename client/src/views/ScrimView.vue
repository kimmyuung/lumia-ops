<template>
  <div class="scrim-view">
    <div class="container">
      <header class="page-header">
        <div class="header-content">
          <h1>
            <Swords :size="32" class="header-icon" />
            <span>스크림 관리</span>
          </h1>
          <p class="page-description">
            자동화된 스크림 매니지먼트 시스템
          </p>
        </div>
        <Button variant="primary">
          <Plus :size="20" />
          <span>스크림 생성</span>
        </Button>
      </header>

      <section class="stats-section">
        <Card class="stat-card">
          <div class="stat-icon">
            <Calendar :size="24" />
          </div>
          <div class="stat-content">
            <span class="stat-value">0</span>
            <span class="stat-label">예정된 스크림</span>
          </div>
        </Card>
        <Card class="stat-card">
          <div class="stat-icon completed">
            <Trophy :size="24" />
          </div>
          <div class="stat-content">
            <span class="stat-value">0</span>
            <span class="stat-label">완료된 스크림</span>
          </div>
        </Card>
        <Card class="stat-card">
          <div class="stat-icon average">
            <TrendingUp :size="24" />
          </div>
          <div class="stat-content">
            <span class="stat-value">-</span>
            <span class="stat-label">평균 순위</span>
          </div>
        </Card>
      </section>

      <section class="empty-state">
        <div class="empty-icon">
          <Swords :size="64" />
        </div>
        <h2>스크림 기록이 없습니다</h2>
        <p>새로운 스크림을 생성하여 기록을 시작하세요.</p>
        <Button variant="primary">
          <Plus :size="20" />
          <span>첫 스크림 생성하기</span>
        </Button>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  Swords,
  Plus,
  Calendar,
  Trophy,
  TrendingUp
} from 'lucide-vue-next'
import { Card, Button } from '@/components/common'
</script>

<style scoped>
.scrim-view {
  min-height: 100%;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--page-padding);
}

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
}

.header-icon {
  color: var(--primary-color);
}

.page-description {
  color: var(--text-muted);
}

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(102, 126, 234, 0.1);
  border-radius: var(--radius-md);
  color: var(--primary-color);
}

.stat-icon.completed {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
}

.stat-icon.average {
  background: rgba(245, 158, 11, 0.1);
  color: var(--warning-color);
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-muted);
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.1) 0%,
    rgba(118, 75, 162, 0.1) 100%
  );
  border-radius: 50%;
  color: var(--primary-color);
}

.empty-state h2 {
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: 1.5rem;
}

/* 반응형 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }
}
</style>
