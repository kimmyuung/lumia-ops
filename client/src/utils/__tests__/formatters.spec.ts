import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { formatDate, formatDateTime, formatRelativeTime } from '../formatters'

describe('formatters', () => {
    describe('formatDate', () => {
        it('should format date in Korean locale', () => {
            const result = formatDate('2026-01-01T12:00:00Z')
            expect(result).toContain('2026')
            expect(result).toContain('1')
        })

        it('should handle different date formats', () => {
            const result = formatDate('2025-12-25')
            expect(result).toContain('2025')
            expect(result).toContain('12')
        })
    })

    describe('formatDateTime', () => {
        it('should include time in the formatted string', () => {
            const result = formatDateTime('2026-01-01T14:30:00Z')
            expect(result).toContain('2026')
        })
    })

    describe('formatRelativeTime', () => {
        beforeEach(() => {
            vi.useFakeTimers()
            vi.setSystemTime(new Date('2026-01-01T12:00:00Z'))
        })

        afterEach(() => {
            vi.useRealTimers()
        })

        it('should return "방금 전" for times less than 60 seconds ago', () => {
            const result = formatRelativeTime('2026-01-01T11:59:30Z')
            expect(result).toBe('방금 전')
        })

        it('should return minutes ago for times less than 60 minutes', () => {
            const result = formatRelativeTime('2026-01-01T11:30:00Z')
            expect(result).toBe('30분 전')
        })

        it('should return hours ago for times less than 24 hours', () => {
            const result = formatRelativeTime('2026-01-01T09:00:00Z')
            expect(result).toBe('3시간 전')
        })

        it('should return days ago for times less than 7 days', () => {
            const result = formatRelativeTime('2025-12-29T12:00:00Z')
            expect(result).toBe('3일 전')
        })

        it('should return formatted date for times more than 7 days ago', () => {
            const result = formatRelativeTime('2025-12-01T12:00:00Z')
            expect(result).toContain('2025')
        })
    })
})
