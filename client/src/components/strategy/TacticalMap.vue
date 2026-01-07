<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'

// Types
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

type Tool = 'select' | 'marker' | 'path' | 'note' | 'eraser'
type MarkerIcon = Marker['icon']

// Props & Emits
interface Props {
  modelValue?: MapData
  readonly?: boolean
  backgroundImage?: string
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false,
  backgroundImage: ''
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: MapData): void
  (e: 'change', value: MapData): void
}>()

// State
const canvasRef = ref<HTMLCanvasElement | null>(null)
const containerRef = ref<HTMLDivElement | null>(null)
let ctx: CanvasRenderingContext2D | null = null

const currentTool = ref<Tool>('select')
const currentColor = ref('#3b82f6')
const currentMarkerIcon = ref<MarkerIcon>('player')
const isDrawing = ref(false)
const currentPath = ref<PathPoint[]>([])

const mapData = ref<MapData>({
  markers: [],
  paths: [],
  notes: []
})

const selectedItem = ref<{ type: 'marker' | 'path' | 'note'; id: string } | null>(null)

// Colors
const colors = ['#3b82f6', '#ef4444', '#22c55e', '#f59e0b', '#8b5cf6', '#ec4899', '#ffffff']

// Marker icons
const markerIcons: { icon: MarkerIcon; label: string; emoji: string }[] = [
  { icon: 'player', label: '플레이어', emoji: '🟢' },
  { icon: 'enemy', label: '적', emoji: '🔴' },
  { icon: 'objective', label: '목표', emoji: '⭐' },
  { icon: 'danger', label: '위험', emoji: '⚠️' },
  { icon: 'item', label: '아이템', emoji: '📦' }
]

// Initialize
onMounted(() => {
  if (canvasRef.value) {
    ctx = canvasRef.value.getContext('2d')
    resizeCanvas()
    window.addEventListener('resize', resizeCanvas)

    if (props.modelValue) {
      mapData.value = JSON.parse(JSON.stringify(props.modelValue))
    }
    render()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas)
})

// Watch for external changes
watch(
  () => props.modelValue,
  newValue => {
    if (newValue) {
      mapData.value = JSON.parse(JSON.stringify(newValue))
      render()
    }
  },
  { deep: true }
)

const resizeCanvas = () => {
  if (canvasRef.value && containerRef.value) {
    const rect = containerRef.value.getBoundingClientRect()
    canvasRef.value.width = rect.width
    canvasRef.value.height = rect.height
    render()
  }
}

// Generate unique ID
const generateId = () => Math.random().toString(36).substring(2, 9)

// Render
const render = () => {
  if (!ctx || !canvasRef.value) return

  const canvas = canvasRef.value
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // Draw background grid
  drawGrid()

  // Draw paths
  mapData.value.paths.forEach(path => {
    drawPath(path)
  })

  // Draw current drawing path
  if (isDrawing.value && currentPath.value.length > 1) {
    drawPath({
      id: 'temp',
      points: currentPath.value,
      color: currentColor.value,
      width: 3
    })
  }

  // Draw markers
  mapData.value.markers.forEach(marker => {
    drawMarker(marker)
  })

  // Draw notes
  mapData.value.notes.forEach(note => {
    drawNote(note)
  })
}

const drawGrid = () => {
  if (!ctx || !canvasRef.value) return

  const canvas = canvasRef.value
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)'
  ctx.lineWidth = 1

  const gridSize = 40

  for (let x = 0; x < canvas.width; x += gridSize) {
    ctx.beginPath()
    ctx.moveTo(x, 0)
    ctx.lineTo(x, canvas.height)
    ctx.stroke()
  }

  for (let y = 0; y < canvas.height; y += gridSize) {
    ctx.beginPath()
    ctx.moveTo(0, y)
    ctx.lineTo(canvas.width, y)
    ctx.stroke()
  }
}

