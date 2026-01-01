dependencies {
    // Core 모듈 가져오기
    implementation(project(":lumia-core"))

    // [Web]
    implementation("org.springframework.boot:spring-boot-starter-web")

    // [Validation] DTO 검증 (@NotNull, @Size 등)
    implementation("org.springframework.boot:spring-boot-starter-validation")
}