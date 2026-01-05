package com.lumiaops.lumiasocket.controller

import com.lumiaops.lumiasocket.dto.StrategyUpdateMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 전략 맵 실시간 동기화 핸들러
 */
@Controller
class StrategyWebSocketController(
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * 전략 맵 변경사항 브로드캐스트
     * /app/strategy.update/{strategyId} → /topic/strategy/{strategyId}
     */
    @MessageMapping("/strategy.update/{strategyId}")
    @SendTo("/topic/strategy/{strategyId}")
    fun updateStrategy(
        @DestinationVariable strategyId: String,
        @Payload message: StrategyUpdateMessage
    ): StrategyUpdateMessage {
        logger.info { "Strategy update in $strategyId by ${message.sender}" }
        return message.copy(
            timestamp = LocalDateTime.now()
        )
    }

    /**
     * 커서 위치 공유 (협업 시 다른 사용자 커서 표시)
     * /app/strategy.cursor/{strategyId} → /topic/strategy/{strategyId}/cursors
     */
    @MessageMapping("/strategy.cursor/{strategyId}")
    @SendTo("/topic/strategy/{strategyId}/cursors")
    fun shareCursor(
        @DestinationVariable strategyId: String,
        @Payload cursor: CursorPosition
    ): CursorPosition {
        return cursor
    }

    /**
     * 전략 편집 세션 참여
     */
    @MessageMapping("/strategy.join/{strategyId}")
    @SendTo("/topic/strategy/{strategyId}")
    fun joinStrategy(
        @DestinationVariable strategyId: String,
        @Payload message: StrategyJoinMessage
    ): StrategyJoinMessage {
        logger.info { "${message.username} joined strategy $strategyId" }
        return message.copy(
            message = "${message.username}님이 편집에 참여했습니다."
        )
    }
}

data class CursorPosition(
    val userId: Long,
    val username: String,
    val x: Double,
    val y: Double
)

data class StrategyJoinMessage(
    val userId: Long,
    val username: String,
    val message: String = ""
)