const drawPath = (path: Path) => {
  if (!ctx || path.points.length < 2) return

  const firstPoint = path.points[0]
  if (!firstPoint) return

  ctx.beginPath()
  ctx.strokeStyle = path.color
  ctx.lineWidth = path.width
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'

  ctx.moveTo(firstPoint.x, firstPoint.y)
  for (let i = 1; i < path.points.length; i++) {
    const point = path.points[i]
    if (point) {
      ctx.lineTo(point.x, point.y)
    }
  }
  ctx.stroke()
}

const drawMarker = (marker: Marker) => {
  if (!ctx) return

  const iconInfo = markerIcons.find(m => m.icon === marker.icon)
  const emoji = iconInfo?.emoji || '📍'

  ctx.font = '24px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText(emoji, marker.x, marker.y)

  if (marker.label) {
    ctx.font = '12px Arial'
    ctx.fillStyle = marker.color
    ctx.fillText(marker.label, marker.x, marker.y + 20)
  }
}

const drawNote = (note: Note) => {
  if (!ctx) return

  ctx.fillStyle = 'rgba(255, 255, 255, 0.9)'
  ctx.font = '14px Arial'
  ctx.textAlign = 'left'
  ctx.textBaseline = 'top'

  const padding = 8
  const metrics = ctx.measureText(note.text)
  const width = metrics.width + padding * 2
  const height = 24

  ctx.fillStyle = 'rgba(0, 0, 0, 0.8)'
  ctx.fillRect(note.x, note.y, width, height)

  ctx.fillStyle = '#ffffff'
  ctx.fillText(note.text, note.x + padding, note.y + 5)
}

// Event handlers
const getMousePos = (e: MouseEvent): { x: number; y: number } => {
  if (!canvasRef.value) return { x: 0, y: 0 }
  const rect = canvasRef.value.getBoundingClientRect()
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
}

const handleMouseDown = (e: MouseEvent) => {
  if (props.readonly) return

  const pos = getMousePos(e)

  switch (currentTool.value) {
    case 'marker':
      addMarker(pos)
      break
    case 'path':
      isDrawing.value = true
      currentPath.value = [pos]
      break
    case 'note':
      addNote(pos)
      break
    case 'eraser':
      eraseAt(pos)
      break
    case 'select':
      selectAt(pos)
      break
  }
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDrawing.value || currentTool.value !== 'path') return

  const pos = getMousePos(e)
  currentPath.value.push(pos)
  render()
}

const handleMouseUp = () => {
  if (currentTool.value === 'path' && isDrawing.value && currentPath.value.length > 1) {
    mapData.value.paths.push({
      id: generateId(),
      points: [...currentPath.value],
      color: currentColor.value,
      width: 3
    })
    emitChange()
  }

  isDrawing.value = false
  currentPath.value = []
  render()
}

const addMarker = (pos: { x: number; y: number }) => {
  const label = prompt('마커 레이블을 입력하세요:', '') || ''

  mapData.value.markers.push({
    id: generateId(),
    x: pos.x,
    y: pos.y,
    icon: currentMarkerIcon.value,
    label,
    color: currentColor.value
  })

  emitChange()
  render()
}

const addNote = (pos: { x: number; y: number }) => {
  const text = prompt('메모를 입력하세요:', '')
  if (!text) return

  mapData.value.notes.push({
    id: generateId(),
    x: pos.x,
    y: pos.y,
    text
  })

  emitChange()
  render()
}

const eraseAt = (pos: { x: number; y: number }) => {
  const threshold = 20

  // Erase markers
  mapData.value.markers = mapData.value.markers.filter(
    m => Math.abs(m.x - pos.x) > threshold || Math.abs(m.y - pos.y) > threshold
  )

  // Erase notes
  mapData.value.notes = mapData.value.notes.filter(
    n => Math.abs(n.x - pos.x) > threshold || Math.abs(n.y - pos.y) > threshold
  )

  // Erase paths (if near any point)
  mapData.value.paths = mapData.value.paths.filter(
    p =>
      !p.points.some(pt => Math.abs(pt.x - pos.x) < threshold && Math.abs(pt.y - pos.y) < threshold)
  )

  emitChange()
  render()
}

