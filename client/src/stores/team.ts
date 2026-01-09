import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { teamApi, type UpdateTeamRequest, type InviteMemberRequest } from '@/api/team'
import type { Team, CreateTeamRequest } from '@/types/team'
import { useUserStore } from './user'
import { useToast } from '@/composables/useToast'
import { getErrorMessage } from '@/utils/error'

export const useTeamStore = defineStore('team', () => {
  const userStore = useUserStore()
  const toast = useToast()

  // 상태
  const teams = ref<Team[]>([])
  const currentTeam = ref<Team | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // 게터
  const hasTeam = computed(() => currentTeam.value !== null)

  const isOwner = computed(() => {
    if (!currentTeam.value || !userStore.user) return false
    const member = currentTeam.value.members.find(m => m.userId === userStore.user?.id)
    return member?.role === 'OWNER'
  })

  const isAdmin = computed(() => {
    if (!currentTeam.value || !userStore.user) return false
    const member = currentTeam.value.members.find(m => m.userId === userStore.user?.id)
    return member?.role === 'OWNER' || member?.role === 'ADMIN'
  })

  const memberCount = computed(() => currentTeam.value?.members.length ?? 0)

  // 액션
  async function fetchTeams() {
    isLoading.value = true
    error.value = null
    try {
      teams.value = await teamApi.getTeams()
    } catch (err) {
      error.value = getErrorMessage(err, '팀 목록을 불러오지 못했습니다.')
    } finally {
      isLoading.value = false
    }
  }

  async function fetchMyTeam() {
    isLoading.value = true
    error.value = null
    try {
      currentTeam.value = await teamApi.getMyTeam()
    } catch (err) {
      error.value = getErrorMessage(err, '팀 정보를 불러오지 못했습니다.')
    } finally {
      isLoading.value = false
    }
  }

  async function fetchTeam(id: string) {
    isLoading.value = true
    error.value = null
    try {
      currentTeam.value = await teamApi.getTeam(id)
    } catch (err) {
      error.value = getErrorMessage(err, '팀 정보를 불러오지 못했습니다.')
    } finally {
      isLoading.value = false
    }
  }

  async function createTeam(data: CreateTeamRequest) {
    isLoading.value = true
    error.value = null
    try {
      const newTeam = await teamApi.createTeam(data)
      teams.value.push(newTeam)
      currentTeam.value = newTeam
      toast.success('팀이 생성되었습니다.')
      return newTeam
    } catch (err) {
      error.value = getErrorMessage(err, '팀 생성에 실패했습니다.')
      toast.error(error.value)
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function updateTeam(id: string, data: UpdateTeamRequest) {
    isLoading.value = true
    error.value = null
    try {
      const updatedTeam = await teamApi.updateTeam(id, data)
      const index = teams.value.findIndex(t => t.id === id)
      if (index !== -1) {
        teams.value[index] = updatedTeam
      }
      if (currentTeam.value?.id === id) {
        currentTeam.value = updatedTeam
      }
      toast.success('팀 정보가 수정되었습니다.')
      return updatedTeam
    } catch (err) {
      error.value = getErrorMessage(err, '팀 수정에 실패했습니다.')
      toast.error(error.value)
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function deleteTeam(id: string) {
    isLoading.value = true
    error.value = null
    try {
      await teamApi.deleteTeam(id)
      teams.value = teams.value.filter(t => t.id !== id)
      if (currentTeam.value?.id === id) {
        currentTeam.value = null
      }
      toast.success('팀이 삭제되었습니다.')
      return true
    } catch (err) {
      error.value = getErrorMessage(err, '팀 삭제에 실패했습니다.')
      toast.error(error.value)
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function inviteMember(data: InviteMemberRequest) {
    if (!currentTeam.value) return null
    isLoading.value = true
    error.value = null
    try {
      const member = await teamApi.inviteMember(currentTeam.value.id, data)
      currentTeam.value.members.push(member)
      toast.success('멤버를 초대했습니다.')
      return member
    } catch (err) {
      error.value = getErrorMessage(err, '멤버 초대에 실패했습니다.')
      toast.error(error.value)
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function removeMember(memberId: string) {
    if (!currentTeam.value) return false
    isLoading.value = true
    error.value = null
    try {
      await teamApi.removeMember(currentTeam.value.id, memberId)
      currentTeam.value.members = currentTeam.value.members.filter(m => m.id !== memberId)
      toast.success('멤버를 제거했습니다.')
      return true
    } catch (err) {
      error.value = getErrorMessage(err, '멤버 제거에 실패했습니다.')
      toast.error(error.value)
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function leaveTeam() {
    if (!currentTeam.value) return false
    isLoading.value = true
    error.value = null
    try {
      await teamApi.leaveTeam(currentTeam.value.id)
      currentTeam.value = null
      toast.info('팀에서 탈퇴했습니다.')
      return true
    } catch (err) {
      error.value = getErrorMessage(err, '팀 탈퇴에 실패했습니다.')
      toast.error(error.value)
      return false
    } finally {
      isLoading.value = false
    }
  }

  function clearError() {
    error.value = null
  }

  return {
    // 상태
    teams,
    currentTeam,
    isLoading,
    error,
    // 게터
    hasTeam,
    isOwner,
    isAdmin,
    memberCount,
    // 액션
    fetchTeams,
    fetchMyTeam,
    fetchTeam,
    createTeam,
    updateTeam,
    deleteTeam,
    inviteMember,
    removeMember,
    leaveTeam,
    clearError
  }
})
