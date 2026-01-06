package com.lumiaops.lumiacore.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * JWT 설정 프로퍼티
 * application.properties의 app.jwt.* 값을 바인딩
 */
@ConfigurationProperties(prefix = "app.jwt")
data class JwtProperties(
    /**
     * JWT 서명에 사용할 비밀키 (Base64 인코딩)
     * 최소 256비트(32바이트) 이상 권장
     */
    val secret: String = "bHVtaWEtb3BzLWRlZmF1bHQtc2VjcmV0LWtleS1mb3ItZGV2ZWxvcG1lbnQtb25seQ==",

    /**
     * 액세스 토큰 만료 시간 (밀리초)
     * 기본값: 1시간 (3600000ms)
     */
    val expirationMs: Long = 3600000,

    /**
     * 리프레시 토큰 만료 시간 (밀리초)
     * 기본값: 7일 (604800000ms)
     */
    val refreshExpirationMs: Long = 604800000,

    /**
     * 최대 동시 세션 수
     * 기본값: 3 (PC, 모바일, 태블릿 등 다중 디바이스 고려)
     */
    val maxSessions: Int = 3
)
