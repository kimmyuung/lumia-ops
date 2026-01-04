package com.lumiaops.lumiaapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * CORS 설정 프로퍼티
 * application.properties의 app.cors.* 값을 바인딩
 */
@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(
    /**
     * 허용할 Origin 목록
     * 예: ["http://localhost:5173", "https://lumia-ops.com"]
     */
    val allowedOrigins: List<String> = listOf("http://localhost:5173"),

    /**
     * 허용할 HTTP 메서드 목록
     */
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"),

    /**
     * 허용할 헤더 목록
     */
    val allowedHeaders: List<String> = listOf("*"),

    /**
     * 인증 정보(쿠키, Authorization 헤더 등) 허용 여부
     */
    val allowCredentials: Boolean = true,

    /**
     * Preflight 요청 캐시 시간 (초)
     * 기본값: 1시간 (3600초)
     */
    val maxAge: Long = 3600L
)
