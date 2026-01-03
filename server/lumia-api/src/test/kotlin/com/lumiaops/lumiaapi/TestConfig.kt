package com.lumiaops.lumiaapi

import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.mail.javamail.JavaMailSender
import com.lumiaops.lumiacore.service.EmailService

@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    fun javaMailSender(): JavaMailSender {
        return mockk(relaxed = true)
    }

    @Bean
    @Primary
    fun emailService(): EmailService {
        return mockk(relaxed = true)
    }
}
