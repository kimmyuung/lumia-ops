package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.ClientErrorLogRequest
import com.lumiaops.lumiaapi.dto.ClientErrorLogResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

/**
 * 클라이언트(프론트엔드) 에러 로깅 컨트롤러
 * 프론트엔드에서 발생한 에러를 서버에 기록
 */
@Tag(name = "로깅", description = "클라이언트 에러 로깅 API")
@RestController
@RequestMapping("/api/logs")
class ErrorLogController {

    @Operation(
        summary = "클라이언트 에러 로깅",
        description = "프론트엔드에서 발생한 에러를 서버에 기록합니다. 프로덕션 환경에서만 사용합니다."
    )
    @PostMapping("/client-error")
    fun logClientError(
        @RequestBody errorLog: ClientErrorLogRequest,
        request: HttpServletRequest
    ): ResponseEntity<ClientErrorLogResponse> {
        
        // 클라이언트 IP 추출
        val clientIp = request.getHeader("X-Forwarded-For") 
            ?: request.getHeader("X-Real-IP")
            ?: request.remoteAddr

        // 구조화된 로깅
        log.error {
            """
            [CLIENT ERROR] ${errorLog.message}
            URL: ${errorLog.url}
            User-Agent: ${errorLog.userAgent}
            Client IP: $clientIp
            Timestamp: ${errorLog.timestamp}
            Stack: ${errorLog.stack}
            Context: ${errorLog.context}
            """.trimIndent()
        }

        // 심각한 에러 패턴 감지 (선택사항)
        if (isCriticalError(errorLog.message)) {
            log.error { "🚨 [CRITICAL CLIENT ERROR] ${errorLog.message}" }
            // 여기서 Slack/Discord 알림을 보낼 수 있음
            // discordWebhookService.send("🚨 심각한 프론트엔드 에러 발생: ${errorLog.message}")
        }

        return ResponseEntity.ok(ClientErrorLogResponse(success = true))
    }

    /**
     * 심각한 에러인지 판단
     */
    private fun isCriticalError(message: String): Boolean {
        val criticalPatterns = listOf(
            "Cannot read property",
            "undefined is not",
            "null is not",
            "Maximum call stack",
            "Out of memory"
        )
        return criticalPatterns.any { message.contains(it, ignoreCase = true) }
    }

    @Operation(
        summary = "클라이언트 에러 일괄 로깅",
        description = "여러 에러를 한 번에 기록합니다. (배치 처리)"
    )
    @PostMapping("/client-errors")
    fun logClientErrors(
        @RequestBody errors: List<ClientErrorLogRequest>,
        request: HttpServletRequest
    ): ResponseEntity<ClientErrorLogResponse> {
        
        val clientIp = request.getHeader("X-Forwarded-For") 
            ?: request.getHeader("X-Real-IP")
            ?: request.remoteAddr

        errors.forEach { errorLog ->
            log.error {
                """
                [CLIENT ERROR BATCH] ${errorLog.message}
                URL: ${errorLog.url}
                Client IP: $clientIp
                """.trimIndent()
            }
        }

        log.info { "📦 [CLIENT ERROR BATCH] ${errors.size}개의 에러 로그 기록됨" }

        return ResponseEntity.ok(
            ClientErrorLogResponse(
                success = true,
                message = "${errors.size}개의 에러 로그가 기록되었습니다"
            )
        )
    }
}
