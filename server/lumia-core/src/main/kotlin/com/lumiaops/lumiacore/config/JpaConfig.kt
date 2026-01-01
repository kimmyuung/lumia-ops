package com.myeongho.lumia.core.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing // 이 어노테이션이 있어야 날짜가 자동으로 찍힙니다.
class JpaConfig