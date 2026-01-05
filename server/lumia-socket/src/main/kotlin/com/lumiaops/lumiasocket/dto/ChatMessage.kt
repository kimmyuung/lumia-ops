package com.lumiaops.lumiasocket.dto

import java.time.LocalDateTime

/**
 * 채팅 메시지 DTO
 */
data class ChatMessage(
    val type: MessageType,
    val roomId: String,
    val sender: String,
    val senderId: Long? = null,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class MessageType {
    CHAT,    // 일반 채팅 메시지
    JOIN,    // 입장 알림
    LEAVE    // 퇴장 알림
}

/**
 * 알림 메시지 DTO
 */
data class NotificationMessage(
    val type: NotificationType,
    val title: String,
    val message: String,
    val targetUserId: Long,
    val relatedId: Long? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    TEAM_INVITE,       // 팀 초대
    SCRIM_STARTED,     // 스크림 시작
    SCRIM_FINISHED,    // 스크림 종료
    MATCH_RESULT,      // 매치 결과 등록
    GENERAL            // 일반 알림
}
