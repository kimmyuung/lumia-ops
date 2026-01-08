import apiClient from './client'

// ==================== 타입 정의 ====================

/** 회원가입 요청 */
export interface RegisterRequest {
  email: string
  password: string
}

/** 로그인 요청 */
export interface LoginRequest {
  email: string
  password: string
}

/** 로그인 응답 */
export interface LoginResponse {
  token: string
  refreshToken: string
  userId: number
  email: string
  nickname: string | null
  status: AccountStatus
  needsNickname: boolean
  message?: string
}

/** 토큰 갱신 응답 */
export interface TokenResponse {
  token: string
  refreshToken: string
}

/** 계정 상태 */
export type AccountStatus =
  | 'PENDING_EMAIL' // 이메일 인증 대기
  | 'PENDING_NICKNAME' // 닉네임 설정 대기
  | 'ACTIVE' // 정상 활성
  | 'LOCKED' // 로그인 5회 실패로 잠김
  | 'DORMANT' // 6개월 이상 미로그인 휴면

/** 이메일 인증 요청 */
export interface VerifyEmailRequest {
  token: string
}

/** 아이디 찾기 요청 */
export interface FindUsernameRequest {
  email: string
}

/** 아이디 찾기 응답 */
export interface FindUsernameResponse {
  email: string | null
  exists: boolean
}

/** 인증 이메일 재발송 요청 */
export interface ResendVerificationRequest {
  email: string
}

/** 공통 메시지 응답 */
export interface MessageResponse {
  success: boolean
  message: string
}

/** 사용자 정보 */
export interface User {
  id: number
  email: string
  nickname: string | null
  status: AccountStatus
  daysUntilNicknameChange: number
}

// ==================== API 함수 ====================

export const authApi = {
  /**
   * 회원가입 (이메일/비밀번호만)
   * 가입 후 이메일 인증 필요
   */
  async register(data: RegisterRequest): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>('/auth/register', data)
    return response.data
  },

  /**
   * 로그인
   * 다양한 상태 반환: Success, NeedsNickname, Locked, Dormant
   */
  async login(data: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>('/auth/login', data)
    return response.data
  },

  /**
   * 이메일 인증
   */
  async verifyEmail(token: string): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>('/auth/verify-email', { token })
    return response.data
  },

  /**
   * 인증 이메일 재발송
   */
  async resendVerification(email: string): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>('/auth/resend-verification', { email })
    return response.data
  },

  /**
   * 아이디 찾기 (인증 없이)
   */
  async findUsername(email: string): Promise<FindUsernameResponse> {
    const response = await apiClient.post<FindUsernameResponse>('/auth/find-username', { email })
    return response.data
  },

  /**
   * 로그아웃
   * 서버에 토큰 무효화 요청 후 로컬 스토리지 정리
   */
  async logout(): Promise<void> {
    try {
      const refreshToken = localStorage.getItem('refreshToken')
      await apiClient.post('/auth/logout', { refreshToken })
    } catch {
      // 서버 요청 실패해도 로컬 정리는 진행
      console.warn('[Auth] Logout API 호출 실패, 로컬 토큰만 삭제')
    } finally {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    }
  },

  /**
   * 토큰 갱신
   * Refresh Token을 사용하여 새 Access Token 발급
   */
  async refreshToken(refreshToken: string): Promise<TokenResponse> {
    const response = await apiClient.post<TokenResponse>('/auth/refresh', { refreshToken })
    return response.data
  }
}

// ==================== OAuth2 타입 정의 ====================

/** OAuth2 로그인 응답 */
export interface OAuth2LoginResponse {
  token: string
  refreshToken: string
  userId: number
  nickname: string | null
  gameNickname: string | null
  needsGameNickname: boolean
  authProvider: 'STEAM' | 'KAKAO'
}

/** Steam 로그인 요청 */
export interface SteamLoginRequest {
  steamId: string
  steamNickname: string
}

/** Kakao 로그인 요청 */
export interface KakaoLoginRequest {
  kakaoId: number
  kakaoNickname: string
  kakaoEmail?: string
}

/** 게임 닉네임 설정 요청 */
export interface SetupGameNicknameRequest {
  gameNickname: string
}

// ==================== OAuth2 API 함수 ====================

export const oauth2Api = {
  /**
   * Steam 로그인 콜백
   */
  async steamCallback(data: SteamLoginRequest): Promise<OAuth2LoginResponse> {
    const response = await apiClient.post<OAuth2LoginResponse>('/auth/oauth2/steam/callback', data)
    return response.data
  },

  /**
   * Kakao 로그인 콜백
   */
  async kakaoCallback(data: KakaoLoginRequest): Promise<OAuth2LoginResponse> {
    const response = await apiClient.post<OAuth2LoginResponse>('/auth/oauth2/kakao/callback', data)
    return response.data
  },

  /**
   * Kakao 인가 코드 콜백 (백엔드에서 토큰 교환)
   */
  async kakaoCodeCallback(code: string): Promise<OAuth2LoginResponse> {
    const response = await apiClient.post<OAuth2LoginResponse>('/auth/oauth2/kakao/code', { code })
    return response.data
  },

  /**
   * 이터널 리턴 닉네임 설정
   */
  async setupGameNickname(gameNickname: string): Promise<MessageResponse> {
    const response = await apiClient.post<MessageResponse>('/auth/oauth2/setup-game-nickname', {
      gameNickname
    })
    return response.data
  },

  /**
   * OAuth 사용자 여부 확인
   */
  async getOAuthStatus(): Promise<{ isOAuthUser: boolean }> {
    const response = await apiClient.get<{ isOAuthUser: boolean }>('/auth/oauth2/status')
    return response.data
  },

  /**
   * Steam 로그인 URL 생성
   */
  getSteamLoginUrl(): string {
    const steamOpenIdUrl = 'https://steamcommunity.com/openid/login'
    const returnUrl = `${window.location.origin}/auth/oauth2/steam/callback`

    const params = new URLSearchParams({
      'openid.ns': 'http://specs.openid.net/auth/2.0',
      'openid.mode': 'checkid_setup',
      'openid.return_to': returnUrl,
      'openid.realm': window.location.origin,
      'openid.identity': 'http://specs.openid.net/auth/2.0/identifier_select',
      'openid.claimed_id': 'http://specs.openid.net/auth/2.0/identifier_select'
    })

    return `${steamOpenIdUrl}?${params.toString()}`
  },

  /**
   * Kakao 로그인 URL 생성
   */
  getKakaoLoginUrl(clientId: string): string {
    const kakaoAuthUrl = 'https://kauth.kakao.com/oauth/authorize'
    const redirectUri = `${window.location.origin}/auth/oauth2/kakao/callback`

    const params = new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri,
      response_type: 'code'
    })

    return `${kakaoAuthUrl}?${params.toString()}`
  }
}

export default authApi

