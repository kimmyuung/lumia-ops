package com.lumiaops.lumiasocket.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * WebSocket STOMP 설정
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 클라이언트가 구독할 수 있는 prefix
        // /topic: 1:N 브로드캐스트 (채팅방 등)
        // /queue: 1:1 개인 메시지 (알림 등)
        registry.enableSimpleBroker("/topic", "/queue")
        
        // 클라이언트가 메시지를 보낼 때 사용하는 prefix
        registry.setApplicationDestinationPrefixes("/app")
        
        // 특정 사용자에게 메시지 보낼 때 prefix
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // WebSocket 엔드포인트
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS() // SockJS 폴백 지원
    }
}
