<template>
  <div class="home">
    <section class="hero">
      <h1>🗺️ Lumia Ops</h1>
      <p class="tagline">이터널 리턴 팀 전략 & 스크림 관리 플랫폼</p>
      <div class="cta-buttons">
        <router-link to="/team" class="btn btn-primary">팀 관리</router-link>
        <router-link to="/strategy" class="btn btn-secondary">전략 보드</router-link>
      </div>
    </section>

    <section class="features">
      <div class="feature-card">
        <h3>📋 팀 관리</h3>
        <p>팀원 초대, 역할 배정, 일정 관리</p>
      </div>
      <div class="feature-card">
        <h3>🗺️ 전략 수립</h3>
        <p>실시간 동기화 택티컬 맵</p>
      </div>
      <div class="feature-card">
        <h3>⚔️ 스크림 기록</h3>
        <p>자동화된 스크림 매니지먼트</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
// Home view logic
</script>

<style scoped>
.home {
  min-height: 100vh;
  padding: 2rem;
}

.hero {
  text-align: center;
  padding: 4rem 0;
}

.hero h1 {
  font-size: 3rem;
  margin-bottom: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.tagline {
  font-size: 1.25rem;
  color: #666;
  margin-bottom: 2rem;
}

.cta-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
  max-width: 1200px;
  margin: 4rem auto;
}

.feature-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
}

.feature-card h3 {
  margin-bottom: 0.5rem;
  color: #333;
}

.feature-card p {
  color: #666;
}
</style>