const selectAt = (pos: { x: number; y: number }) => {
  const threshold = 20

  // Check markers
  const marker = mapData.value.markers.find(
    m => Math.abs(m.x - pos.x) < threshold && Math.abs(m.y - pos.y) < threshold
  )
  if (marker) {
    selectedItem.value = { type: 'marker', id: marker.id }
    return
  }

  selectedItem.value = null
}

const clearAll = () => {
  if (confirm('모든 내용을 삭제하시겠습니까?')) {
    mapData.value = { markers: [], paths: [], notes: [] }
    emitChange()
    render()
  }
}

const emitChange = () => {
  emit('update:modelValue', JSON.parse(JSON.stringify(mapData.value)))
  emit('change', JSON.parse(JSON.stringify(mapData.value)))
}

// Tool selection
const selectTool = (tool: Tool) => {
  currentTool.value = tool
  selectedItem.value = null
}
</script>

<template>
  <div class="tactical-map-editor">
    <!-- Toolbar -->
    <div v-if="!readonly" class="toolbar">
      <div class="tool-group">
        <button
          v-for="tool in [
            { id: 'select', icon: '👆', label: '선택' },
            { id: 'marker', icon: '📍', label: '마커' },
            { id: 'path', icon: '✏️', label: '경로' },
            { id: 'note', icon: '📝', label: '메모' },
            { id: 'eraser', icon: '🧹', label: '지우기' }
          ]"
          :key="tool.id"
          :class="['tool-btn', { active: currentTool === tool.id }]"
          :title="tool.label"
          @click="selectTool(tool.id as Tool)"
        >
          {{ tool.icon }}
        </button>
      </div>

      <div v-if="currentTool === 'marker'" class="tool-group marker-icons">
        <button
          v-for="marker in markerIcons"
          :key="marker.icon"
          :class="['tool-btn', { active: currentMarkerIcon === marker.icon }]"
          :title="marker.label"
          @click="currentMarkerIcon = marker.icon"
        >
          {{ marker.emoji }}
        </button>
      </div>

      <div class="tool-group colors">
        <button
          v-for="color in colors"
          :key="color"
          :class="['color-btn', { active: currentColor === color }]"
          :style="{ backgroundColor: color }"
          @click="currentColor = color"
        />
      </div>

      <div class="tool-group">
        <button class="tool-btn danger" title="전체 삭제" @click="clearAll">🗑️</button>
      </div>
    </div>

    <!-- Canvas -->
    <div ref="containerRef" class="canvas-container">
      <canvas
        ref="canvasRef"
        @mousedown="handleMouseDown"
        @mousemove="handleMouseMove"
        @mouseup="handleMouseUp"
        @mouseleave="handleMouseUp"
      />
    </div>
  </div>
</template>

<style scoped>
.tactical-map-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(135deg, #0f0f1a 0%, #1a1a2e 100%);
  border-radius: 12px;
  overflow: hidden;
}

.toolbar {
  display: flex;
  gap: 16px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.5);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-wrap: wrap;
}

.tool-group {
  display: flex;
  gap: 4px;
  align-items: center;
}

.tool-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.2s;
}

.tool-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.tool-btn.active {
  background: var(--color-accent, #3b82f6);
  border-color: var(--color-accent, #3b82f6);
}

.tool-btn.danger:hover {
  background: rgba(239, 68, 68, 0.3);
  border-color: #ef4444;
}

.color-btn {
  width: 28px;
  height: 28px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  cursor: pointer;
  transition: transform 0.2s;
}

.color-btn:hover {
  transform: scale(1.1);
}

.color-btn.active {
  border-color: white;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
}

.canvas-container {
  flex: 1;
  position: relative;
  min-height: 400px;
}

canvas {
  width: 100%;
  height: 100%;
  cursor: crosshair;
}

.canvas-container:has(.tool-btn.active[title='선택']) canvas {
  cursor: pointer;
}
</style>
