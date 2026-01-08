package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.Notification
import com.lumiaops.lumiacore.domain.NotificationType
import com.lumiaops.lumiacore.domain.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface NotificationRepository : JpaRepository<Notification, Long> {
    
    /**
     * 사용자의 알림 목록 조회 (최신순)
     */
    fun findByUserOrderByCreatedAtDesc(user: User, pageable: Pageable): List<Notification>
    
    /**
     * 사용자의 읽지 않은 알림 목록
     */
    fun findByUserAndIsReadFalseOrderByCreatedAtDesc(user: User): List<Notification>
    
    /**
     * 사용자의 읽지 않은 알림 개수
     */
    fun countByUserAndIsReadFalse(user: User): Long
    
    /**
     * 타입별 알림 조회
     */
    fun findByUserAndTypeOrderByCreatedAtDesc(user: User, type: NotificationType): List<Notification>
    
    /**
     * 사용자의 모든 알림 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false")
    fun markAllAsRead(user: User): Int
    
    /**
     * 오래된 알림 삭제 (30일 이상)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    fun deleteOldNotifications(cutoffDate: java.time.LocalDateTime): Int
}
