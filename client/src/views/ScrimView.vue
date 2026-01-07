<template>
  <div class="scrim-view">
    <div class="container">
      <PageHeader
        title="스크림 관리"
        description="자동화된 스크림 매니지먼트 시스템"
        :icon="Swords"
      >
        <template #actions>
          <Button variant="primary" @click="showCreateModal = true">
            <Plus :size="20" />
            <span>스크림 생성</span>
          </Button>
        </template>
      </PageHeader>

      <!-- 통계 카드 -->
      <section class="stats-section">
        <template v-if="isLoading && scrims.length === 0">
          <Skeleton v-for="i in 3" :key="i" class="stat-skeleton" />
        </template>
        <template v-else>
          <Card class="stat-card">
            <div class="stat-icon">
              <Calendar :size="24" />
            </div>
            <div class="stat-content">
              <span class="stat-value">{{ scheduledScrims.length }}</span>
              <span class="stat-label">예정된 스크림</span>
            </div>
          </Card>
          <Card class="stat-card">
            <div class="stat-icon completed">
              <Trophy :size="24" />
            </div>
            <div class="stat-content">
              <span class="stat-value">{{ scrimStats.count }}</span>
              <span class="stat-label">완료된 스크림</span>
            </div>
          </Card>
          <Card class="stat-card">
            <div class="stat-icon average">
              <TrendingUp :size="24" />
            </div>
            <div class="stat-content">
              <span class="stat-value">{{ scrimStats.avgPlacement }}</span>
              <span class="stat-label">평균 순위</span>
            </div>
          </Card>
        </template>
      </section>

      <!-- 필터 탭 -->
      <section class="filter-section">
        <div class="filter-tabs">
          <button
            v-for="filter in filters"
            :key="filter.value"
            class="filter-tab"
            :class="{ active: statusFilter === filter.value }"
            @click="setStatusFilter(filter.value)"
          >
            {{ filter.label }}
          </button>
        </div>
      </section>

      <!-- 스크림 목록 -->
      <section class="scrims-section">
        <template v-if="isLoading && scrims.length === 0">
          <SkeletonCard v-for="i in 3" :key="i" />
        </template>
        <template v-else-if="filteredScrims.length > 0">
          <Card
            v-for="scrim in filteredScrims"
            :key="scrim.id"
            hoverable
            class="scrim-card"
            @click="selectScrim(scrim)"
          >
            <div class="scrim-header">
              <div class="scrim-info">
                <h3>{{ scrim.title }}</h3>
                <div class="scrim-meta">
                  <span class="scrim-date">
                    <Calendar :size="14" />
                    {{ formatDate(scrim.scheduledAt) }}
                  </span>
                  <span v-if="scrim.opponentTeamName" class="scrim-opponent">
                    vs {{ scrim.opponentTeamName }}
                  </span>
                </div>
              </div>
              <span class="scrim-status" :class="scrim.status.toLowerCase()">
                {{ getStatusLabel(scrim.status) }}
              </span>
            </div>
            <div v-if="scrim.results.length > 0" class="scrim-results">
              <span class="results-label">결과:</span>
              <div class="results-summary">
                <span v-for="(result, idx) in scrim.results.slice(0, 3)" :key="result.id">
                  {{ result.placement }}위 ({{ result.kills }}킬){{
                    idx < Math.min(scrim.results.length, 3) - 1 ? ', ' : ''
                  }}
                </span>
                <span v-if="scrim.results.length > 3">... +{{ scrim.results.length - 3 }}</span>
              </div>
            </div>
            <div class="scrim-actions">
              <Button variant="ghost" size="sm" @click.stop="editScrim(scrim)">
                <Edit :size="16" />
              </Button>
              <Button variant="ghost" size="sm" @click.stop="confirmDelete(scrim)">
                <Trash2 :size="16" />
              </Button>
            </div>
          </Card>
        </template>
        <template v-else>
          <div class="empty-state">
            <div class="empty-icon">
              <Swords :size="64" />
            </div>
            <h2>스크림 기록이 없습니다</h2>
            <p>새로운 스크림을 생성하여 기록을 시작하세요.</p>
            <Button variant="primary" @click="showCreateModal = true">
              <Plus :size="20" />
              <span>첫 스크림 생성하기</span>
            </Button>
          </div>
        </template>
      </section>
    </div>

    <!-- 스크림 생성/수정 모달 -->
    <ScrimFormModal
      v-if="showCreateModal || showEditModal"
      :scrim="showEditModal ? selectedScrim : null"
      @close="closeModals"
      @saved="handleScrimSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Swords, Plus, Calendar, Trophy, TrendingUp, Edit, Trash2 } from 'lucide-vue-next'
