import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  statisticsApi,
  type TeamStatsResponse,
  type RecentPerformanceResponse,
  type LeaderboardEntry
} from '@/api/statistics'
import {
  playerStatsApi,
  type PlayerStatsResponse,
  type CharacterStatsResponse,
  type GameRecordResponse
} from '@/api/playerStats'
import { getErrorMessage } from '@/utils/error'

export const useStatisticsStore = defineStore('statistics', () => {
  // 상태
  const teamStats = ref<TeamStatsResponse | null>(null)
  const recentPerformance = ref<RecentPerformanceResponse | null>(null)
  const leaderboard = ref<LeaderboardEntry[]>([])
  const playerStats = ref<PlayerStatsResponse | null>(null)
  const playerCharacters = ref<CharacterStatsResponse[]>([])
  const playerGames = ref<GameRecordResponse[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const apiConfigured = ref<boolean | null>(null)

  // 게터
  const hasTeamStats = computed(() => teamStats.value !== null)
  const hasPlayerStats = computed(() => playerStats.value !== null)
  const leaderboardTop3 = computed(() => leaderboard.value.slice(0, 3))

  // 액션 - 팀 통계
  async function fetchTeamStats(teamId: number) {
    isLoading.value = true
    error.value = null
    try {
      teamStats.value = await statisticsApi.getTeamStats(teamId)
      return teamStats.value
    } catch (err) {
      error.value = getErrorMessage(err, '팀 통계를 불러오지 못했습니다.')
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function fetchRecentPerformance(teamId: number, count = 10) {
    isLoading.value = true
    error.value = null
    try {
      recentPerformance.value = await statisticsApi.getRecentPerformance(teamId, count)
      return recentPerformance.value
    } catch (err) {
      error.value = getErrorMessage(err, '최근 성적을 불러오지 못했습니다.')
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function fetchLeaderboard() {
    isLoading.value = true
    error.value = null
    try {
      const response = await statisticsApi.getLeaderboard()
      leaderboard.value = response.teams
      return leaderboard.value
    } catch (err) {
      error.value = getErrorMessage(err, '리더보드를 불러오지 못했습니다.')
      return []
    } finally {
      isLoading.value = false
    }
  }

  // 액션 - 플레이어 통계
  async function fetchPlayerStats(nickname: string, seasonId?: number, teamMode?: number) {
    isLoading.value = true
    error.value = null
    try {
      playerStats.value = await playerStatsApi.getPlayerStats(nickname, seasonId, teamMode)
      return playerStats.value
    } catch (err) {
      error.value = getErrorMessage(err, '플레이어 통계를 불러오지 못했습니다.')
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function fetchPlayerCharacters(nickname: string, limit = 5, seasonId?: number) {
    isLoading.value = true
    error.value = null
    try {
      playerCharacters.value = await playerStatsApi.getTopCharacters(nickname, limit, seasonId)
      return playerCharacters.value
    } catch (err) {
      error.value = getErrorMessage(err, '실험체 정보를 불러오지 못했습니다.')
      return []
    } finally {
      isLoading.value = false
    }
  }

  async function fetchPlayerGames(nickname: string, limit = 10) {
    isLoading.value = true
    error.value = null
    try {
      playerGames.value = await playerStatsApi.getRecentGames(nickname, limit)
      return playerGames.value
    } catch (err) {
      error.value = getErrorMessage(err, '게임 기록을 불러오지 못했습니다.')
      return []
    } finally {
      isLoading.value = false
    }
  }

  async function checkApiStatus() {
    try {
      const status = await playerStatsApi.getApiStatus()
      apiConfigured.value = status.configured
      return status.configured
    } catch {
      apiConfigured.value = false
      return false
    }
  }

  function clearError() {
    error.value = null
  }

  function clearPlayerStats() {
    playerStats.value = null
    playerCharacters.value = []
    playerGames.value = []
  }

  return {
    // 상태
    teamStats,
    recentPerformance,
    leaderboard,
    playerStats,
    playerCharacters,
    playerGames,
    isLoading,
    error,
    apiConfigured,
    // 게터
    hasTeamStats,
    hasPlayerStats,
    leaderboardTop3,
    // 액션
    fetchTeamStats,
    fetchRecentPerformance,
    fetchLeaderboard,
    fetchPlayerStats,
    fetchPlayerCharacters,
    fetchPlayerGames,
    checkApiStatus,
    clearError,
    clearPlayerStats
  }
})
