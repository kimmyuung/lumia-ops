package com.lumiaops.lumiasocket.controller

import com.lumiaops.lumiasocket.dto.NotificationMessage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

private val logger = KotlinLogging.logger {}

/**
 * 알림 메시지 핸들러
 */
@Controller
class NotificationController(
    private val messagingTemplate: SimpMessagingTemplate
) {

    /**
     * 특정 사용자에게 알림 전송
     */
    fun sendNotificationToUser(userId: Long, notification: NotificationMessage) {
        logger.info { "Sending notification to user $userId: ${notification.title}" }
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            "/queue/notifications",
            notification
        )
    }

    /**
     * 팀 전체에 알림 전송
     */
    fun sendNotificationToTeam(teamId: Long, notification: NotificationMessage) {
        logger.info { "Sending notification to team $teamId: ${notification.title}" }
        messagingTemplate.convertAndSend(
            "/topic/team/$teamId/notifications",
            notification
        )
    }

    /**
     * 스크림 참가자 전체에 알림 전송
     */
    fun sendNotificationToScrim(scrimId: Long, notification: NotificationMessage) {
        logger.info { "Sending notification to scrim $scrimId: ${notification.title}" }
        messagingTemplate.convertAndSend(
            "/topic/scrim/$scrimId/notifications",
            notification
        )
    }
}
