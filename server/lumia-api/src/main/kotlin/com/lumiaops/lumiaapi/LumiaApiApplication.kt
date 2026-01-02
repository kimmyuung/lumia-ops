package com.lumiaops.lumiaapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@ComponentScan(basePackages = ["com.lumiaops"])
@EntityScan(basePackages = ["com.lumiaops"])
@EnableJpaRepositories(basePackages = ["com.lumiaops"])
class LumiaApiApplication

fun main(args: Array<String>) {
    runApplication<LumiaApiApplication>(*args)
}
