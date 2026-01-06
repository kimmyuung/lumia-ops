package com.lumiaops.lumiaapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.security.JwtAuthenticationFilter
import com.lumiaops.lumiaapi.security.RateLimitFilter
import com.lumiaops.lumiacore.config.JwtProperties
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

/**
 * Spring Security 설정
 * JWT 기반 무상태(Stateless) 인증 구성
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties::class, CorsProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val rateLimitFilter: RateLimitFilter,
    private val objectMapper: ObjectMapper,
    private val corsProperties: CorsProperties
) {


    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CORS 설정
            .cors { it.configurationSource(corsConfigurationSource()) }
            // CSRF 비활성화 (JWT 사용 시 불필요)
            .csrf { it.disable() }
            // 세션 미사용 (Stateless)
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // 예외 처리 - 인증 실패 시 401 반환
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint(authenticationEntryPoint())
            }
            // 요청별 인증 규칙
            .authorizeHttpRequests { auth ->
                auth
                    // 인증 없이 접근 가능한 경로
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/password/**").permitAll()
                    // 초대 토큰 검증만 허용 (수락/거절은 인증 필요)
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/invitations/{token}").permitAll()
                    // H2 콘솔 (개발용)
                    .requestMatchers("/h2-console/**").permitAll()
                    // Swagger UI
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    // Actuator 엔드포인트 (health, info)
                    .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                    // 나머지는 인증 필요
                    .anyRequest().authenticated()
            }
            // H2 콘솔을 위한 frameOptions 설정 (개발용)
            .headers { headers ->
                headers.frameOptions { it.sameOrigin() }
            }
            // Rate Limit 필터 추가 (가장 먼저 실행)
            .addFilterBefore(rateLimitFilter, CorsFilter::class.java)
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    /**
     * 인증 실패 시 401 Unauthorized 응답 반환
     */
    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint { request: HttpServletRequest, response: HttpServletResponse, _ ->
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"
            
            val errorResponse = mapOf(
                "status" to 401,
                "error" to "Unauthorized",
                "code" to "AUTHENTICATION_REQUIRED",
                "message" to "인증이 필요합니다",
                "path" to request.requestURI
            )
            
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = corsProperties.allowedOrigins
            allowedMethods = corsProperties.allowedMethods
            allowedHeaders = corsProperties.allowedHeaders
            allowCredentials = corsProperties.allowCredentials
            maxAge = corsProperties.maxAge
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
