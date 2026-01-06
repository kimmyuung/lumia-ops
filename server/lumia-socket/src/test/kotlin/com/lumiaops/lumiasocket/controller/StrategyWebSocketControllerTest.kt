package com.lumiaops.lumiasocket.controller

import com.lumiaops.lumiasocket.dto.StrategyUpdateMessage
import com.lumiaops.lumiasocket.dto.StrategyAction
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.messaging.simp.SimpMessagingTemplate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("StrategyWebSocketController 단위 테스트")
class StrategyWebSocketControllerTest {

    private lateinit var controller: StrategyWebSocketController
    private lateinit var messagingTemplate: SimpMessagingTemplate

    @BeforeEach
    fun setUp() {
        messagingTemplate = mockk(relaxed = true)
        controller = StrategyWebSocketController(messagingTemplate)
    }

    @Nested
    @DisplayName("updateStrategy 테스트")
    inner class UpdateStrategyTest {

        @Test
        @DisplayName("전략 업데이트 시 timestamp가 설정되어야 함")
        fun `should set timestamp when updating strategy`() {
            // given
            val strategyId = "strategy-123"
            val message = StrategyUpdateMessage(
                strategyId = strategyId,
                sender = "user1",
                senderId = 1L,
                action = StrategyAction.ADD_MARKER,
                data = """{"x": 100, "y": 200}"""
            )

            // when
            val result = controller.updateStrategy(strategyId, message)

            // then
            assertNotNull(result.timestamp)
            assertEquals(StrategyAction.ADD_MARKER, result.action)
            assertEquals("user1", result.sender)
        }

        @Test
        @DisplayName("데이터가 그대로 유지되어야 함")
        fun `should preserve data`() {
            // given
            val data = """{"x": 50.0, "y": 75.0, "color": "red"}"""
            val message = StrategyUpdateMessage(
                strategyId = "strategy-1",
                sender = "user2",
                senderId = 2L,
                action = StrategyAction.ADD_PATH,
                data = data
            )

            // when
            val result = controller.updateStrategy("strategy-1", message)

            // then
            assertEquals(data, result.data)
        }
    }

    @Nested
    @DisplayName("shareCursor 테스트")
    inner class ShareCursorTest {

        @Test
        @DisplayName("커서 위치가 그대로 반환되어야 함")
        fun `should return cursor position as is`() {
            // given
            val cursor = CursorPosition(
                userId = 1L,
                username = "user1",
                x = 100.5,
                y = 200.3
            )

            // when
            val result = controller.shareCursor("strategy-1", cursor)

            // then
            assertEquals(1L, result.userId)
            assertEquals("user1", result.username)
            assertEquals(100.5, result.x)
            assertEquals(200.3, result.y)
        }
    }

    @Nested
    @DisplayName("joinStrategy 테스트")
    inner class JoinStrategyTest {

        @Test
        @DisplayName("참여 메시지가 생성되어야 함")
        fun `should create join message`() {
            // given
            val message = StrategyJoinMessage(
                userId = 1L,
                username = "newEditor"
            )

            // when
            val result = controller.joinStrategy("strategy-1", message)

            // then
            assertEquals(1L, result.userId)
            assertEquals("newEditor", result.username)
            assert(result.message.contains("newEditor"))
            assert(result.message.contains("참여"))
        }
    }
}
