package com.lumiaops.lumiasocket.controller

import com.lumiaops.lumiasocket.dto.ChatMessage
import com.lumiaops.lumiasocket.dto.MessageType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("ChatController 단위 테스트")
class ChatControllerTest {

    private lateinit var chatController: ChatController

    @BeforeEach
    fun setUp() {
        chatController = ChatController()
    }

    @Nested
    @DisplayName("sendMessage 테스트")
    inner class SendMessageTest {

        @Test
        @DisplayName("메시지 전송 시 roomId와 timestamp가 설정되어야 함")
        fun `should set roomId and timestamp when sending message`() {
            // given
            val roomId = "room-123"
            val message = ChatMessage(
                type = MessageType.CHAT,
                roomId = "",
                sender = "user1",
                content = "Hello!"
            )

            // when
            val result = chatController.sendMessage(roomId, message)

            // then
            assertEquals(roomId, result.roomId)
            assertEquals("user1", result.sender)
            assertEquals("Hello!", result.content)
            assertNotNull(result.timestamp)
        }

        @Test
        @DisplayName("메시지 타입이 유지되어야 함")
        fun `should preserve message type`() {
            // given
            val message = ChatMessage(
                type = MessageType.CHAT,
                roomId = "room-1",
                sender = "user1",
                content = "Test"
            )

            // when
            val result = chatController.sendMessage("room-1", message)

            // then
            assertEquals(MessageType.CHAT, result.type)
        }
    }

    @Nested
    @DisplayName("joinRoom 테스트")
    inner class JoinRoomTest {

        @Test
        @DisplayName("입장 시 시스템 메시지가 생성되어야 함")
        fun `should create system message when joining room`() {
            // given
            val roomId = "room-456"
            val message = ChatMessage(
                type = MessageType.JOIN,
                roomId = roomId,
                sender = "newUser",
                content = ""
            )
            val headerAccessor = SimpMessageHeaderAccessor.create()
            headerAccessor.sessionAttributes = mutableMapOf<String, Any>()

            // when
            val result = chatController.joinRoom(roomId, message, headerAccessor)

            // then
            assertEquals(MessageType.JOIN, result.type)
            assertEquals("System", result.sender)
            assertEquals(roomId, result.roomId)
            assert(result.content.contains("newUser"))
            assert(result.content.contains("입장"))
        }

        @Test
        @DisplayName("세션에 사용자 정보가 저장되어야 함")
        fun `should store user info in session`() {
            // given
            val roomId = "room-789"
            val message = ChatMessage(
                type = MessageType.JOIN,
                roomId = roomId,
                sender = "testUser",
                content = ""
            )
            val sessionAttributes = mutableMapOf<String, Any>()
            val headerAccessor = SimpMessageHeaderAccessor.create()
            headerAccessor.sessionAttributes = sessionAttributes

            // when
            chatController.joinRoom(roomId, message, headerAccessor)

            // then
            assertEquals("testUser", sessionAttributes["username"])
            assertEquals(roomId, sessionAttributes["roomId"])
        }
    }

    @Nested
    @DisplayName("leaveRoom 테스트")
    inner class LeaveRoomTest {

        @Test
        @DisplayName("퇴장 시 시스템 메시지가 생성되어야 함")
        fun `should create system message when leaving room`() {
            // given
            val roomId = "room-123"
            val message = ChatMessage(
                type = MessageType.LEAVE,
                roomId = roomId,
                sender = "leavingUser",
                content = ""
            )

            // when
            val result = chatController.leaveRoom(roomId, message)

            // then
            assertEquals(MessageType.LEAVE, result.type)
            assertEquals("System", result.sender)
            assertEquals(roomId, result.roomId)
            assert(result.content.contains("leavingUser"))
            assert(result.content.contains("퇴장"))
        }
    }
}
