package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Comment
import com.lumiaops.lumiacore.domain.CommentTargetType
import com.lumiaops.lumiacore.domain.NotificationType
import com.lumiaops.lumiacore.repository.CommentRepository
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 코멘트 서비스
 */
@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 코멘트 작성
     */
    @Transactional
    fun createComment(
        targetType: CommentTargetType,
        targetId: Long,
        userId: Long,
        content: String,
        notifyUserIds: List<Long> = emptyList() // 알림 받을 사용자들
    ): Comment {
        val user = userRepository.findById(userId).orElseThrow {
            IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        }

        require(content.isNotBlank()) { "코멘트 내용은 비어있을 수 없습니다" }

        val comment = Comment(
            targetType = targetType,
            targetId = targetId,
            user = user,
            content = content
        )

        val saved = commentRepository.save(comment)
        log.info("코멘트 생성: targetType=$targetType, targetId=$targetId, userId=$userId")

        // 알림 전송 (작성자 제외)
        notifyUserIds
            .filter { it != userId }
            .forEach { targetUserId ->
                notificationService.createNotification(
                    userId = targetUserId,
                    type = NotificationType.COMMENT_ADDED,
                    title = "새 코멘트",
                    message = "${user.nickname}님이 코멘트를 남겼습니다.",
                    relatedId = targetId,
                    relatedType = targetType.name
                )
            }

        return saved
    }

    /**
     * 대상별 코멘트 목록 조회
     */
    fun getComments(
        targetType: CommentTargetType,
        targetId: Long,
        page: Int = 0,
        size: Int = 20
    ): List<Comment> {
        return commentRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
            targetType, targetId, PageRequest.of(page, size)
        )
    }

    /**
     * 코멘트 개수
     */
    fun getCommentCount(targetType: CommentTargetType, targetId: Long): Long {
        return commentRepository.countByTargetTypeAndTargetId(targetType, targetId)
    }

    /**
     * 코멘트 수정
     */
    @Transactional
    fun updateComment(commentId: Long, userId: Long, newContent: String): Comment {
        val comment = commentRepository.findById(commentId).orElseThrow {
            IllegalArgumentException("코멘트를 찾을 수 없습니다: $commentId")
        }

        require(comment.user.id == userId) { "수정 권한이 없습니다" }

        comment.updateContent(newContent)
        return comment
    }

    /**
     * 코멘트 삭제
     */
    @Transactional
    fun deleteComment(commentId: Long, userId: Long) {
        val comment = commentRepository.findById(commentId).orElseThrow {
            IllegalArgumentException("코멘트를 찾을 수 없습니다: $commentId")
        }

        require(comment.user.id == userId) { "삭제 권한이 없습니다" }

        commentRepository.delete(comment)
        log.info("코멘트 삭제: id=$commentId, userId=$userId")
    }
}
