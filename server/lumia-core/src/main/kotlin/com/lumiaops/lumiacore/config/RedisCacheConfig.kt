package com.lumiaops.lumiacore.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Redis 캐시 설정
 */
@Configuration
@EnableCaching
class RedisCacheConfig {

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer())
            )
            .entryTtl(Duration.ofMinutes(30)) // 기본 TTL: 30분

        // 캐시별 TTL 설정
        val cacheConfigs = mapOf(
            // 통계 - 10분
            "teamStatistics" to defaultConfig.entryTtl(Duration.ofMinutes(10)),
            "leaderboard" to defaultConfig.entryTtl(Duration.ofMinutes(10)),
            
            // 플레이어 전적 - 5분 (외부 API)
            "playerStats" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "playerGames" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            
            // 메타 분석 - 1시간
            "metaAnalysis" to defaultConfig.entryTtl(Duration.ofHours(1))
        )

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build()
    }
}
