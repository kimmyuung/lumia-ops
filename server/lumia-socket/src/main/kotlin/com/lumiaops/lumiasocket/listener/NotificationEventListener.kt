package com.lumiaops.lumiasocket.listener

import com.lumiaops.lumiacore.service.NotificationCreatedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * 알림 이벤트 리스너
 * NotificationService에서 발행한 이벤트를 받아 WebSocket으로 실시간 전송
 */
@Component
class NotificationEventListener(
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * 알림 생성 이벤트 처리
     * 해당 사용자의 개인 큐로 알림 전송
     */
    @EventListener
    fun handleNotificationCreated(event: NotificationCreatedEvent) {
        val notification = event.notification
        val userId = notification.user.id ?: return

        logger.info { "Sending notification to user $userId: ${notification.title}" }

        // 개인 큐로 전송 (/user/{userId}/queue/notifications)
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            NotificationPayload(
                id = notification.id!!,
                type = notification.type.name,
                title = notification.title,
                message = notification.message,
                relatedId = notification.relatedId,
                relatedType = notification.relatedType,
                createdAt = notification.createdAt.toString()
            )
        )
    }
}

/**
 * WebSocket 알림 페이로드
 */
data class NotificationPayload(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val relatedId: Long?,
    val relatedType: String?,
    val createdAt: String
)
