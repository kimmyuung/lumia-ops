import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "2.0.0"
	// Spring의 @Configuration, @Transactional 등이 붙은 클래스를 자동으로 open 처리 (상속 가능하게)
	kotlin("plugin.spring") version "2.0.0"
	// JPA Entity에 기본 생성자(No-arg)를 자동으로 만들어줌 (Hibernate 필수)
	kotlin("plugin.jpa") version "2.0.0"
}

allprojects {
	group = "com.myeongho"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

// 루트 프로젝트는 애플리케이션이 아니므로 bootJar/jar 비활성화
tasks.getByName("bootJar") {
	enabled = false
}

tasks.getByName("jar") {
	enabled = false
}

subprojects {
	apply(plugin = "java-library")
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	java {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}

	dependencies {
		// [Kotlin 필수]
		implementation("com.fasterxml.jackson.module:jackson-module-kotlin") // JSON 처리 시 data class 지원
		implementation("org.jetbrains.kotlin:kotlin-reflect") // 런타임 리플렉션
		implementation("org.jetbrains.kotlin:kotlin-stdlib")

		// [Test]
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
		testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs += "-Xjsr305=strict" // Spring의 Null-Safety 어노테이션을 엄격하게 준수
			jvmTarget = "21"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}