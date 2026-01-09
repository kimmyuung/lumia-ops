import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.plugins.JavaPluginExtension

plugins {
	id("org.springframework.boot") version "3.4.1" apply false
	id("io.spring.dependency-management") version "1.1.4" apply false
	kotlin("jvm") version "2.0.0" apply false
	// Spring의 @Configuration, @Transactional 등이 붙은 클래스를 자동으로 open 처리 (상속 가능하게)
	kotlin("plugin.spring") version "2.0.0" apply false
	// JPA Entity에 기본 생성자(No-arg)를 자동으로 만들어줌 (Hibernate 필수)
	kotlin("plugin.jpa") version "2.0.0" apply false
}

allprojects {
	group = "com.lumia-ops"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java-library")
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")

	configure<JavaPluginExtension> {
		toolchain {
			languageVersion = JavaLanguageVersion.of(21)
		}
	}

	dependencies {
		// [Kotlin 필수]
		add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin") // JSON 처리 시 data class 지원
		add("implementation", "org.jetbrains.kotlin:kotlin-reflect") // 런타임 리플렉션
		add("implementation", "org.jetbrains.kotlin:kotlin-stdlib")

		// [Test]
		add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
		add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5")
		add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")

		// [Logging]
		add("implementation", "io.github.oshai:kotlin-logging-jvm:7.0.3")

		// [Test - MockK]
		add("testImplementation", "io.mockk:mockk:1.13.8")
	}

	tasks.withType<KotlinCompile>().configureEach {
		compilerOptions {
			freeCompilerArgs.add("-Xjsr305=strict") // Spring의 Null-Safety 어노테이션을 엄격하게 준수
			jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}