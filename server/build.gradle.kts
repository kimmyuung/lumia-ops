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
		finalizedBy("jacocoTestReport") // 테스트 후 리포트 자동 생성
	}
	
	// JaCoCo 설정
	apply(plugin = "jacoco")
	
	configure<JacocoPluginExtension> {
		toolVersion = "0.8.11"
	}
	
	tasks.named<JacocoReport>("jacocoTestReport") {
		dependsOn(tasks.named("test"))
		
		reports {
			xml.required.set(true)  // Codecov 업로드용
			html.required.set(true) // 로컬 확인용
			csv.required.set(false)
		}
		
		classDirectories.setFrom(
			files(classDirectories.files.map {
				fileTree(it) {
					exclude(
						"**/*Application*",
						"**/*Config*",
						"**/*Dto*",
						"**/*Request*",
						"**/*Response*",
						"**/domain/**"  // Entity는 커버리지에서 제외
					)
				}
			})
		)
	}
	
	tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
		dependsOn(tasks.named("test"))
		
		violationRules {
			rule {
				limit {
					minimum = "0.70".toBigDecimal()  // 70% 최소 커버리지
				}
			}
			
			rule {
				element = "CLASS"
				limit {
					counter = "LINE"
					value = "COVEREDRATIO"
					minimum = "0.60".toBigDecimal()  // 클래스별 60%
				}
			}
		}
	}
}
