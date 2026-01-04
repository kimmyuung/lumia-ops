import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { scrimApi } from '../scrim'
import apiClient from '../client'

vi.mock('../client', () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        patch: vi.fn(),
        delete: vi.fn()
    }
}))

describe('scrimApi', () => {
    const mockGet = vi.mocked(apiClient.get)
    const mockPost = vi.mocked(apiClient.post)
    const mockPatch = vi.mocked(apiClient.patch)
    const mockDelete = vi.mocked(apiClient.delete)

    beforeEach(() => {
        vi.clearAllMocks()
    })

    afterEach(() => {
        vi.restoreAllMocks()
    })

    describe('getScrims', () => {
        it('should call GET /scrims without teamId', async () => {
            const mockScrims = [{ id: '1', title: 'Scrim1' }]
            mockGet.mockResolvedValue({ data: mockScrims })

            const result = await scrimApi.getScrims()

            expect(mockGet).toHaveBeenCalledWith('/scrims', { params: {} })
            expect(result).toEqual(mockScrims)
        })

        it('should call GET /scrims with teamId', async () => {
            const mockScrims = [{ id: '1', title: 'Scrim1' }]
            mockGet.mockResolvedValue({ data: mockScrims })

            const result = await scrimApi.getScrims('team1')

            expect(mockGet).toHaveBeenCalledWith('/scrims', { params: { teamId: 'team1' } })
            expect(result).toEqual(mockScrims)
        })
    })

    describe('getScrim', () => {
        it('should call GET /scrims/:id', async () => {
            const mockScrim = { id: '1', title: 'Scrim1' }
            mockGet.mockResolvedValue({ data: mockScrim })

            const result = await scrimApi.getScrim('1')

            expect(mockGet).toHaveBeenCalledWith('/scrims/1')
            expect(result).toEqual(mockScrim)
        })
    })

    describe('createScrim', () => {
        it('should call POST /scrims', async () => {
            const createData = {
                title: 'New Scrim',
                scheduledAt: '2026-01-05T10:00:00Z',
                teamId: 'team1'
            }
            const mockScrim = { id: '1', ...createData }
            mockPost.mockResolvedValue({ data: mockScrim })

            const result = await scrimApi.createScrim(createData)

            expect(mockPost).toHaveBeenCalledWith('/scrims', createData)
            expect(result).toEqual(mockScrim)
        })
    })

    describe('updateScrim', () => {
        it('should call PATCH /scrims/:id', async () => {
            const updateData = { title: 'Updated Scrim' }
            const mockScrim = { id: '1', title: 'Updated Scrim' }
            mockPatch.mockResolvedValue({ data: mockScrim })

            const result = await scrimApi.updateScrim('1', updateData)

            expect(mockPatch).toHaveBeenCalledWith('/scrims/1', updateData)
            expect(result).toEqual(mockScrim)
        })
    })

    describe('deleteScrim', () => {
        it('should call DELETE /scrims/:id', async () => {
            mockDelete.mockResolvedValue({})

            await scrimApi.deleteScrim('1')

            expect(mockDelete).toHaveBeenCalledWith('/scrims/1')
        })
    })

    describe('updateStatus', () => {
        it('should call PATCH /scrims/:id/status', async () => {
            const mockScrim = { id: '1', status: 'COMPLETED' }
            mockPatch.mockResolvedValue({ data: mockScrim })

            const result = await scrimApi.updateStatus('1', 'COMPLETED')

            expect(mockPatch).toHaveBeenCalledWith('/scrims/1/status', { status: 'COMPLETED' })
            expect(result).toEqual(mockScrim)
        })
    })

    describe('addResult', () => {
        it('should call POST /scrims/:scrimId/results', async () => {
            const resultData = { round: 1, placement: 1, kills: 10 }
            const mockResult = { id: 'r1', ...resultData }
            mockPost.mockResolvedValue({ data: mockResult })

            const result = await scrimApi.addResult('1', resultData)

            expect(mockPost).toHaveBeenCalledWith('/scrims/1/results', resultData)
            expect(result).toEqual(mockResult)
        })
    })

    describe('updateResult', () => {
        it('should call PATCH /scrims/:scrimId/results/:resultId', async () => {
            const updateData = { kills: 15 }
            const mockResult = { id: 'r1', kills: 15 }
            mockPatch.mockResolvedValue({ data: mockResult })

            const result = await scrimApi.updateResult('1', 'r1', updateData)

            expect(mockPatch).toHaveBeenCalledWith('/scrims/1/results/r1', updateData)
            expect(result).toEqual(mockResult)
        })
    })

    describe('deleteResult', () => {
        it('should call DELETE /scrims/:scrimId/results/:resultId', async () => {
            mockDelete.mockResolvedValue({})

            await scrimApi.deleteResult('1', 'r1')

            expect(mockDelete).toHaveBeenCalledWith('/scrims/1/results/r1')
        })
    })
})
