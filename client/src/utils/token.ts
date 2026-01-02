/**
 * JWT 토큰 관련 유틸리티
 */

export interface JwtPayload {
    sub: string // subject (user id)
    exp: number // expiration time (Unix timestamp)
    iat: number // issued at (Unix timestamp)
    [key: string]: unknown
}

/**
 * JWT 토큰을 디코딩합니다.
 * 서명 검증은 하지 않습니다 (서버에서 처리).
 */
export function decodeJwt(token: string): JwtPayload | null {
    try {
        const parts = token.split('.')
        if (parts.length !== 3) return null

        const payload = parts[1]
        if (!payload) return null

        // Base64Url -> Base64 변환
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        )

        return JSON.parse(jsonPayload) as JwtPayload
    } catch {
        return null
    }
}

/**
 * 토큰이 만료되었는지 확인합니다.
 * @param token JWT 토큰
 * @param bufferSeconds 만료 전 버퍼 시간 (기본 60초)
 */
export function isTokenExpired(token: string, bufferSeconds = 60): boolean {
    const payload = decodeJwt(token)
    if (!payload || !payload.exp) return true

    const expirationTime = payload.exp * 1000 // Unix timestamp -> milliseconds
    const now = Date.now()
    const bufferMs = bufferSeconds * 1000

    return now >= expirationTime - bufferMs
}

/**
 * 토큰의 남은 유효 시간을 반환합니다 (밀리초).
 * 만료된 경우 0을 반환합니다.
 */
export function getTokenRemainingTime(token: string): number {
    const payload = decodeJwt(token)
    if (!payload || !payload.exp) return 0

    const expirationTime = payload.exp * 1000
    const remaining = expirationTime - Date.now()

    return Math.max(0, remaining)
}

/**
 * 토큰에서 사용자 ID를 추출합니다.
 */
export function getTokenUserId(token: string): string | null {
    const payload = decodeJwt(token)
    return payload?.sub || null
}
