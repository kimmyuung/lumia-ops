package com.lumiaops.lumiaapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Rate Limiting 설정 속성
 */
@ConfigurationProperties(prefix = "rate-limit")
data class RateLimitProperties(
    /** Rate Limiting 활성화 여부 */
    val enabled: Boolean = true,
    /** 허용 요청 수 */
    val requests: Long = 100,
    /** 시간 윈도우 (초) */
    val durationSeconds: Long = 60
)
