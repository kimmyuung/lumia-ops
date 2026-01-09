package com.lumiaops.lumiacore.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

/**
 * 공통 Bean 설정
 */
@Configuration
class AppConfig {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}
