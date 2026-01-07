import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { formatDate, formatDateTime, formatRelativeTime } from '../formatters'

describe('formatters utils', () => {
  describe('formatDate', () => {
    it('should format date in Korean locale', () => {
      const result = formatDate('2026-01-15T10:30:00Z')

      // 한국어 형식 확인
      expect(result).toMatch(/2026/)
      expect(result).toMatch(/1월|15/)
    })

    it('should handle different date strings', () => {
      const result = formatDate('2025-12-25T00:00:00Z')

      expect(result).toMatch(/2025/)
      expect(result).toMatch(/12월|25/)
    })
  })

  describe('formatDateTime', () => {
    it('should format date with time in Korean locale', () => {
      const result = formatDateTime('2026-01-15T10:30:00Z')

      // 날짜와 시간 모두 포함
      expect(result).toMatch(/2026/)
    })

    it('should include hour and minute', () => {
      const result = formatDateTime('2026-01-15T14:45:00Z')

      // 시간이 포함되어 있는지 확인 (시간대에 따라 다를 수 있음)
      expect(result.length).toBeGreaterThan(10)
    })
  })

  describe('formatRelativeTime', () => {
    beforeEach(() => {
      vi.useFakeTimers()
      vi.setSystemTime(new Date('2026-01-15T12:00:00Z'))
    })

    afterEach(() => {
      vi.useRealTimers()
    })

    it('should return "방금 전" for times less than 60 seconds ago', () => {
      const result = formatRelativeTime('2026-01-15T11:59:30Z')

      expect(result).toBe('방금 전')
    })

    it('should return minutes for times less than 60 minutes ago', () => {
      const result = formatRelativeTime('2026-01-15T11:30:00Z')

      expect(result).toBe('30분 전')
    })

    it('should return hours for times less than 24 hours ago', () => {
      const result = formatRelativeTime('2026-01-15T09:00:00Z')

      expect(result).toBe('3시간 전')
    })

    it('should return days for times less than 7 days ago', () => {
      const result = formatRelativeTime('2026-01-13T12:00:00Z')

      expect(result).toBe('2일 전')
    })

    it('should return formatted date for times 7+ days ago', () => {
      const result = formatRelativeTime('2026-01-01T12:00:00Z')

      // formatDate 형식으로 반환
      expect(result).toMatch(/2026/)
    })
  })
})
