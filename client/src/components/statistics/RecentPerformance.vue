<template>
  <div class="recent-performance">
    <div class="header">
      <h3>📊 최근 {{ performance.matchCount }}경기 성적</h3>
      <span class="trend-badge" :class="trendClass">
        {{ trendIcon }} {{ trendLabel }}
      </span>
    </div>
    
    <div class="summary-stats">
      <div class="summary-item">
        <span class="label">평균 순위</span>
        <span class="value">{{ performance.averageRank.toFixed(1) }}</span>
      </div>
      <div class="summary-item">
        <span class="label">총 킬</span>
        <span class="value">{{ performance.totalKills }}</span>
      </div>
      <div class="summary-item">
        <span class="label">총 점수</span>
        <span class="value highlight">{{ performance.totalScore }}</span>
      </div>
      <div class="summary-item">
        <span class="label">우승</span>
        <span class="value win">{{ performance.winCount }}회</span>
      </div>
    </div>
    
    <div class="matches-list">
      <div 
        v-for="match in performance.matches" 
        :key="match.matchId"
        class="match-item"
        :class="{ 'win': match.rank === 1, 'top3': match.rank <= 3 }"
      >
        <span class="match-rank">
          {{ match.rank === 1 ? '🏆' : match.rank <= 3 ? '🎖️' : '' }}
          #{{ match.rank }}
        </span>
        <span class="match-kills">{{ match.kills }} kills</span>
        <span class="match-score">+{{ match.score }}점</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { RecentPerformanceResponse } from '@/api/statistics'

const props = defineProps<{
  performance: RecentPerformanceResponse
}>()

const trendClass = computed(() => {
  switch (props.performance.trend) {
    case 'IMPROVING': return 'improving'
    case 'DECLINING': return 'declining'
    default: return 'stable'
  }
})

const trendIcon = computed(() => {
  switch (props.performance.trend) {
    case 'IMPROVING': return '📈'
    case 'DECLINING': return '📉'
    default: return '➡️'
  }
})

const trendLabel = computed(() => {
  switch (props.performance.trend) {
    case 'IMPROVING': return '상승세'
    case 'DECLINING': return '하락세'
    default: return '안정'
  }
})
</script>

<style scoped>
.recent-performance {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  border: 1px solid var(--border-color);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.header h3 {
  margin: 0;
  font-size: 1.125rem;
}

.trend-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-full);
  font-size: 0.875rem;
  font-weight: 500;
}

.trend-badge.improving {
  background: rgba(var(--success-rgb), 0.1);
  color: var(--success-color);
}

.trend-badge.declining {
  background: rgba(var(--error-rgb), 0.1);
  color: var(--error-color);
}

.trend-badge.stable {
  background: var(--bg-color-alt);
  color: var(--text-muted);
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.summary-item {
  text-align: center;
  padding: 0.75rem;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
}

.summary-item .label {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 0.25rem;
}

.summary-item .value {
  font-size: 1.25rem;
  font-weight: 600;
}

.summary-item .value.highlight {
  color: var(--success-color);
}

.summary-item .value.win {
  color: var(--primary-color);
}

.matches-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.match-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0.75rem;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
  font-size: 0.875rem;
}

.match-item.win {
  background: linear-gradient(135deg, rgba(var(--primary-rgb), 0.15), rgba(var(--success-rgb), 0.15));
  border: 1px solid rgba(var(--primary-rgb), 0.3);
}

.match-item.top3:not(.win) {
  border: 1px solid rgba(var(--success-rgb), 0.3);
}

.match-rank {
  font-weight: 600;
}

.match-kills {
  color: var(--text-muted);
}

.match-score {
  color: var(--success-color);
  font-weight: 500;
}

@media (max-width: 768px) {
  .summary-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
