package com.lumiaops.lumiacore.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * Redis 캐싱 설정
 * Spring Cache 추상화 + Redis를 사용한 분산 캐싱
 */
@Configuration
@EnableCaching
class RedisConfig {

    /**
     * Redis 연결 팩토리
     */
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration().apply {
            hostName = System.getenv("REDIS_HOST") ?: "localhost"
            port = System.getenv("REDIS_PORT")?.toInt() ?: 6379
            password = System.getenv("REDIS_PASSWORD")?.let {
                org.springframework.data.redis.connection.RedisPassword.of(it)
            } ?: org.springframework.data.redis.connection.RedisPassword.none()
        }
        return LettuceConnectionFactory(config)
    }

    /**
     * RedisTemplate 설정
     * 직접 Redis 작업이 필요할 때 사용
     */
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            this.connectionFactory = connectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = GenericJackson2JsonRedisSerializer()
        }
    }

    /**
     * 캐시 매니저 설정
     * @Cacheable, @CacheEvict 등의 어노테이션 사용 시 적용됨
     */
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        // 기본 캐시 설정 (TTL 1시간)
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )
            .disableCachingNullValues()  // null 값은 캐싱하지 않음

        // 캐시별 TTL 설정
        val cacheConfigurations = mapOf(
            "team" to defaultConfig.entryTtl(Duration.ofHours(1)),
            "team:members" to defaultConfig.entryTtl(Duration.ofHours(1)),
            "user" to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "user:profile" to defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "stats:team" to defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "stats:scrim" to defaultConfig.entryTtl(Duration.ofMinutes(5))
        )

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}
