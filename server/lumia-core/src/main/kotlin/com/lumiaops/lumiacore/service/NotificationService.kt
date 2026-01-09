package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Notification
import com.lumiaops.lumiacore.domain.NotificationType
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.NotificationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 알림 서비스
 */
@Service
@Transactional(readOnly = true)
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 알림 생성
     */
    @Transactional
    fun createNotification(
        userId: Long,
        type: NotificationType,
        title: String,
        message: String,
        relatedId: Long? = null,
        relatedType: String? = null
    ): Notification {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        }

        val notification = Notification(
            user = user,
            type = type,
            title = title,
            message = message,
            relatedId = relatedId,
            relatedType = relatedType
        )

        val saved = notificationRepository.save(notification)
        log.info("알림 생성: userId=$userId, type=$type, title=$title")

        // 실시간 알림 이벤트 발행 (WebSocket 전송용)
        eventPublisher.publishEvent(NotificationCreatedEvent(saved))

        return saved
    }

    /**
     * 여러 사용자에게 알림 전송
     */
    @Transactional
    fun createNotifications(
        userIds: List<Long>,
        type: NotificationType,
        title: String,
        message: String,
        relatedId: Long? = null,
        relatedType: String? = null
    ): List<Notification> {
        return userIds.mapNotNull { userId ->
            try {
                createNotification(userId, type, title, message, relatedId, relatedType)
            } catch (e: Exception) {
                log.warn("알림 생성 실패: userId=$userId, error=${e.message}")
                null
            }
        }
    }

    /**
     * 사용자의 알림 목록 조회
     */
    fun getNotifications(userId: Long, page: Int = 0, size: Int = 20): List<Notification> {
        val user = userRepository.findById(userId).orElse(null) ?: return emptyList()
        return notificationRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size))
    }

    /**
     * 읽지 않은 알림 목록
     */
    fun getUnreadNotifications(userId: Long): List<Notification> {
        val user = userRepository.findById(userId).orElse(null) ?: return emptyList()
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
    }

    /**
     * 읽지 않은 알림 개수
     */
    fun getUnreadCount(userId: Long): Long {
        val user = userRepository.findById(userId).orElse(null) ?: return 0
        return notificationRepository.countByUserAndIsReadFalse(user)
    }

    /**
     * 알림 읽음 처리
     */
    @Transactional
    fun markAsRead(notificationId: Long, userId: Long): Boolean {
        val notification = notificationRepository.findById(notificationId).orElse(null)
            ?: return false

        if (notification.user.id != userId) {
            log.warn("알림 읽음 처리 권한 없음: notificationId=$notificationId, userId=$userId")
            return false
        }

        notification.markAsRead()
        return true
    }

    /**
     * 모든 알림 읽음 처리
     */
    @Transactional
    fun markAllAsRead(userId: Long): Int {
        val user = userRepository.findById(userId).orElse(null) ?: return 0
        return notificationRepository.markAllAsRead(user)
    }

    /**
     * 오래된 알림 정리 (30일 이상)
     */
    @Transactional
    fun cleanupOldNotifications(): Int {
        val cutoffDate = LocalDateTime.now().minusDays(30)
        val count = notificationRepository.deleteOldNotifications(cutoffDate)
        log.info("오래된 알림 삭제: ${count}개")
        return count
    }

    // ==================== 편의 메서드 ====================

    /**
     * 팀 초대 알림
     */
    @Transactional
    fun notifyTeamInvite(userId: Long, teamName: String, inviterName: String, teamId: Long) {
        createNotification(
            userId = userId,
            type = NotificationType.TEAM_INVITE,
            title = "팀 초대",
            message = "${inviterName}님이 '$teamName' 팀에 초대했습니다.",
            relatedId = teamId,
            relatedType = "TEAM"
        )
    }

    /**
     * 스크림 시작 알림
     */
    @Transactional
    fun notifyScrimStarted(userIds: List<Long>, scrimTitle: String, scrimId: Long) {
        createNotifications(
            userIds = userIds,
            type = NotificationType.SCRIM_STARTED,
            title = "스크림 시작",
            message = "'$scrimTitle' 스크림이 시작되었습니다.",
            relatedId = scrimId,
            relatedType = "SCRIM"
        )
    }

    /**
     * 스크림 종료 알림
     */
    @Transactional
    fun notifyScrimFinished(userIds: List<Long>, scrimTitle: String, scrimId: Long) {
        createNotifications(
            userIds = userIds,
            type = NotificationType.SCRIM_FINISHED,
            title = "스크림 종료",
            message = "'$scrimTitle' 스크림이 종료되었습니다.",
            relatedId = scrimId,
            relatedType = "SCRIM"
        )
    }
}

/**
 * 알림 생성 이벤트 (WebSocket 실시간 전송용)
 */
data class NotificationCreatedEvent(val notification: Notification)
