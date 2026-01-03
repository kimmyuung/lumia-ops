<template>
  <div class="home">
    <section class="hero">
      <div class="hero-content">
        <div class="hero-badge">
          <Sparkles :size="16" />
          <span>이터널 리턴 전략 플랫폼</span>
        </div>
        <h1>
          <Map :size="48" class="hero-icon" />
          <span>Lumia Ops</span>
        </h1>
        <p class="tagline">팀 전략 수립부터 스크림 관리까지, 승리를 위한 모든 것</p>
        <div class="cta-buttons">
          <Button to="/team" variant="primary" size="lg">
            <Users :size="20" />
            <span>팀 관리</span>
          </Button>
          <Button to="/strategy" variant="secondary" size="lg">
            <Target :size="20" />
            <span>전략 보드</span>
          </Button>
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
import { Map, Users, Target, Trophy, Sparkles } from 'lucide-vue-next'
import { Card, Button } from '@/components/common'
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
    rgba(102, 126, 234, 0.08) 0%,
    rgba(118, 75, 162, 0.08) 100%
  );
  position: relative;
  overflow: hidden;
}

.hero::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle at 30% 50%,
    rgba(102, 126, 234, 0.1) 0%,
    transparent 50%
  );
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(2%, 2%) rotate(5deg); }
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  font-size: 0.875rem;
  color: var(--primary-color);
  margin-bottom: 1.5rem;
}

.hero h1 {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  font-size: clamp(2.5rem, 6vw, 4rem);
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
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.tagline {
  font-size: clamp(1rem, 2.5vw, 1.25rem);
  color: var(--text-muted);
  margin-bottom: 2.5rem;
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
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
  padding: 2.5rem 2rem;
}

.feature-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 1.25rem;
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

.feature-card:hover .feature-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: var(--shadow-glow);
}

.feature-card h3 {
  margin-bottom: 0.5rem;
  color: var(--text-color);
  font-size: 1.25rem;
}

.feature-card p {
  color: var(--text-muted);
  font-size: 0.95rem;
}

/* 반응형 */
@media (max-width: 480px) {
  .cta-buttons {
    flex-direction: column;
  }
  
  .cta-buttons :deep(.btn) {
    width: 100%;
    justify-content: center;
  }
}
