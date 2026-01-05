package com.lumiaops.lumiasocket.security

import com.lumiaops.lumiacore.security.JwtTokenProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

/**
 * WebSocket STOMP 메시지에서 JWT 토큰을 검증하는 인터셉터
 * 
 * CONNECT 프레임에서 Authorization 헤더를 추출하여 JWT 토큰을 검증하고,
 * 유효한 경우 사용자 정보를 세션에 저장합니다.
 */
@Component
class JwtChannelInterceptor(
    private val jwtTokenProvider: JwtTokenProvider
) : ChannelInterceptor {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        // CONNECT 프레임에서만 인증 처리
        if (accessor.command == StompCommand.CONNECT) {
            val authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER)
            
            if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
                val token = authHeader.substring(BEARER_PREFIX.length)
                
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        val userId = jwtTokenProvider.getUserIdFromToken(token)
                        val email = jwtTokenProvider.getEmailFromToken(token)
                        
                        // 인증 객체 생성
                        val authentication = UsernamePasswordAuthenticationToken(
                            WebSocketPrincipal(userId, email),
                            null,
                            listOf(SimpleGrantedAuthority("ROLE_USER"))
                        )
                        
                        // 세션에 인증 정보 저장
                        accessor.user = authentication
                        
                        log.info { "[WebSocket] 사용자 인증 성공: userId=$userId, email=$email" }
                    } else {
                        log.warn { "[WebSocket] 유효하지 않은 JWT 토큰" }
                        // 토큰이 유효하지 않으면 연결 거부하지 않고 익명으로 처리
                        // 필요시 여기서 예외를 던져 연결을 거부할 수 있음
                    }
                } catch (e: Exception) {
                    log.error(e) { "[WebSocket] JWT 토큰 검증 중 오류 발생" }
                }
            } else {
                log.debug { "[WebSocket] Authorization 헤더 없음 (익명 연결)" }
            }
        }
        
        return message
    }
}

/**
 * WebSocket 세션에서 사용되는 Principal 객체
 */
data class WebSocketPrincipal(
    val userId: Long,
    val email: String
) : java.security.Principal {
    override fun getName(): String = userId.toString()
}
