import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface User {
    id: string
    nickname: string
    email: string
    teamId?: string
}

export const useUserStore = defineStore('user', () => {
    const user = ref<User | null>(null)
    const token = ref<string | null>(null)

    const isLoggedIn = computed(() => !!user.value)
    const hasTeam = computed(() => !!user.value?.teamId)

    function setUser(newUser: User) {
        user.value = newUser
    }

    function setToken(newToken: string) {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    function logout() {
        user.value = null
        token.value = null
        localStorage.removeItem('token')
    }

    function loadToken() {
        const savedToken = localStorage.getItem('token')
        if (savedToken) {
            token.value = savedToken
        }
    }

    return {
        user,
        token,
        isLoggedIn,
        hasTeam,
        setUser,
        setToken,
        logout,
        loadToken
    }
})
