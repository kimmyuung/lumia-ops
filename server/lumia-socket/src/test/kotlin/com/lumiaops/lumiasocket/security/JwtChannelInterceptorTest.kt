package com.lumiaops.lumiasocket.security

import com.lumiaops.lumiacore.security.JwtTokenProvider
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.GenericMessage
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DisplayName("JwtChannelInterceptor 단위 테스트")
class JwtChannelInterceptorTest {

    private lateinit var interceptor: JwtChannelInterceptor
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var messageChannel: MessageChannel

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mockk()
        interceptor = JwtChannelInterceptor(jwtTokenProvider)
        messageChannel = mockk()
    }

    @Nested
    @DisplayName("CONNECT 프레임 인증 테스트")
    inner class ConnectAuthenticationTest {

        @Test
        @DisplayName("유효한 JWT 토큰으로 인증 성공")
        fun `should authenticate with valid JWT token`() {
            // given
            val token = "valid.jwt.token"
            val userId = 1L
            val email = "test@example.com"

            val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
            accessor.setNativeHeader("Authorization", "Bearer $token")
            accessor.setLeaveMutable(true)  // mutable로 설정해야 user 수정이 가능
            val message = MessageBuilder.createMessage(ByteArray(0), accessor.messageHeaders)

            every { jwtTokenProvider.validateToken(token) } returns true
            every { jwtTokenProvider.getUserIdFromToken(token) } returns userId
            every { jwtTokenProvider.getEmailFromToken(token) } returns email

            // when
            val result = interceptor.preSend(message, messageChannel)

            // then
            assertNotNull(result)
            // accessor가 mutable이고 user가 설정됨
            val resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor::class.java)
            assertNotNull(resultAccessor?.user)
            
            val auth = resultAccessor?.user as? org.springframework.security.authentication.UsernamePasswordAuthenticationToken
            val principal = auth?.principal as? WebSocketPrincipal
            assertEquals(userId, principal?.userId)
            assertEquals(email, principal?.email)
        }

        @Test
        @DisplayName("유효하지 않은 JWT 토큰 시 인증 없이 진행")
        fun `should proceed without authentication when token is invalid`() {
            // given
            val token = "invalid.jwt.token"

            val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
            accessor.setNativeHeader("Authorization", "Bearer $token")
            val message = createMessage(accessor)

            every { jwtTokenProvider.validateToken(token) } returns false

            // when
            val result = interceptor.preSend(message, messageChannel)

            // then
            assertNotNull(result)
            val resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor::class.java)
            assertNull(resultAccessor?.user)
        }

        @Test
        @DisplayName("Authorization 헤더 없으면 익명 처리")
        fun `should allow anonymous connection without Authorization header`() {
            // given
            val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
            val message = createMessage(accessor)

            // when
            val result = interceptor.preSend(message, messageChannel)

            // then
            assertNotNull(result)
            val resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor::class.java)
            assertNull(resultAccessor?.user)
        }

        @Test
        @DisplayName("Bearer 접두사 없는 헤더는 무시")
        fun `should ignore Authorization header without Bearer prefix`() {
            // given
            val accessor = StompHeaderAccessor.create(StompCommand.CONNECT)
            accessor.setNativeHeader("Authorization", "Basic sometoken")
            val message = createMessage(accessor)

            // when
            val result = interceptor.preSend(message, messageChannel)

            // then
            assertNotNull(result)
            val resultAccessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor::class.java)
            assertNull(resultAccessor?.user)
        }
    }

    @Nested
    @DisplayName("비-CONNECT 프레임 테스트")
    inner class NonConnectFrameTest {

        @Test
        @DisplayName("SEND 프레임은 인증 처리 안함")
        fun `should not process authentication for SEND command`() {
            // given
            val accessor = StompHeaderAccessor.create(StompCommand.SEND)
            accessor.setNativeHeader("Authorization", "Bearer some.token")
            val message = createMessage(accessor)

            // when
            val result = interceptor.preSend(message, messageChannel)

            // then
            assertNotNull(result)
            // 인증 처리하지 않으므로 user는 null
        }

        @Test
        @DisplayName("SUBSCRIBE 프레임은 인증 처리 안함")
        fun `should not process authentication for SUBSCRIBE command`() {
            // given
            val accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE)
            val message = createMessage(accessor)

            // when
            val result = interceptor.preSend(message, messageChannel)

            // then
            assertNotNull(result)
        }
    }

    @Nested
    @DisplayName("WebSocketPrincipal 테스트")
    inner class WebSocketPrincipalTest {

        @Test
        @DisplayName("getName()은 userId를 반환해야 함")
        fun `should return userId from getName()`() {
            // given
            val principal = WebSocketPrincipal(userId = 123L, email = "test@example.com")

            // when & then
            assertEquals("123", principal.name)
        }
    }

    private fun createMessage(accessor: StompHeaderAccessor): Message<ByteArray> {
        return GenericMessage(ByteArray(0), accessor.messageHeaders)
    }

    private fun createMutableMessage(accessor: StompHeaderAccessor): Message<ByteArray> {
        return GenericMessage(ByteArray(0), accessor.messageHeaders)
    }
}
