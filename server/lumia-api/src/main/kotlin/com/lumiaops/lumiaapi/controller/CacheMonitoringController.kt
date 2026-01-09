package com.lumiaops.lumiaapi.controller

import org.springframework.cache.CacheManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 캐시 모니터링 및 관리 컨트롤러
 * 캐시 상태 조회, 캐시 삭제 등의 기능 제공
 */
@RestController
@RequestMapping("/actuator/cache")
class CacheMonitoringController(
    private val cacheManager: CacheManager
) {

    /**
     * 모든 캐시 이름 조회
     */
    @GetMapping("/names")
    fun getCacheNames(): ResponseEntity<Collection<String>> {
        return ResponseEntity.ok(cacheManager.cacheNames)
    }

    /**
     * 특정 캐시의 통계 정보 조회 (Redis 구현체에 따라 다를 수 있음)
     */
    @GetMapping("/stats/{cacheName}")
    fun getCacheStats(@PathVariable cacheName: String): ResponseEntity<Map<String, Any>> {
        val cache = cacheManager.getCache(cacheName)
            ?: return ResponseEntity.notFound().build()

        val stats: Map<String, Any> = mapOf(
            "cacheName" to cacheName,
            "nativeCache" to (cache.nativeCache::class.simpleName ?: "Unknown"),
            "exists" to true
        )

        return ResponseEntity.ok(stats)
    }

    /**
     * 특정 캐시 전체 삭제
     */
    @DeleteMapping("/evict/{cacheName}")
    fun evictCache(@PathVariable cacheName: String): ResponseEntity<Map<String, Any>> {
        val cache = cacheManager.getCache(cacheName)
            ?: return ResponseEntity.notFound().build()

        cache.clear()

        return ResponseEntity.ok(
            mapOf(
                "cacheName" to cacheName,
                "evicted" to true,
                "message" to "${cacheName} 캐시가 삭제되었습니다"
            )
        )
    }

    /**
     * 모든 캐시 삭제
     */
    @DeleteMapping("/evict-all")
    fun evictAllCaches(): ResponseEntity<Map<String, Any>> {
        val cacheNames = cacheManager.cacheNames
        cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }

        return ResponseEntity.ok(
            mapOf(
                "evictedCaches" to cacheNames,
                "count" to cacheNames.size,
                "message" to "모든 캐시가 삭제되었습니다"
            )
        )
    }
}
