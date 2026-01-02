import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { isTokenExpired, getTokenRemainingTime } from '@/utils/token'
import type { AccountStatus } from '@/api/auth'

export interface User {
  id: string
  nickname: string
  email: string
  teamId?: string
  status?: AccountStatus
}

/** 임시 사용자 정보 (닉네임 설정 전) */
export interface TempUser {
  id: number
  email: string
  status: AccountStatus
}

export const useUserStore = defineStore('user', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const tempUser = ref<TempUser | null>(null) // 닉네임 설정 전 임시 정보
  let tokenCheckInterval: ReturnType<typeof setInterval> | null = null

  // 토큰이 유효한지 확인 (존재하고 만료되지 않았는지)
  const isValidToken = computed(() => {
    if (!token.value) return false
    return !isTokenExpired(token.value)
  })

  const isLoggedIn = computed(() => !!user.value && isValidToken.value)
  const hasTeam = computed(() => !!user.value?.teamId)
  const needsNickname = computed(() => !!tempUser.value)

  function setUser(newUser: User) {
    user.value = newUser
    tempUser.value = null // 닉네임 설정 완료
  }

  function setTempUser(newTempUser: TempUser) {
    tempUser.value = newTempUser
  }

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
    startTokenCheck()
  }

  function logout() {
    user.value = null
    token.value = null
    tempUser.value = null
    localStorage.removeItem('token')
    stopTokenCheck()
  }

  function loadToken() {
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
      // 저장된 토큰이 만료되었는지 확인
      if (isTokenExpired(savedToken)) {
        console.info('[Auth] 저장된 토큰이 만료되어 제거합니다.')
        localStorage.removeItem('token')
        return false
      }
      token.value = savedToken
      startTokenCheck()
      return true
    }
    return false
  }

  /**
   * 토큰 만료를 주기적으로 확인합니다.
   * 만료 5분 전에 경고하고, 만료 시 로그아웃 처리합니다.
   */
  function startTokenCheck() {
    stopTokenCheck()

    tokenCheckInterval = setInterval(() => {
      if (!token.value) {
        stopTokenCheck()
        return
      }

      const remaining = getTokenRemainingTime(token.value)

      // 토큰이 만료된 경우
      if (remaining <= 0) {
        console.warn('[Auth] 토큰이 만료되었습니다.')
        logout()
        // 로그인 페이지로 리다이렉트는 router guard에서 처리
        window.dispatchEvent(new CustomEvent('auth:token-expired'))
        return
      }

      // 5분 이내로 만료되는 경우 경고 이벤트 발생
      if (remaining <= 5 * 60 * 1000) {
        window.dispatchEvent(
          new CustomEvent('auth:token-expiring', {
            detail: { remainingMs: remaining }
          })
        )
      }
    }, 30 * 1000) // 30초마다 확인
  }

  function stopTokenCheck() {
    if (tokenCheckInterval) {
      clearInterval(tokenCheckInterval)
      tokenCheckInterval = null
    }
  }

  return {
    user,
    token,
    tempUser,
    isLoggedIn,
    isValidToken,
    hasTeam,
    needsNickname,
    setUser,
    setTempUser,
    setToken,
    logout,
    loadToken
  }
})
