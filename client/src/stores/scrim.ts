import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { scrimApi, type UpdateScrimRequest, type AddResultRequest } from '@/api/scrim'
import type { Scrim, ScrimResult, CreateScrimRequest, ScrimStatus } from '@/types/scrim'
import { useToast } from '@/composables/useToast'

export const useScrimStore = defineStore('scrim', () => {
    const toast = useToast()

    // 상태
    const scrims = ref<Scrim[]>([])
    const currentScrim = ref<Scrim | null>(null)
    const isLoading = ref(false)
    const error = ref<string | null>(null)
    const statusFilter = ref<ScrimStatus | 'ALL'>('ALL')

    // 게터
    const filteredScrims = computed(() => {
        if (statusFilter.value === 'ALL') return scrims.value
        return scrims.value.filter(s => s.status === statusFilter.value)
    })

    const scheduledScrims = computed(() =>
        scrims.value.filter(s => s.status === 'SCHEDULED')
    )

    const completedScrims = computed(() =>
        scrims.value.filter(s => s.status === 'COMPLETED')
    )

    const scrimStats = computed(() => {
        const completed = completedScrims.value
        if (completed.length === 0) return { count: 0, avgPlacement: 0, totalKills: 0 }

        let totalPlacement = 0
        let totalKills = 0
        let resultCount = 0

        completed.forEach(scrim => {
            scrim.results.forEach(result => {
                totalPlacement += result.placement
                totalKills += result.kills
                resultCount++
            })
        })

        return {
            count: completed.length,
            avgPlacement: resultCount > 0 ? (totalPlacement / resultCount).toFixed(1) : '-',
            totalKills
        }
    })

    // 액션
    async function fetchScrims(teamId?: string) {
        isLoading.value = true
        error.value = null
        try {
            scrims.value = await scrimApi.getScrims(teamId)
        } catch (err: unknown) {
            const e = err as { message?: string }
            error.value = e.message || '스크림 목록을 불러오지 못했습니다.'
        } finally {
            isLoading.value = false
        }
    }

    async function fetchScrim(id: string) {
        isLoading.value = true
        error.value = null
        try {
            currentScrim.value = await scrimApi.getScrim(id)
            return currentScrim.value
        } catch (err: unknown) {
            const e = err as { message?: string }
            error.value = e.message || '스크림 정보를 불러오지 못했습니다.'
            return null
        } finally {
            isLoading.value = false
        }
    }

    async function createScrim(data: CreateScrimRequest) {
        isLoading.value = true
        error.value = null
        try {
            const newScrim = await scrimApi.createScrim(data)
            scrims.value.unshift(newScrim)
            toast.success('스크림이 생성되었습니다.')
            return newScrim
        } catch (err: unknown) {
            const e = err as { message?: string }
            error.value = e.message || '스크림 생성에 실패했습니다.'
            toast.error(error.value)
            return null
        } finally {
            isLoading.value = false
        }
    }

    async function updateScrim(id: string, data: UpdateScrimRequest) {
        isLoading.value = true
        error.value = null
        try {
            const updatedScrim = await scrimApi.updateScrim(id, data)
            const index = scrims.value.findIndex(s => s.id === id)
            if (index !== -1) {
                scrims.value[index] = updatedScrim
            }
            if (currentScrim.value?.id === id) {
                currentScrim.value = updatedScrim
            }
            toast.success('스크림이 수정되었습니다.')
            return updatedScrim
        } catch (err: unknown) {
            const e = err as { message?: string }
            error.value = e.message || '스크림 수정에 실패했습니다.'
            toast.error(error.value)
            return null
        } finally {
            isLoading.value = false
        }
    }

    async function deleteScrim(id: string) {
        isLoading.value = true
        error.value = null
        try {
            await scrimApi.deleteScrim(id)
            scrims.value = scrims.value.filter(s => s.id !== id)
            if (currentScrim.value?.id === id) {
                currentScrim.value = null
            }
            toast.success('스크림이 삭제되었습니다.')
            return true
        } catch (err: unknown) {
            const e = err as { message?: string }
            error.value = e.message || '스크림 삭제에 실패했습니다.'
            toast.error(error.value)
            return false
        } finally {
            isLoading.value = false
        }
    }

    async function updateStatus(id: string, status: ScrimStatus) {
        isLoading.value = true
        error.value = null
        try {
            const updatedScrim = await scrimApi.updateStatus(id, status)
            const index = scrims.value.findIndex(s => s.id === id)
            if (index !== -1) {
                scrims.value[index] = updatedScrim
            }
            if (currentScrim.value?.id === id) {
                currentScrim.value = updatedScrim
            }
            toast.success('상태가 변경되었습니다.')
            return updatedScrim
        } catch (err: unknown) {
            const e = err as { message?: string }
            error.value = e.message || '상태 변경에 실패했습니다.'
            toast.error(error.value)
            return null
        } finally {
            isLoading.value = false
        }
    }

    async function addResult(scrimId: string, data: AddResultRequest) {
        isLoading.value = true
        error.value = null
        try {
            const result = await scrimApi.addResult(scrimId, data)
            const scrim = scrims.value.find(s => s.id === scrimId)
            if (scrim) {
                scrim.results.push(result)
            }
            if (currentScrim.value?.id === scrimId) {
                currentScrim.value.results.push(result)
            }
            toast.success('결과가 추가되었습니다.')
            return result
        } catch (err: unknown) {
            const e = err as { message?: string }
            error.value = e.message || '결과 추가에 실패했습니다.'
            toast.error(error.value)
            return null
        } finally {
            isLoading.value = false
        }
    }

    async function deleteResult(scrimId: string, resultId: string) {
        isLoading.value = true
        error.value = null
        try {
            await scrimApi.deleteResult(scrimId, resultId)
            const scrim = scrims.value.find(s => s.id === scrimId)
            if (scrim) {
                scrim.results = scrim.results.filter(r => r.id !== resultId)
            }
            if (currentScrim.value?.id === scrimId) {
                currentScrim.value.results = currentScrim.value.results.filter(r => r.id !== resultId)
            }
            toast.success('결과가 삭제되었습니다.')
            return true
        } catch (err: unknown) {
            const e = err as { message?: string }
            error.value = e.message || '결과 삭제에 실패했습니다.'
            toast.error(error.value)
            return false
        } finally {
            isLoading.value = false
        }
    }

    function setStatusFilter(status: ScrimStatus | 'ALL') {
        statusFilter.value = status
    }

    function clearError() {
        error.value = null
    }

    return {
        // 상태
        scrims,
        currentScrim,
        isLoading,
        error,
        statusFilter,
        // 게터
        filteredScrims,
        scheduledScrims,
        completedScrims,
        scrimStats,
        // 액션
        fetchScrims,
        fetchScrim,
        createScrim,
        updateScrim,
        deleteScrim,
        updateStatus,
        addResult,
        deleteResult,
        setStatusFilter,
        clearError
    }
})
