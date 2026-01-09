package com.lumiaops.lumiaapi.dto

import java.time.LocalDateTime

/**
 * 클라이언트(프론트엔드) 에러 로그 요청
 */
data class ClientErrorLogRequest(
    val message: String,
    val stack: String?,
    val url: String,
    val userAgent: String,
    val timestamp: String? = null,
    val context: Map<String, Any>? = null
)

/**
 * 클라이언트 에러 로그 응답
 */
data class ClientErrorLogResponse(
    val success: Boolean,
    val message: String = "에러 로그가 기록되었습니다"
)
