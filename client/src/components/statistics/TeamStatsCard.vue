<template>
  <div class="team-stats-card">
    <div class="card-header">
      <h3>{{ stats.teamName }}</h3>
      <span class="match-count">{{ stats.totalMatches }}경기</span>
    </div>
    
    <div class="stats-grid">
      <div class="stat-item">
        <span class="stat-label">평균 순위</span>
        <span class="stat-value rank">{{ stats.averageRank.toFixed(1) }}</span>
      </div>
      
      <div class="stat-item">
        <span class="stat-label">승률</span>
        <span class="stat-value">{{ stats.winRate.toFixed(1) }}%</span>
      </div>
      
      <div class="stat-item">
        <span class="stat-label">Top3 비율</span>
        <span class="stat-value">{{ stats.top3Rate.toFixed(1) }}%</span>
      </div>
      
      <div class="stat-item">
        <span class="stat-label">총 킬</span>
        <span class="stat-value">{{ stats.totalKills }}</span>
      </div>
      
      <div class="stat-item">
        <span class="stat-label">경기당 킬</span>
        <span class="stat-value">{{ stats.averageKillsPerMatch.toFixed(1) }}</span>
      </div>
      
      <div class="stat-item">
        <span class="stat-label">총 점수</span>
        <span class="stat-value highlight">{{ stats.totalScore.toLocaleString() }}</span>
      </div>
    </div>
    
    <div class="win-stats">
      <div class="win-bar">
        <div class="win-fill" :style="{ width: stats.winRate + '%' }"></div>
      </div>
      <div class="win-details">
        <span>🏆 {{ stats.winCount }}승</span>
        <span>🥇🥈🥉 Top3 {{ stats.top3Count }}회</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TeamStatsResponse } from '@/api/statistics'

defineProps<{
  stats: TeamStatsResponse
}>()
</script>

<style scoped>
.team-stats-card {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  border: 1px solid var(--border-color);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.card-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: var(--text-color);
}

.match-count {
  font-size: 0.875rem;
  color: var(--text-muted);
  background: var(--bg-color-alt);
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-full);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-item {
  text-align: center;
  padding: 0.75rem;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
}

.stat-label {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-color);
}

.stat-value.rank {
  color: var(--primary-color);
}

.stat-value.highlight {
  color: var(--success-color);
}

.win-stats {
  margin-top: 1rem;
}

.win-bar {
  height: 8px;
  background: var(--bg-color-alt);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.win-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--success-color));
  border-radius: var(--radius-full);
  transition: width 0.5s ease;
}

.win-details {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: var(--text-muted);
}
</style>
