package com.lumiaops.lumiacore.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * 스케줄링 설정
 * @Scheduled 어노테이션 활성화
 */
@Configuration
@EnableScheduling
class SchedulingConfig
