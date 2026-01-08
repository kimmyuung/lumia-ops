package com.lumiaops.lumiacore.domain

/**
 * 인증 제공자 (로그인 방식)
 */
enum class AuthProvider {
    EMAIL,   // 기존 이메일 로그인 (레거시)
    STEAM,   // Steam OpenID
    KAKAO    // Kakao OAuth2
}
