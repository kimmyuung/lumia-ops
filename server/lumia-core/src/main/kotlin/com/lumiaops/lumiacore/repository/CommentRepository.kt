package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.Comment
import com.lumiaops.lumiacore.domain.CommentTargetType
import com.lumiaops.lumiacore.domain.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    
    /**
     * 대상별 코멘트 목록 (최신순)
     */
    fun findByTargetTypeAndTargetIdOrderByCreatedAtDesc(
        targetType: CommentTargetType,
        targetId: Long,
        pageable: Pageable
    ): List<Comment>

    /**
     * 대상별 코멘트 개수
     */
    fun countByTargetTypeAndTargetId(targetType: CommentTargetType, targetId: Long): Long

    /**
     * 사용자의 코멘트 목록
     */
    fun findByUserOrderByCreatedAtDesc(user: User, pageable: Pageable): List<Comment>

    /**
     * 대상의 모든 코멘트 삭제
     */
    fun deleteByTargetTypeAndTargetId(targetType: CommentTargetType, targetId: Long)
}
