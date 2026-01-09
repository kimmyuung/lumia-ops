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
    val durationSeconds: Long = 60,
    /** Bucket 만료 시간 (초) - 마지막 요청 후 이 시간이 지나면 Bucket 제거 */
    val bucketExpirySeconds: Long = 3600,
    /** 화이트리스트 IP (Rate Limiting 적용 제외) */
    val whitelistIps: List<String> = listOf("127.0.0.1", "0:0:0:0:0:0:0:1"),
    /** 민감 엔드포인트 설정 (로그인, 회원가입 등) */
    val sensitiveEndpoints: SensitiveEndpointConfig = SensitiveEndpointConfig()
)

/**
 * 민감 엔드포인트별 제한 설정
 */
data class SensitiveEndpointConfig(
    /** 활성화 여부 */
    val enabled: Boolean = true,
    /** 로그인 시도 제한 (분당) */
    val loginRequestsPerMinute: Long = 10,
    /** 회원가입 시도 제한 (분당) */
    val registerRequestsPerMinute: Long = 5,
    /** 비밀번호 찾기 제한 (분당) */
    val passwordResetRequestsPerMinute: Long = 3
)
