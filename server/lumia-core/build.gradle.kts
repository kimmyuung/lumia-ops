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

	// [Database]
	runtimeOnly("com.h2database:h2")
	// runtimeOnly("org.postgresql:postgresql")
}