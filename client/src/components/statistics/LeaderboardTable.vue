<template>
  <div class="leaderboard-table">
    <div class="table-header">
      <h3>🏆 팀 순위표</h3>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th class="rank-col">순위</th>
            <th class="team-col">팀</th>
            <th class="stat-col">경기</th>
            <th class="stat-col">평균순위</th>
            <th class="stat-col">승</th>
            <th class="stat-col">Top3</th>
            <th class="stat-col">총점</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="entry in entries"
            :key="entry.teamId"
            :class="{ 'top-rank': entry.rank <= 3, 'my-team': entry.teamId === myTeamId }"
          >
            <td class="rank-col">
              <span class="rank-badge" :class="getRankClass(entry.rank)">
                {{ getRankEmoji(entry.rank) }} {{ entry.rank }}
              </span>
            </td>
            <td class="team-col">
              <span class="team-name">{{ entry.teamName }}</span>
            </td>
            <td class="stat-col">{{ entry.totalMatches }}</td>
            <td class="stat-col">{{ entry.averageRank.toFixed(1) }}</td>
            <td class="stat-col">{{ entry.winCount }}</td>
            <td class="stat-col">{{ entry.top3Count }}</td>
            <td class="stat-col score">{{ entry.totalScore.toLocaleString() }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="entries.length === 0" class="empty-state">
        <p>아직 등록된 팀이 없습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { LeaderboardEntry } from '@/api/statistics'

defineProps<{
  entries: LeaderboardEntry[]
  myTeamId?: number
}>()

function getRankClass(rank: number): string {
  if (rank === 1) return 'gold'
  if (rank === 2) return 'silver'
  if (rank === 3) return 'bronze'
  return ''
}

function getRankEmoji(rank: number): string {
  if (rank === 1) return '🥇'
  if (rank === 2) return '🥈'
  if (rank === 3) return '🥉'
  return ''
}
</script>

<style scoped>
.leaderboard-table {
  background: var(--card-bg);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.table-header {
  padding: 1rem 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.table-header h3 {
  margin: 0;
  font-size: 1.125rem;
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: var(--bg-color-alt);
}

th,
td {
  padding: 0.75rem 1rem;
  text-align: left;
}

th {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

tbody tr {
  border-bottom: 1px solid var(--border-color);
  transition: background 0.2s;
}

tbody tr:hover {
  background: var(--bg-color-alt);
}

tbody tr:last-child {
  border-bottom: none;
}

.top-rank {
  background: linear-gradient(90deg, rgba(var(--primary-rgb), 0.05), transparent);
}

.my-team {
  background: rgba(var(--success-rgb), 0.1) !important;
}

.rank-col {
  width: 80px;
}

.team-col {
  min-width: 150px;
}

.stat-col {
  width: 80px;
  text-align: center;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-weight: 600;
}

.rank-badge.gold {
  color: #ffd700;
}

.rank-badge.silver {
  color: #c0c0c0;
}

.rank-badge.bronze {
  color: #cd7f32;
}

.team-name {
  font-weight: 500;
}

.score {
  font-weight: 600;
  color: var(--success-color);
}

.empty-state {
  padding: 3rem;
  text-align: center;
  color: var(--text-muted);
}
</style>
