package com.lumiaops.lumiasocket

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.lumiaops.lumiasocket", "com.lumiaops.lumiacore"])
class LumiaSocketApplication

fun main(args: Array<String>) {
    runApplication<LumiaSocketApplication>(*args)
}
