package com.lumiaops.lumiacore.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * 보안 관련 Bean 설정
 */
@Configuration
class SecurityBeanConfig {

    /**
     * 비밀번호 인코더 Bean
     * BCrypt 해시 알고리즘 사용
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
