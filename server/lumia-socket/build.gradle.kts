dependencies {
    // Core 모듈 가져오기
    implementation(project(":lumia-core"))

    // [WebSocket]
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    
    // [JSON]
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // [Logging]
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
}