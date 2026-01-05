package com.lumiaops.lumiaapi.security

import com.lumiaops.lumiacore.security.JwtTokenProvider
import com.lumiaops.lumiacore.service.TokenService
import com.lumiaops.lumiacore.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 인증 필터
 * 모든 요청에서 Authorization 헤더를 검사하여 JWT 토큰 검증
 * 블랙리스트에 있는 토큰은 거부
 */
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userService: UserService,
    private val tokenService: TokenService
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)

            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 블랙리스트 확인
                if (tokenService.isBlacklisted(token)) {
                    log.debug("블랙리스트에 있는 토큰 거부")
                    filterChain.doFilter(request, response)
                    return
                }
                
                if (jwtTokenProvider.isAccessToken(token)) {
                    val userId = jwtTokenProvider.getUserIdFromToken(token)
                    val user = userService.findById(userId)

                    if (user != null) {
                        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                        val authentication = UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

                        SecurityContextHolder.getContext().authentication = authentication
                        log.debug("인증 성공: userId=$userId, email=${user.email}")
                    }
                }
            }
        } catch (e: Exception) {
            log.error("인증 처리 중 오류 발생: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     * Authorization: Bearer <token> 형식
     */
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION_HEADER)
        return if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            bearerToken.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }
}
