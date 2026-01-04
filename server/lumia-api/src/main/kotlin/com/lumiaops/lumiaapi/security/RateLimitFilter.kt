package com.lumiaops.lumiaapi.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * IP 기반 Rate Limiting 필터
 * - 로그인 API: 분당 5회 제한
 * - 기타 API: 분당 100회 제한
 */
@Component
class RateLimitFilter : OncePerRequestFilter() {

    // IP별 요청 횟수 추적
    private val requestCounts = ConcurrentHashMap<String, RateLimitBucket>()
    
    // 설정값
    private val loginRateLimit = 5
    private val defaultRateLimit = 100
    private val windowMs = 60_000L // 1분

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val clientIp = getClientIp(request)
        val path = request.requestURI
        val isLoginPath = path.contains("/auth/login") || path.contains("/auth/register")
        
        val limit = if (isLoginPath) loginRateLimit else defaultRateLimit
        val bucketKey = if (isLoginPath) "$clientIp:login" else "$clientIp:default"
        
        val bucket = requestCounts.computeIfAbsent(bucketKey) { 
            RateLimitBucket(AtomicInteger(0), System.currentTimeMillis()) 
        }
        
        synchronized(bucket) {
            val now = System.currentTimeMillis()
            
            // 시간 창 초과 시 리셋
            if (now - bucket.windowStart > windowMs) {
                bucket.count.set(0)
                bucket.windowStart = now
            }
            
            if (bucket.count.incrementAndGet() > limit) {
                response.status = HttpStatus.TOO_MANY_REQUESTS.value()
                response.contentType = "application/json"
                response.writer.write("""{"status":429,"error":"Too Many Requests","message":"요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.","code":"RATE_LIMIT_EXCEEDED"}""")
                return
            }
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (xForwardedFor.isNullOrBlank()) {
            request.remoteAddr
        } else {
            xForwardedFor.split(",")[0].trim()
        }
    }
    
    private data class RateLimitBucket(
        val count: AtomicInteger,
        var windowStart: Long
    )
}
