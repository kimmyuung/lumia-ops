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
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger {}

/**
 * IP별 Token Bucket 구현 (만료 시간 지원)
 */
class TokenBucket(
    private val capacity: Long,
    private val refillRatePerSecond: Double
) {
    private val tokens = AtomicLong(capacity)
    private val lastRefillTime = AtomicLong(System.nanoTime())
    private val lastAccessTime = AtomicLong(System.currentTimeMillis())

    /**
     * 마지막 접근 시간
     */
    fun getLastAccessTime(): Long = lastAccessTime.get()

    /**
     * 토큰 소비 시도
     * @return Pair(성공 여부, 남은 토큰 수 또는 대기 시간(나노초))
     */
    fun tryConsume(): TokenConsumeResult {
        lastAccessTime.set(System.currentTimeMillis())
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
 * 
 * 기능:
 * - IP 기반 요청 제한
 * - 화이트리스트 IP 지원
 * - 민감 엔드포인트별 차등 제한 (로그인, 회원가입 등)
 * - 주기적 메모리 정리
 */
@Component
@EnableConfigurationProperties(RateLimitProperties::class)
class RateLimitFilter(
    private val rateLimitProperties: RateLimitProperties,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    // IP별 Bucket 저장소 (인메모리)
    private val buckets = ConcurrentHashMap<String, TokenBucket>()
    
    // 민감 엔드포인트용 별도 Bucket (IP + 엔드포인트)
    private val sensitiveBuckets = ConcurrentHashMap<String, TokenBucket>()
    
    // 민감 엔드포인트 패턴
    private val sensitiveEndpoints = mapOf(
        "/auth/login" to { props: RateLimitProperties -> props.sensitiveEndpoints.loginRequestsPerMinute },
        "/auth/register" to { props: RateLimitProperties -> props.sensitiveEndpoints.registerRequestsPerMinute },
        "/password/forgot" to { props: RateLimitProperties -> props.sensitiveEndpoints.passwordResetRequestsPerMinute },
        "/password/reset" to { props: RateLimitProperties -> props.sensitiveEndpoints.passwordResetRequestsPerMinute }
    )

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
        
        // 화이트리스트 IP는 Rate Limiting 적용 제외
        if (isWhitelisted(clientIp)) {
            filterChain.doFilter(request, response)
            return
        }
        
        // 민감 엔드포인트 체크
        val requestPath = request.requestURI
        val sensitiveResult = checkSensitiveEndpoint(clientIp, requestPath, request, response)
        if (sensitiveResult != null) {
            if (!sensitiveResult) return // 제한 초과로 응답 완료
        }

        // 일반 Rate Limiting
        val bucket = buckets.computeIfAbsent(clientIp) { createDefaultBucket() }
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
     * 화이트리스트 IP 확인
     */
    private fun isWhitelisted(ip: String): Boolean {
        return rateLimitProperties.whitelistIps.contains(ip)
    }
    
    /**
     * 민감 엔드포인트 Rate Limiting 체크
     * @return null: 민감 엔드포인트 아님, true: 통과, false: 제한 초과
     */
    private fun checkSensitiveEndpoint(
        clientIp: String,
        requestPath: String,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): Boolean? {
        if (!rateLimitProperties.sensitiveEndpoints.enabled) return null
        
        val limitFn = sensitiveEndpoints.entries.find { requestPath.endsWith(it.key) }?.value
            ?: return null
        
        val limit = limitFn(rateLimitProperties)
        val bucketKey = "$clientIp:$requestPath"
        
        val bucket = sensitiveBuckets.computeIfAbsent(bucketKey) {
            TokenBucket(capacity = limit, refillRatePerSecond = limit.toDouble() / 60.0)
        }
        
        val result = bucket.tryConsume()
        
        return if (result.isConsumed) {
            response.addHeader("X-Rate-Limit-Endpoint", requestPath)
            response.addHeader("X-Rate-Limit-Remaining", result.remainingTokens.toString())
            true
        } else {
            logger.warn { "Sensitive endpoint rate limit exceeded: IP=$clientIp, path=$requestPath" }
            val waitTimeSeconds = result.nanosToWait / 1_000_000_000
            sendErrorResponse(request, response, waitTimeSeconds, "민감 엔드포인트 요청 한도를 초과했습니다.")
            false
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
     * 기본 TokenBucket 생성
     */
    private fun createDefaultBucket(): TokenBucket {
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
        retryAfterSeconds: Long,
        customMessage: String? = null
    ) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val message = customMessage ?: "요청 한도를 초과했습니다. ${retryAfterSeconds}초 후에 다시 시도해주세요."
        
        val errorResponse = mapOf(
            "status" to 429,
            "error" to "Too Many Requests",
            "code" to "RATE_LIMIT_EXCEEDED",
            "message" to message,
            "path" to request.requestURI
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
    
    /**
     * 만료된 Bucket 정리 (1시간마다 실행)
     */
    @Scheduled(fixedRate = 3600000) // 1시간
    fun cleanupExpiredBuckets() {
        val now = System.currentTimeMillis()
        val expiryMs = rateLimitProperties.bucketExpirySeconds * 1000
        
        var removedCount = 0
        
        // 일반 Bucket 정리
        buckets.entries.removeIf { entry ->
            val expired = (now - entry.value.getLastAccessTime()) > expiryMs
            if (expired) removedCount++
            expired
        }
        
        // 민감 엔드포인트 Bucket 정리
        sensitiveBuckets.entries.removeIf { entry ->
            val expired = (now - entry.value.getLastAccessTime()) > expiryMs
            if (expired) removedCount++
            expired
        }
        
        if (removedCount > 0) {
            logger.info { "Cleaned up $removedCount expired rate limit buckets" }
        }
    }
}
