package com.lumiaops.lumiacore.domain

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 알림 엔티티
 */
@Entity
@Table(name = "notifications")
class Notification(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false, length = 500)
    val message: String,

    /** 관련 엔티티 ID (팀, 스크림 등) */
    val relatedId: Long? = null,

    /** 관련 엔티티 타입 */
    val relatedType: String? = null,

    /** 읽음 여부 */
    var isRead: Boolean = false,

    /** 읽은 시간 */
    var readAt: LocalDateTime? = null
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    /**
     * 읽음 처리
     */
    fun markAsRead() {
        if (!isRead) {
            isRead = true
            readAt = LocalDateTime.now()
        }
    }
}

/**
 * 알림 타입
 */
enum class NotificationType {
    TEAM_INVITE,        // 팀 초대
    TEAM_JOIN,          // 팀 가입 알림 (팀장에게)
    TEAM_LEAVE,         // 팀 탈퇴 알림
    SCRIM_SCHEDULED,    // 스크림 예정
    SCRIM_STARTED,      // 스크림 시작
    SCRIM_FINISHED,     // 스크림 종료
    MATCH_RESULT,       // 매치 결과 등록
    STRATEGY_SHARED,    // 전략 공유됨
    COMMENT_ADDED,      // 코멘트 추가됨
    GENERAL             // 일반 알림
}
