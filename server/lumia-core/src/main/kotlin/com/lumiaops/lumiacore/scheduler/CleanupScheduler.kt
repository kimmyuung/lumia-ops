package com.lumiaops.lumiacore.scheduler

import com.lumiaops.lumiacore.service.NotificationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

/**
 * 정리 작업 스케줄러
 * 주기적으로 오래된 데이터를 정리합니다.
 */
@Component
class CleanupScheduler(
    private val notificationService: NotificationService
) {

    /**
     * 오래된 알림 정리 (매일 새벽 3시)
     * - 30일 이상 된 알림 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupOldNotifications() {
        log.info { "=== 오래된 알림 정리 시작 ===" }
        try {
            val deletedCount = notificationService.cleanupOldNotifications()
            log.info { "오래된 알림 정리 완료: ${deletedCount}개 삭제됨" }
        } catch (e: Exception) {
            log.error(e) { "알림 정리 중 오류 발생" }
        }
    }

    /**
     * 알림 정리 테스트용 (수동 호출)
     */
    fun runCleanupManually(): Int {
        log.info { "수동 알림 정리 실행" }
        return notificationService.cleanupOldNotifications()
    }
}
