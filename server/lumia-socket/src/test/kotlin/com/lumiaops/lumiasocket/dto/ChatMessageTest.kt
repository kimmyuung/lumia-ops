package com.lumiaops.lumiasocket.dto

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("ChatMessage DTO 단위 테스트")
class ChatMessageTest {

    @Nested
    @DisplayName("ChatMessage 생성 테스트")
    inner class ChatMessageCreationTest {

        @Test
        @DisplayName("ChatMessage 객체 생성 성공")
        fun `should create ChatMessage with all fields`() {
            // given
            val timestamp = LocalDateTime.now()

            // when
            val message = ChatMessage(
                type = MessageType.CHAT,
                roomId = "room-1",
                sender = "user1",
                senderId = 1L,
                content = "Hello, World!",
                timestamp = timestamp
            )

            // then
            assertEquals(MessageType.CHAT, message.type)
            assertEquals("room-1", message.roomId)
            assertEquals("user1", message.sender)
            assertEquals(1L, message.senderId)
            assertEquals("Hello, World!", message.content)
            assertEquals(timestamp, message.timestamp)
        }

        @Test
        @DisplayName("기본 timestamp가 설정되어야 함")
        fun `should have default timestamp`() {
            // when
            val message = ChatMessage(
                type = MessageType.CHAT,
                roomId = "room-1",
                sender = "user1",
                content = "Test"
            )

            // then
            assertNotNull(message.timestamp)
        }

        @Test
        @DisplayName("senderId는 nullable이어야 함")
        fun `should allow null senderId`() {
            // when
            val message = ChatMessage(
                type = MessageType.CHAT,
                roomId = "room-1",
                sender = "anonymous",
                content = "Test"
            )

            // then
            assertEquals(null, message.senderId)
        }
    }

    @Nested
    @DisplayName("ChatMessage copy 테스트")
    inner class ChatMessageCopyTest {

        @Test
        @DisplayName("copy로 roomId만 변경 가능")
        fun `should copy with different roomId`() {
            // given
            val original = ChatMessage(
                type = MessageType.CHAT,
                roomId = "room-1",
                sender = "user1",
                content = "Original"
            )

            // when
            val copied = original.copy(roomId = "room-2")

            // then
            assertEquals("room-2", copied.roomId)
            assertEquals(original.sender, copied.sender)
            assertEquals(original.content, copied.content)
        }
    }

    @Nested
    @DisplayName("MessageType enum 테스트")
    inner class MessageTypeTest {

        @Test
        @DisplayName("모든 MessageType 값이 존재해야 함")
        fun `should have all message types`() {
            // then
            assertEquals(3, MessageType.entries.size)
            assertNotNull(MessageType.CHAT)
            assertNotNull(MessageType.JOIN)
            assertNotNull(MessageType.LEAVE)
        }
    }

    @Nested
    @DisplayName("NotificationMessage 테스트")
    inner class NotificationMessageTest {

        @Test
        @DisplayName("NotificationMessage 객체 생성 성공")
        fun `should create NotificationMessage`() {
            // when
            val notification = NotificationMessage(
                type = NotificationType.TEAM_INVITE,
                title = "팀 초대",
                message = "Team A에서 초대가 왔습니다",
                targetUserId = 1L,
                relatedId = 10L
            )

            // then
            assertEquals(NotificationType.TEAM_INVITE, notification.type)
            assertEquals("팀 초대", notification.title)
            assertEquals(1L, notification.targetUserId)
            assertEquals(10L, notification.relatedId)
        }
    }

    @Nested
    @DisplayName("NotificationType enum 테스트")
    inner class NotificationTypeTest {

        @Test
        @DisplayName("모든 NotificationType 값이 존재해야 함")
        fun `should have all notification types`() {
            // then
            assertEquals(5, NotificationType.entries.size)
            assertNotNull(NotificationType.TEAM_INVITE)
            assertNotNull(NotificationType.SCRIM_STARTED)
            assertNotNull(NotificationType.SCRIM_FINISHED)
            assertNotNull(NotificationType.MATCH_RESULT)
            assertNotNull(NotificationType.GENERAL)
        }
    }
}
