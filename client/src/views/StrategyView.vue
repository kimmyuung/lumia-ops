<template>
  <div class="strategy-view">
    <div class="container">
      <PageHeader
        title="전략 수립 보드"
        description="실시간 동기화 택티컬 맵으로 팀 전략을 수립하세요."
        :icon="Target"
      />

      <!-- Strategy Controls -->
      <section class="controls-section">
        <div class="controls-left">
          <input
            v-model="strategyTitle"
            type="text"
            class="title-input"
            placeholder="전략 제목을 입력하세요..."
          />
        </div>
        <div class="controls-right">
          <Button variant="secondary" :disabled="loading" @click="handleLoad">
            <FolderOpen :size="18" />
            <span>불러오기</span>
          </Button>
          <Button variant="primary" :disabled="loading || !strategyTitle" @click="handleSave">
            <Save :size="18" />
            <span>저장</span>
          </Button>
        </div>
      </section>

      <!-- Tactical Map -->
      <section class="map-section">
        <TacticalMap v-model="mapData" @change="handleMapChange" />
      </section>

      <!-- Saved Strategies List -->
      <section class="strategies-section">
        <h2>
          <FileText :size="20" />
          저장된 전략
        </h2>
        <div v-if="strategies.length > 0" class="strategies-grid">
          <Card
            v-for="strategy in strategies"
            :key="strategy.id"
            hoverable
            class="strategy-card"
            @click="loadStrategy(strategy)"
          >
            <h4>{{ strategy.title }}</h4>
            <p class="strategy-date">{{ formatDate(strategy.updatedAt) }}</p>
          </Card>
        </div>
        <div v-else class="empty-state">
          <p>저장된 전략이 없습니다.</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Target, Save, FolderOpen, FileText } from 'lucide-vue-next'
import { Card, Button, PageHeader } from '@/components/common'
import TacticalMap from '@/components/strategy/TacticalMap.vue'
import { useToast } from '@/composables/useToast'

interface Marker {
  id: string
  x: number
  y: number
  icon: 'player' | 'enemy' | 'objective' | 'danger' | 'item'
  label: string
  color: string
}

interface PathPoint {
  x: number
  y: number
}

interface Path {
  id: string
  points: PathPoint[]
  color: string
  width: number
}

interface Note {
  id: string
  x: number
  y: number
  text: string
}

interface MapData {
  markers: Marker[]
  paths: Path[]
  notes: Note[]
}

interface Strategy {
  id: number
  title: string
  mapData: string
  createdAt: string
  updatedAt: string
}

const { show } = useToast()

const strategyTitle = ref('')
const mapData = ref<MapData>({ markers: [], paths: [], notes: [] })
const strategies = ref<Strategy[]>([])
const loading = ref(false)
const currentStrategyId = ref<number | null>(null)

onMounted(() => {
  fetchStrategies()
})

const fetchStrategies = async () => {
  try {
    // API 호출 (현재는 localStorage 사용)
    const saved = localStorage.getItem('lumia-strategies')
    if (saved) {
      strategies.value = JSON.parse(saved)
    }
  } catch (error) {
    console.error('Failed to fetch strategies:', error)
  }
}

const handleMapChange = (data: MapData) => {
  mapData.value = data
}

const handleSave = async () => {
  if (!strategyTitle.value.trim()) {
    show('제목을 입력하세요', 'warning')
    return
  }

  loading.value = true
  try {
    const now = new Date().toISOString()

    if (currentStrategyId.value) {
      // Update existing
      const index = strategies.value.findIndex(s => s.id === currentStrategyId.value)
      if (index >= 0) {
        const existing = strategies.value[index]
        if (existing) {
          strategies.value[index] = {
            id: existing.id,
            title: strategyTitle.value,
            mapData: JSON.stringify(mapData.value),
            createdAt: existing.createdAt,
            updatedAt: now
          }
        }
      }
    } else {
      // Create new
      const newStrategy: Strategy = {
        id: Date.now(),
        title: strategyTitle.value,
        mapData: JSON.stringify(mapData.value),
        createdAt: now,
        updatedAt: now
      }
      strategies.value.unshift(newStrategy)
      currentStrategyId.value = newStrategy.id
    }

    // Save to localStorage (later: API call)
    localStorage.setItem('lumia-strategies', JSON.stringify(strategies.value))
    show('저장되었습니다', 'success')
  } catch (error) {
    show('저장에 실패했습니다', 'error')
  } finally {
    loading.value = false
  }
}

const handleLoad = () => {
  // Show strategies list (scroll to it)
  document.querySelector('.strategies-section')?.scrollIntoView({ behavior: 'smooth' })
}

const loadStrategy = (strategy: Strategy) => {
  currentStrategyId.value = strategy.id
  strategyTitle.value = strategy.title
  try {
    mapData.value = JSON.parse(strategy.mapData)
  } catch {
    mapData.value = { markers: [], paths: [], notes: [] }
  }
  show(`"${strategy.title}" 전략을 불러왔습니다`, 'success')
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}
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

.controls-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.controls-left {
  flex: 1;
  min-width: 200px;
}

.title-input {
  width: 100%;
  max-width: 400px;
  padding: 12px 16px;
  font-size: 1rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background: var(--color-background);
  color: var(--color-text);
}

.title-input:focus {
  outline: none;
  border-color: var(--color-accent);
}

.controls-right {
  display: flex;
  gap: 8px;
}

.map-section {
  margin-bottom: var(--section-gap);
  height: 600px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.strategies-section {
  margin-top: 48px;
}

.strategies-section h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  color: var(--color-text);
}

.strategies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.strategy-card {
  cursor: pointer;
  padding: 20px;
  transition: all 0.2s;
}

.strategy-card:hover {
  transform: translateY(-2px);
}

.strategy-card h4 {
  margin-bottom: 8px;
  color: var(--color-text);
}

.strategy-date {
  font-size: 0.875rem;
  color: var(--color-text-mute);
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: var(--color-text-mute);
  background: var(--color-background-soft);
  border-radius: 12px;
}

@media (max-width: 768px) {
  .map-section {
    height: 450px;
  }

  .controls-section {
    flex-direction: column;
    align-items: stretch;
  }

  .controls-right {
    justify-content: flex-end;
  }
}
</style>
