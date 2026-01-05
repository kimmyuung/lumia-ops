package com.lumiaops.lumiasocket.controller

import com.lumiaops.lumiasocket.dto.ChatMessage
import com.lumiaops.lumiasocket.dto.MessageType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

/**
 * 채팅 메시지 핸들러
 */
@Controller
class ChatController {

    /**
     * 채팅 메시지 전송
     * /app/chat.send/{roomId} → /topic/room/{roomId}
     */
    @MessageMapping("/chat.send/{roomId}")
    @SendTo("/topic/room/{roomId}")
    fun sendMessage(
        @DestinationVariable roomId: String,
        @Payload message: ChatMessage
    ): ChatMessage {
        logger.info { "Chat message in room $roomId from ${message.sender}: ${message.content}" }
        return message.copy(
            roomId = roomId,
            timestamp = LocalDateTime.now()
        )
    }

    /**
     * 채팅방 입장
     * /app/chat.join/{roomId} → /topic/room/{roomId}
     */
    @MessageMapping("/chat.join/{roomId}")
    @SendTo("/topic/room/{roomId}")
    fun joinRoom(
        @DestinationVariable roomId: String,
        @Payload message: ChatMessage,
        headerAccessor: SimpMessageHeaderAccessor
    ): ChatMessage {
        // 세션에 사용자 정보 저장
        headerAccessor.sessionAttributes?.put("username", message.sender)
        headerAccessor.sessionAttributes?.put("roomId", roomId)
        
        logger.info { "${message.sender} joined room $roomId" }
        
        return ChatMessage(
            type = MessageType.JOIN,
            roomId = roomId,
            sender = "System",
            content = "${message.sender}님이 입장하셨습니다.",
            timestamp = LocalDateTime.now()
        )
    }

    /**
     * 채팅방 퇴장
     * /app/chat.leave/{roomId} → /topic/room/{roomId}
     */
    @MessageMapping("/chat.leave/{roomId}")
    @SendTo("/topic/room/{roomId}")
    fun leaveRoom(
        @DestinationVariable roomId: String,
        @Payload message: ChatMessage
    ): ChatMessage {
        logger.info { "${message.sender} left room $roomId" }
        
        return ChatMessage(
            type = MessageType.LEAVE,
            roomId = roomId,
            sender = "System",
            content = "${message.sender}님이 퇴장하셨습니다.",
            timestamp = LocalDateTime.now()
        )
    }
}
