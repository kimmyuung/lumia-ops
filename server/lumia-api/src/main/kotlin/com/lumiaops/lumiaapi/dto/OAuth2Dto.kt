package com.lumiaops.lumiaapi.dto

/**
 * Steam 로그인 요청
 */
data class SteamLoginRequest(
    val steamId: String,       // Steam 64비트 ID
    val steamNickname: String  // Steam 프로필 이름
)

/**
 * Kakao 로그인 요청
 */
data class KakaoLoginRequest(
    val kakaoId: Long,          // Kakao 사용자 ID
    val kakaoNickname: String,  // Kakao 닉네임
    val kakaoEmail: String? = null  // Kakao 이메일 (선택)
)

/**
 * Kakao 인가 코드 요청 (백엔드에서 토큰 교환)
 */
data class KakaoCodeRequest(
    val code: String  // Kakao 인가 코드
)

/**
 * OAuth2 로그인 응답
 */
data class OAuth2LoginResponse(
    val token: String,           // JWT 액세스 토큰
    val refreshToken: String,    // JWT 리프레시 토큰
    val userId: Long,
    val nickname: String?,
    val gameNickname: String?,   // 이터널 리턴 닉네임 (설정된 경우)
    val needsGameNickname: Boolean, // 이터널 리턴 닉네임 설정 필요 여부
    val authProvider: String     // STEAM or KAKAO
)

/**
 * 이터널 리턴 닉네임 설정 요청
 */
data class SetupGameNicknameRequest(
    val gameNickname: String  // 이터널 리턴 인게임 닉네임
)
