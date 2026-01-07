package com.lumiaops.lumiacore.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

/**
 * 이터널 리턴 API 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "eternal-return.api")
data class EternalReturnProperties(
    val key: String = "",
    val baseUrl: String = "https://open-api.bser.io"
)

/**
 * 이터널 리턴 API 설정
 */
@Configuration
@EnableConfigurationProperties(EternalReturnProperties::class)
class EternalReturnConfig(
    private val properties: EternalReturnProperties
) {

    @Bean
    fun eternalReturnRestTemplate(): RestTemplate {
        return RestTemplate()
    }

    companion object {
        const val API_KEY_HEADER = "x-api-key"
    }
}