import { Card, Button, PageHeader, Skeleton, SkeletonCard } from '@/components/common'
import ScrimFormModal from '@/components/scrim/ScrimFormModal.vue'
import { useScrimStore } from '@/stores/scrim'
import { useConfirm } from '@/composables/useConfirm'
import { storeToRefs } from 'pinia'
import type { Scrim, ScrimStatus } from '@/types/scrim'
import { formatDateTime } from '@/utils/formatters'

const scrimStore = useScrimStore()
const { confirm } = useConfirm()

const { scrims, filteredScrims, scheduledScrims, scrimStats, isLoading, statusFilter } =
  storeToRefs(scrimStore)

const showCreateModal = ref(false)
const showEditModal = ref(false)
const selectedScrim = ref<Scrim | null>(null)

const filters = [
  { label: '전체', value: 'ALL' as const },
  { label: '예정됨', value: 'SCHEDULED' as ScrimStatus },
  { label: '진행 중', value: 'IN_PROGRESS' as ScrimStatus },
  { label: '완료', value: 'COMPLETED' as ScrimStatus }
]

onMounted(() => {
  scrimStore.fetchScrims()
})

function formatDate(dateString: string): string {
  return formatDateTime(dateString)
}

function getStatusLabel(status: ScrimStatus): string {
  switch (status) {
    case 'SCHEDULED':
      return '예정됨'
    case 'IN_PROGRESS':
      return '진행 중'
    case 'COMPLETED':
      return '완료'
    case 'CANCELLED':
      return '취소됨'
    default:
      return status
  }
}

function setStatusFilter(status: ScrimStatus | 'ALL') {
  scrimStore.setStatusFilter(status)
}

function selectScrim(scrim: Scrim) {
  selectedScrim.value = scrim
}

function editScrim(scrim: Scrim) {
  selectedScrim.value = scrim
  showEditModal.value = true
}

function closeModals() {
  showCreateModal.value = false
  showEditModal.value = false
  selectedScrim.value = null
}

function handleScrimSaved(_scrim: Scrim) {
  closeModals()
}

async function confirmDelete(scrim: Scrim) {
  const confirmed = await confirm({
    title: '스크림 삭제',
    message: `"${scrim.title}" 스크림을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`,
    confirmText: '삭제',
    cancelText: '취소',
    variant: 'danger'
  })

  if (confirmed) {
    await scrimStore.deleteScrim(scrim.id)
  }
}
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

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-skeleton {
  height: 100px;
  border-radius: var(--radius-lg);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem;
}

.stat-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border-radius: var(--radius-md);
  color: var(--primary-color);
}

.stat-icon.completed {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(16, 185, 129, 0.25) 100%);
  color: var(--success-color);
}

.stat-icon.average {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.15) 0%, rgba(245, 158, 11, 0.25) 100%);
  color: var(--warning-color);
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-color);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-muted);
}

.filter-section {
  margin-bottom: 1.5rem;
}

.filter-tabs {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 0.5rem 1rem;
  border: none;
  background: var(--bg-color-alt);
  color: var(--text-muted);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-weight: 500;
}

.filter-tab:hover {
  background: var(--border-color);
  color: var(--text-color);
}

.filter-tab.active {
  background: var(--primary-color);
  color: white;
}

.scrims-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.scrim-card {
  padding: 1.25rem;
  cursor: pointer;
  position: relative;
}

.scrim-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.scrim-info h3 {
  margin-bottom: 0.25rem;
  color: var(--text-color);
}

.scrim-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: var(--text-muted);
}

.scrim-date {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.scrim-status {
  font-size: 0.75rem;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.scrim-status.scheduled {
  background: rgba(59, 130, 246, 0.1);
  color: var(--info-color);
}

.scrim-status.in_progress {
  background: rgba(245, 158, 11, 0.1);
  color: var(--warning-color);
}

.scrim-status.completed {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
}

.scrim-status.cancelled {
  background: rgba(239, 68, 68, 0.1);
  color: var(--error-color);
}

.scrim-results {
  display: flex;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-muted);
  margin-bottom: 0.5rem;
}

.results-label {
  font-weight: 500;
}

.scrim-actions {
  position: absolute;
  top: 1rem;
  right: 1rem;
  display: flex;
  gap: 0.25rem;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.scrim-card:hover .scrim-actions {
  opacity: 1;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--card-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border-radius: 50%;
  color: var(--primary-color);
}

.empty-state h2 {
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: 2rem;
}

@media (max-width: 768px) {
  .stats-section {
    grid-template-columns: 1fr;
  }

  .scrim-actions {
    opacity: 1;
  }
}
</style>
