package com.lumiaops.lumiaapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI (Swagger) 설정
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"
        
        return OpenAPI()
            .info(apiInfo())
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT 토큰을 입력하세요 (Bearer 접두사 없이)")
                    )
            )
    }

    private fun apiInfo(): Info {
        return Info()
            .title("Lumia Ops API")
            .description("""
                |Lumia Ops - e스포츠 팀 운영 플랫폼 API
                |
                |## 인증
                |대부분의 API는 JWT 토큰이 필요합니다.
                |1. `/auth/login`으로 로그인하여 토큰을 받으세요
                |2. 오른쪽 상단의 'Authorize' 버튼을 클릭하세요
                |3. 토큰을 입력하세요 (Bearer 접두사 없이)
            """.trimMargin())
            .version("1.0.0")
            .contact(
                Contact()
                    .name("Lumia Ops Team")
                    .email("support@lumiaops.com")
            )
            .license(
                License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")
            )
    }
}
