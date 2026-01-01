<template>
  <div class="home">
    <section class="hero">
      <div class="hero-content">
        <h1>
          <Map :size="48" class="hero-icon" />
          <span>Lumia Ops</span>
        </h1>
        <p class="tagline">이터널 리턴 팀 전략 & 스크림 관리 플랫폼</p>
        <div class="cta-buttons">
          <router-link to="/team" class="btn btn-primary">
            <Users :size="20" />
            <span>팀 관리</span>
          </router-link>
          <router-link to="/strategy" class="btn btn-secondary">
            <Target :size="20" />
            <span>전략 보드</span>
          </router-link>
        </div>
      </div>
    </section>

    <section class="features">
      <Card hoverable class="feature-card">
        <div class="feature-icon">
          <Users :size="32" />
        </div>
        <h3>팀 관리</h3>
        <p>팀원 초대, 역할 배정, 일정 관리</p>
      </Card>

      <Card hoverable class="feature-card">
        <div class="feature-icon">
          <Target :size="32" />
        </div>
        <h3>전략 수립</h3>
        <p>실시간 동기화 택티컬 맵</p>
      </Card>

      <Card hoverable class="feature-card">
        <div class="feature-icon">
          <Trophy :size="32" />
        </div>
        <h3>스크림 기록</h3>
        <p>자동화된 스크림 매니지먼트</p>
      </Card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { Map, Users, Target, Trophy } from 'lucide-vue-next'
import { Card } from '@/components/common'
</script>

<style scoped>
.home {
  min-height: 100%;
}

.hero {
  text-align: center;
  padding: var(--section-gap) var(--page-padding);
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.05) 0%,
    rgba(118, 75, 162, 0.05) 100%
  );
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero h1 {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  font-size: clamp(2rem, 5vw, 3.5rem);
  margin-bottom: 1rem;
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--secondary-color) 100%
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-icon {
  color: var(--secondary-color);
  -webkit-text-fill-color: initial;
}

.tagline {
  font-size: clamp(1rem, 2.5vw, 1.25rem);
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1.75rem;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-weight: 600;
  transition: all var(--transition-normal);
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.btn-primary {
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--secondary-color) 100%
  );
  color: white;
}

.btn-secondary {
  background: white;
  color: var(--text-color);
  box-shadow: var(--shadow-sm);
}

.btn-secondary:hover {
  background: #f8f8f8;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--section-gap) var(--page-padding);
}

.feature-card {
  text-align: center;
  padding: 2rem;
}

.feature-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 1rem;
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

.feature-card h3 {
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.feature-card p {
  color: var(--text-muted);
}

/* 반응형 */
@media (max-width: 480px) {
  .cta-buttons {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
