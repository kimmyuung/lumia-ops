package com.lumiaops.lumiaapi.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.env.Environment

/**
 * CORS 설정 프로퍼티
 * application.properties의 app.cors.* 값을 바인딩
 * 
 * 보안 권장사항:
 * - 프로덕션 환경에서는 명시적인 도메인만 허용
 * - 와일드카드(*) 사용 지양
 */
@ConfigurationProperties(prefix = "app.cors")
class CorsProperties {
    private val log = LoggerFactory.getLogger(javaClass)
    
    @Autowired
    private lateinit var environment: Environment

    /**
     * 허용할 Origin 목록
     * 예: ["http://localhost:5173", "https://lumia-ops.com"]
     */
    var allowedOrigins: List<String> = listOf("http://localhost:5173")

    /**
     * 허용할 HTTP 메서드 목록
     */
    var allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")

    /**
     * 허용할 헤더 목록
     */
    var allowedHeaders: List<String> = listOf("*")

    /**
     * 인증 정보(쿠키, Authorization 헤더 등) 허용 여부
     */
    var allowCredentials: Boolean = true

    /**
     * Preflight 요청 캐시 시간 (초)
     * 기본값: 1시간 (3600초)
     */
    var maxAge: Long = 3600L

    @PostConstruct
    fun validateConfiguration() {
        val activeProfiles = environment.activeProfiles.toList()
        val isProduction = activeProfiles.any { it.equals("prod", ignoreCase = true) }

        // 와일드카드 사용 경고
        if (allowedOrigins.any { it == "*" }) {
            if (isProduction) {
                log.error("🚨 CORS 보안 경고: 프로덕션 환경에서 allowedOrigins에 와일드카드(*)를 사용하면 안됩니다!")
            } else {
                log.warn("⚠️ CORS 경고: allowedOrigins에 와일드카드(*)가 설정되어 있습니다. 개발 환경에서만 사용하세요.")
            }
        }

        // 프로덕션 환경 검증
        if (isProduction) {
            // localhost 허용 경고
            if (allowedOrigins.any { it.contains("localhost") }) {
                log.warn("⚠️ CORS 경고: 프로덕션 환경에서 localhost가 허용되어 있습니다. 보안을 확인하세요.")
            }

            // HTTPS 미사용 경고
            if (allowedOrigins.any { it.startsWith("http://") && !it.contains("localhost") }) {
                log.warn("⚠️ CORS 경고: 프로덕션 환경에서 HTTP(비암호화) Origin이 허용되어 있습니다.")
            }
        }

        log.info("CORS 설정 로드 완료: allowedOrigins=$allowedOrigins")
    }

    /**
     * 특정 Origin이 허용되는지 확인
     */
    fun isOriginAllowed(origin: String): Boolean {
        return allowedOrigins.any { it == "*" || it.equals(origin, ignoreCase = true) }
    }
}

