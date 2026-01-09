dependencies {
    // Core 모듈 가져오기
    implementation(project(":lumia-core"))

    // [Web]
    implementation("org.springframework.boot:spring-boot-starter-web")

    // [Security]
    implementation("org.springframework.boot:spring-boot-starter-security")

    // [Validation] DTO 검증 (@NotNull, @Size 등)
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Mail
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // [API Documentation] SpringDoc OpenAPI (Swagger UI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // [Actuator] 헬스체크, 메트릭, 정보 엔드포인트
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // [Rate Limiting] simple token bucket
    // Built with Kotlin, no external library needed

    // Test
    testImplementation("org.springframework.security:spring-security-test")
}