import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useScrimStore } from '../scrim'
import type { Scrim, ScrimResult } from '@/types/scrim'

// API 모킹
vi.mock('@/api/scrim', () => ({
  scrimApi: {
    getScrims: vi.fn(),
    getScrim: vi.fn(),
    createScrim: vi.fn(),
    updateScrim: vi.fn(),
    deleteScrim: vi.fn(),
    updateStatus: vi.fn(),
    addResult: vi.fn(),
    deleteResult: vi.fn()
  }
}))

// Toast 모킹
vi.mock('@/composables/useToast', () => ({
  useToast: () => ({
    success: vi.fn(),
    error: vi.fn(),
    info: vi.fn(),
    warning: vi.fn()
  })
}))

// 테스트용 mock 데이터
const mockScrim: Scrim = {
  id: 'scrim-1',
  title: 'Test Scrim',
  scheduledAt: '2026-01-15T14:00:00Z',
  status: 'SCHEDULED',
  teamId: 'team-1',
  opponentTeamName: 'Opponent Team',
  results: [],
  createdAt: '2026-01-01T00:00:00Z'
}

const mockResult: ScrimResult = {
  id: 'result-1',
  scrimId: 'scrim-1',
  round: 1,
  placement: 1,
  kills: 10,
  notes: 'Great performance'
}

describe('useScrimStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('initial state', () => {
    it('should have empty scrims array initially', () => {
      const store = useScrimStore()
      expect(store.scrims).toEqual([])
    })

    it('should have null currentScrim initially', () => {
      const store = useScrimStore()
      expect(store.currentScrim).toBeNull()
    })

    it('should not be loading initially', () => {
      const store = useScrimStore()
      expect(store.isLoading).toBe(false)
    })

    it('should have no error initially', () => {
      const store = useScrimStore()
      expect(store.error).toBeNull()
    })

    it('should have ALL as default status filter', () => {
      const store = useScrimStore()
      expect(store.statusFilter).toBe('ALL')
    })
  })

  describe('filteredScrims', () => {
    it('should return all scrims when filter is ALL', () => {
      const store = useScrimStore()
      store.scrims = [
        { ...mockScrim, id: '1', status: 'SCHEDULED' },
        { ...mockScrim, id: '2', status: 'COMPLETED' },
        { ...mockScrim, id: '3', status: 'IN_PROGRESS' }
      ]

      expect(store.filteredScrims).toHaveLength(3)
    })

    it('should filter by status', () => {
      const store = useScrimStore()
      store.scrims = [
        { ...mockScrim, id: '1', status: 'SCHEDULED' },
        { ...mockScrim, id: '2', status: 'COMPLETED' },
        { ...mockScrim, id: '3', status: 'SCHEDULED' }
      ]
      store.setStatusFilter('SCHEDULED')

      expect(store.filteredScrims).toHaveLength(2)
      expect(store.filteredScrims.every(s => s.status === 'SCHEDULED')).toBe(true)
    })
  })

  describe('scheduledScrims', () => {
    it('should return only scheduled scrims', () => {
      const store = useScrimStore()
      store.scrims = [
        { ...mockScrim, id: '1', status: 'SCHEDULED' },
        { ...mockScrim, id: '2', status: 'COMPLETED' },
        { ...mockScrim, id: '3', status: 'SCHEDULED' }
      ]

      expect(store.scheduledScrims).toHaveLength(2)
    })
  })

  describe('completedScrims', () => {
    it('should return only completed scrims', () => {
      const store = useScrimStore()
      store.scrims = [
        { ...mockScrim, id: '1', status: 'SCHEDULED' },
        { ...mockScrim, id: '2', status: 'COMPLETED' },
        { ...mockScrim, id: '3', status: 'COMPLETED' }
      ]

      expect(store.completedScrims).toHaveLength(2)
    })
  })

  describe('scrimStats', () => {
    it('should return zero stats when no completed scrims', () => {
      const store = useScrimStore()
      store.scrims = []

      expect(store.scrimStats).toEqual({
        count: 0,
        avgPlacement: 0,
        totalKills: 0
      })
    })

    it('should calculate correct stats from completed scrims', () => {
      const store = useScrimStore()
      store.scrims = [
        {
          ...mockScrim,
          id: '1',
          status: 'COMPLETED',
          results: [
            { ...mockResult, placement: 1, kills: 10 },
            { ...mockResult, placement: 3, kills: 5 }
          ]
        },
        {
          ...mockScrim,
          id: '2',
          status: 'COMPLETED',
          results: [{ ...mockResult, placement: 2, kills: 8 }]
        }
      ]

      expect(store.scrimStats.count).toBe(2)
      expect(store.scrimStats.avgPlacement).toBe('2.0') // (1+3+2)/3 = 2.0
      expect(store.scrimStats.totalKills).toBe(23) // 10+5+8 = 23
    })
  })

  describe('setStatusFilter', () => {
    it('should update status filter', () => {
      const store = useScrimStore()

      store.setStatusFilter('COMPLETED')

      expect(store.statusFilter).toBe('COMPLETED')
    })
  })

  describe('clearError', () => {
    it('should clear error', () => {
      const store = useScrimStore()
      store.error = 'Some error'

      store.clearError()

      expect(store.error).toBeNull()
    })
  })
})
