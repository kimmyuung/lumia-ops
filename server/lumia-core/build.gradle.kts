// 타입을 명시하는 방식(val bootJar: Type by tasks)은
// 플러그인 클래스패스 문제로 에러가 나기 쉬우므로 아래 방식으로 변경합니다.

tasks.getByName("bootJar") {
	enabled = false
}

tasks.getByName("jar") {
	enabled = true
}

dependencies {
	// [JPA]
	api("org.springframework.boot:spring-boot-starter-data-jpa")

	// [Mail]
	api("org.springframework.boot:spring-boot-starter-mail")

	// [Security - BCrypt]
	api("org.springframework.security:spring-security-crypto")

	// [Database]
	runtimeOnly("com.h2database:h2")
	// runtimeOnly("org.postgresql:postgresql")

	// [JWT - jjwt]
	api("io.jsonwebtoken:jjwt-api:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

	// [Test - MockK]
	testImplementation("io.mockk:mockk:1.13.8")
	testImplementation("io.mockk:mockk-jvm:1.13.8")
}