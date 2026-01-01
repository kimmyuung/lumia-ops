import apiClient from './client'

// 인증 관련 타입
export interface LoginRequest {
    email: string
    password: string
}

export interface RegisterRequest {
    nickname: string
    email: string
    password: string
}

export interface AuthResponse {
    token: string
    user: {
        id: string
        nickname: string
        email: string
        teamId?: string
    }
}

export interface User {
    id: string
    nickname: string
    email: string
    teamId?: string
}

// 인증 API
export const authApi = {
    /**
     * 로그인
     */
    async login(data: LoginRequest): Promise<AuthResponse> {
        const response = await apiClient.post<AuthResponse>('/auth/login', data)
        return response.data
    },

    /**
     * 회원가입
     */
    async register(data: RegisterRequest): Promise<AuthResponse> {
        const response = await apiClient.post<AuthResponse>('/auth/register', data)
        return response.data
    },

    /**
     * 로그아웃
     */
    async logout(): Promise<void> {
        await apiClient.post('/auth/logout')
    },

    /**
     * 현재 사용자 정보 조회
     */
    async me(): Promise<User> {
        const response = await apiClient.get<User>('/auth/me')
        return response.data
    },

    /**
     * 토큰 갱신
     */
    async refresh(): Promise<{ token: string }> {
        const response = await apiClient.post<{ token: string }>('/auth/refresh')
        return response.data
    }
}

export default authApi
