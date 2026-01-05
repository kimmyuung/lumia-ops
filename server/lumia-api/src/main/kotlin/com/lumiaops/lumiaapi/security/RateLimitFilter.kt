package com.lumiaops.lumiaapi.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.config.RateLimitProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger {}

/**
 * IP별 Token Bucket 구현
 */
class TokenBucket(
    private val capacity: Long,
    private val refillRatePerSecond: Double
) {
    private val tokens = AtomicLong(capacity)
    private val lastRefillTime = AtomicLong(System.nanoTime())

    /**
     * 토큰 소비 시도
     * @return Pair(성공 여부, 남은 토큰 수 또는 대기 시간(나노초))
     */
    fun tryConsume(): TokenConsumeResult {
        refill()
        val currentTokens = tokens.get()
        return if (currentTokens > 0) {
            val newTokens = tokens.decrementAndGet()
            TokenConsumeResult(true, newTokens.coerceAtLeast(0), 0)
        } else {
            // 토큰이 없으면, 다음 토큰까지 대기 시간 계산
            val nanosPerToken = (1_000_000_000.0 / refillRatePerSecond).toLong()
            TokenConsumeResult(false, 0, nanosPerToken)
        }
    }

    private fun refill() {
        val now = System.nanoTime()
        val last = lastRefillTime.get()
        val elapsed = now - last
        val tokensToAdd = (elapsed * refillRatePerSecond / 1_000_000_000.0).toLong()
        
        if (tokensToAdd > 0) {
            val newTokens = (tokens.get() + tokensToAdd).coerceAtMost(capacity)
            tokens.set(newTokens)
            lastRefillTime.set(now)
        }
    }
}

data class TokenConsumeResult(
    val isConsumed: Boolean,
    val remainingTokens: Long,
    val nanosToWait: Long
)

/**
 * IP 기반 Rate Limiting 필터
 * Token Bucket 알고리즘 사용 (순수 Kotlin 구현)
 */
@Component
@EnableConfigurationProperties(RateLimitProperties::class)
class RateLimitFilter(
    private val rateLimitProperties: RateLimitProperties,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    // IP별 Bucket 저장소 (인메모리)
    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Rate limiting이 비활성화된 경우 바로 통과
        if (!rateLimitProperties.enabled) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = getClientIp(request)
        val bucket = buckets.computeIfAbsent(clientIp) { createNewBucket() }
        val result = bucket.tryConsume()

        if (result.isConsumed) {
            // 남은 요청 수를 헤더에 추가
            response.addHeader("X-Rate-Limit-Remaining", result.remainingTokens.toString())
            response.addHeader("X-Rate-Limit-Limit", rateLimitProperties.requests.toString())
            filterChain.doFilter(request, response)
        } else {
            // 요청 제한 초과
            logger.warn { "Rate limit exceeded for IP: $clientIp" }
            
            val waitTimeSeconds = result.nanosToWait / 1_000_000_000
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", waitTimeSeconds.toString())
            response.addHeader("X-Rate-Limit-Limit", rateLimitProperties.requests.toString())
            response.addHeader("X-Rate-Limit-Remaining", "0")
            
            sendErrorResponse(request, response, waitTimeSeconds)
        }
    }

    /**
     * 클라이언트 IP 추출 (프록시 환경 고려)
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (!xForwardedFor.isNullOrBlank()) {
            // 프록시를 거친 경우 첫 번째 IP가 원본 클라이언트 IP
            xForwardedFor.split(",").first().trim()
        } else {
            request.remoteAddr
        }
    }

    /**
     * 새 TokenBucket 생성
     */
    private fun createNewBucket(): TokenBucket {
        val refillRate = rateLimitProperties.requests.toDouble() / rateLimitProperties.durationSeconds
        return TokenBucket(
            capacity = rateLimitProperties.requests,
            refillRatePerSecond = refillRate
        )
    }

    /**
     * 429 Too Many Requests 응답 전송
     */
    private fun sendErrorResponse(
        request: HttpServletRequest,
        response: HttpServletResponse,
        retryAfterSeconds: Long
    ) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = mapOf(
            "status" to 429,
            "error" to "Too Many Requests",
            "code" to "RATE_LIMIT_EXCEEDED",
            "message" to "요청 한도를 초과했습니다. ${retryAfterSeconds}초 후에 다시 시도해주세요.",
            "path" to request.requestURI
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
