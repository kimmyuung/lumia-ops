package com.lumiaops.lumiacore.domain

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*

/**
 * 코멘트 엔티티
 * 전략, 스크림 등에 달리는 비동기 댓글
 */
@Entity
@Table(
    name = "comments",
    indexes = [
        Index(name = "idx_comment_target", columnList = "targetType, targetId"),
        Index(name = "idx_comment_user", columnList = "user_id")
    ]
)
class Comment(
    /** 대상 타입 (STRATEGY, SCRIM 등) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val targetType: CommentTargetType,

    /** 대상 ID */
    @Column(nullable = false)
    val targetId: Long,

    /** 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    /** 내용 */
    @Column(nullable = false, length = 1000)
    var content: String
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    /**
     * 내용 수정
     */
    fun updateContent(newContent: String) {
        require(newContent.isNotBlank()) { "코멘트 내용은 비어있을 수 없습니다" }
        this.content = newContent
    }
}

/**
 * 코멘트 대상 타입
 */
enum class CommentTargetType {
    STRATEGY,    // 전략
    SCRIM,       // 스크림
    MATCH        // 매치
}
