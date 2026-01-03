package com.lumiaops.lumiaapi.dto

import java.time.LocalDateTime

/**
 * 표준 에러 응답 DTO
 */
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val code: String,
    val message: String,
    val path: String? = null,
    val field: String? = null,
    val details: List<FieldError>? = null
) {
    companion object {
        fun of(status: Int, error: String, code: String, message: String, path: String? = null) = ErrorResponse(
            status = status,
            error = error,
            code = code,
            message = message,
            path = path
        )
    }
}

/**
 * 필드 에러 (유효성 검증 실패 시)
 */
data class FieldError(
    val field: String,
    val message: String,
    val rejectedValue: Any? = null
)
