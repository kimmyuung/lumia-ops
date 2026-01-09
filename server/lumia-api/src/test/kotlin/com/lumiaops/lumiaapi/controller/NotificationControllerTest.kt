package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiacore.domain.Notification
import com.lumiaops.lumiacore.domain.NotificationType
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.service.NotificationService
import com.lumiaops.lumiacore.service.UserService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("NotificationController 통합 테스트")
class NotificationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var notificationService: NotificationService

    @MockBean
    private lateinit var userService: UserService

    private lateinit var testUser: User
    private lateinit var testNotification: Notification

    @BeforeEach
    fun setUp() {
        testUser = User(
            email = "test@example.com",
            password = "password123"
        ).apply {
            val idField = User::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }

        testNotification = Notification(
            user = testUser,
            type = NotificationType.TEAM_INVITE,
            title = "팀 초대",
            message = "새로운 팀 초대가 있습니다."
        ).apply {
            val idField = Notification::class.java.getDeclaredField("id")
            idField.isAccessible = true
            idField.set(this, 1L)
        }
    }

    @Nested
    @DisplayName("GET /api/notifications")
    inner class GetNotifications {

        @Test
        @WithMockUser(username = "test@example.com")
        fun `알림 목록 조회 성공`() {
            `when`(userService.findByEmail("test@example.com")).thenReturn(testUser)
            `when`(notificationService.getNotifications(1L, 0, 20)).thenReturn(listOf(testNotification))

            mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].title").value("팀 초대"))
                .andExpect(jsonPath("$[0].type").value("TEAM_INVITE"))
        }

        @Test
        fun `인증 없이 접근시 401 반환`() {
            mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread")
    inner class GetUnreadNotifications {

        @Test
        @WithMockUser(username = "test@example.com")
        fun `읽지 않은 알림 조회 성공`() {
            `when`(userService.findByEmail("test@example.com")).thenReturn(testUser)
            `when`(notificationService.getUnreadNotifications(1L)).thenReturn(listOf(testNotification))

            mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].isRead").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread/count")
    inner class GetUnreadCount {

        @Test
        @WithMockUser(username = "test@example.com")
        fun `읽지 않은 알림 개수 조회 성공`() {
            `when`(userService.findByEmail("test@example.com")).thenReturn(testUser)
            `when`(notificationService.getUnreadCount(1L)).thenReturn(5L)

            mockMvc.perform(get("/api/notifications/unread/count"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.count").value(5))
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/{id}/read")
    inner class MarkAsRead {

        @Test
        @WithMockUser(username = "test@example.com")
        fun `알림 읽음 처리 성공`() {
            `when`(userService.findByEmail("test@example.com")).thenReturn(testUser)
            `when`(notificationService.markAsRead(1L, 1L)).thenReturn(true)

            mockMvc.perform(patch("/api/notifications/1/read"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/read-all")
    inner class MarkAllAsRead {

        @Test
        @WithMockUser(username = "test@example.com")
        fun `모든 알림 읽음 처리 성공`() {
            `when`(userService.findByEmail("test@example.com")).thenReturn(testUser)
            `when`(notificationService.markAllAsRead(1L)).thenReturn(10)

            mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.updated").value(10))
        }
    }
}
