<template>
  <div class="statistics-view">
    <header class="page-header">
      <h1>📊 통계 대시보드</h1>
      <p>팀 순위표와 성적을 확인하세요</p>
    </header>

    <!-- 내 팀 통계 섹션 -->
    <section v-if="hasTeam" class="my-team-section">
      <div class="section-header">
        <h2>내 팀 통계</h2>
        <button class="refresh-btn" :disabled="isLoading" @click="refreshMyTeamStats">
          <RefreshCw :size="18" :class="{ spinning: isLoading }" />
        </button>
      </div>

      <div v-if="teamStats" class="team-stats-grid">
        <TeamStatsCard :stats="teamStats" />
        <RecentPerformance v-if="recentPerformance" :performance="recentPerformance" />
      </div>

      <div v-else-if="isLoading" class="loading-state">
        <Spinner />
        <p>통계를 불러오는 중...</p>
      </div>

      <div v-else class="empty-state">
        <p>아직 기록된 스크림이 없습니다.</p>
      </div>
    </section>

    <!-- 팀이 없는 경우 -->
    <section v-else class="no-team-section">
      <div class="no-team-card">
        <Users :size="48" />
        <h3>팀에 가입하세요</h3>
        <p>팀에 가입하면 팀 통계를 확인할 수 있습니다.</p>
        <router-link to="/team" class="join-btn">팀 페이지로 이동</router-link>
      </div>
    </section>

    <!-- 리더보드 섹션 -->
    <section class="leaderboard-section">
      <div class="section-header">
        <h2>전체 팀 순위</h2>
        <button class="refresh-btn" :disabled="isLoading" @click="refreshLeaderboard">
          <RefreshCw :size="18" :class="{ spinning: isLoading }" />
        </button>
      </div>

      <LeaderboardTable :entries="leaderboard" :my-team-id="currentTeamId" />
    </section>

    <!-- 에러 메시지 -->
    <div v-if="error" class="error-banner">
      <AlertCircle :size="18" />
      <span>{{ error }}</span>
      <button @click="clearError">×</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { RefreshCw, Users, AlertCircle } from 'lucide-vue-next'
import { useStatisticsStore } from '@/stores/statistics'
import { useTeamStore } from '@/stores/team'
import TeamStatsCard from '@/components/statistics/TeamStatsCard.vue'
import LeaderboardTable from '@/components/statistics/LeaderboardTable.vue'
import RecentPerformance from '@/components/statistics/RecentPerformance.vue'
import Spinner from '@/components/common/Spinner.vue'

const statisticsStore = useStatisticsStore()
const teamStore = useTeamStore()

// Computed
const hasTeam = computed(() => teamStore.hasTeam)
const currentTeamId = computed(() => {
  const id = teamStore.currentTeam?.id
  return id ? Number(id) : undefined
})
const teamStats = computed(() => statisticsStore.teamStats)
const recentPerformance = computed(() => statisticsStore.recentPerformance)
const leaderboard = computed(() => statisticsStore.leaderboard)
const isLoading = computed(() => statisticsStore.isLoading)
const error = computed(() => statisticsStore.error)

// 액션
async function refreshMyTeamStats() {
  if (!currentTeamId.value) return
  await Promise.all([
    statisticsStore.fetchTeamStats(currentTeamId.value),
    statisticsStore.fetchRecentPerformance(currentTeamId.value)
  ])
}

async function refreshLeaderboard() {
  await statisticsStore.fetchLeaderboard()
}

function clearError() {
  statisticsStore.clearError()
}

// 초기 로드
onMounted(async () => {
  // 팀 정보 먼저 로드
  if (!teamStore.currentTeam) {
    await teamStore.fetchMyTeam()
  }

  // 리더보드 로드
  await statisticsStore.fetchLeaderboard()

  // 내 팀 통계 로드
  if (currentTeamId.value) {
    await Promise.all([
      statisticsStore.fetchTeamStats(currentTeamId.value),
      statisticsStore.fetchRecentPerformance(currentTeamId.value)
    ])
  }
})
</script>

<style scoped>
.statistics-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-header h1 {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.page-header p {
  color: var(--text-muted);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h2 {
  font-size: 1.25rem;
  margin: 0;
}

.refresh-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
  color: var(--text-muted);
  cursor: pointer;
  transition: all 0.2s;
}

.refresh-btn:hover:not(:disabled) {
  background: var(--primary-color);
  color: white;
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.my-team-section {
  margin-bottom: 3rem;
}

.team-stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

@media (max-width: 992px) {
  .team-stats-grid {
    grid-template-columns: 1fr;
  }
}

.leaderboard-section {
  margin-bottom: 2rem;
}

.no-team-section {
  margin-bottom: 3rem;
}

.no-team-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem;
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  text-align: center;
}

.no-team-card svg {
  color: var(--text-muted);
  margin-bottom: 1rem;
}

.no-team-card h3 {
  margin-bottom: 0.5rem;
}

.no-team-card p {
  color: var(--text-muted);
  margin-bottom: 1.5rem;
}

.join-btn {
  display: inline-flex;
  padding: 0.75rem 1.5rem;
  background: var(--primary-color);
  color: white;
  text-decoration: none;
  border-radius: var(--radius-md);
  font-weight: 500;
  transition: background 0.2s;
}

.join-btn:hover {
  background: var(--primary-color-dark);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3rem;
  color: var(--text-muted);
}

.empty-state {
  padding: 3rem;
  text-align: center;
  color: var(--text-muted);
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}

.error-banner {
  position: fixed;
  bottom: 2rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: var(--error-color);
  color: white;
  border-radius: var(--radius-md);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.error-banner button {
  background: none;
  border: none;
  color: white;
  font-size: 1.25rem;
  cursor: pointer;
  opacity: 0.8;
}

.error-banner button:hover {
  opacity: 1;
}
</style>
