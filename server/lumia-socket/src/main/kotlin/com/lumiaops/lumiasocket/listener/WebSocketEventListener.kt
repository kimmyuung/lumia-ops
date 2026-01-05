package com.lumiaops.lumiasocket.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

private val logger = KotlinLogging.logger {}

/**
 * WebSocket 연결/해제 이벤트 리스너
 */
@Component
class WebSocketEventListener {

    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectedEvent) {
        logger.info { "New WebSocket connection established" }
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val username = headerAccessor.sessionAttributes?.get("username") as? String
        val roomId = headerAccessor.sessionAttributes?.get("roomId") as? String

        if (username != null) {
            logger.info { "User disconnected: $username from room: $roomId" }
            // 여기서 퇴장 메시지를 브로드캐스트할 수 있음
        }
    }
}
