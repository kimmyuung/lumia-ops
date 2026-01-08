package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiacore.domain.Comment
import com.lumiaops.lumiacore.domain.CommentTargetType
import com.lumiaops.lumiacore.service.CommentService
import com.lumiaops.lumiacore.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

/**
 * 코멘트 API 컨트롤러
 */
@Tag(name = "코멘트", description = "전략/스크림 코멘트 API")
@RestController
@RequestMapping("/api/comments")
class CommentController(
    private val commentService: CommentService,
    private val userService: UserService
) {

    @Operation(summary = "코멘트 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{targetType}/{targetId}")
    fun getComments(
        @PathVariable targetType: CommentTargetType,
        @PathVariable targetId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<CommentResponse>> {
        val comments = commentService.getComments(targetType, targetId, page, size)
        return ResponseEntity.ok(comments.map { it.toResponse() })
    }

    @Operation(summary = "코멘트 개수 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{targetType}/{targetId}/count")
    fun getCommentCount(
        @PathVariable targetType: CommentTargetType,
        @PathVariable targetId: Long
    ): ResponseEntity<Map<String, Long>> {
        val count = commentService.getCommentCount(targetType, targetId)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @Operation(summary = "코멘트 작성")
    @ApiResponse(responseCode = "201", description = "작성 성공")
    @PostMapping("/{targetType}/{targetId}")
    fun createComment(
        @PathVariable targetType: CommentTargetType,
        @PathVariable targetId: Long,
        @RequestBody request: CreateCommentRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CommentResponse> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val comment = commentService.createComment(
            targetType = targetType,
            targetId = targetId,
            userId = user.id!!,
            content = request.content
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(comment.toResponse())
    }

    @Operation(summary = "코멘트 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/{commentId}")
    fun updateComment(
        @PathVariable commentId: Long,
        @RequestBody request: UpdateCommentRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CommentResponse> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val comment = commentService.updateComment(commentId, user.id!!, request.content)
        return ResponseEntity.ok(comment.toResponse())
    }

    @Operation(summary = "코멘트 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable commentId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        commentService.deleteComment(commentId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    private fun Comment.toResponse() = CommentResponse(
        id = this.id!!,
        targetType = this.targetType.name,
        targetId = this.targetId,
        userId = this.user.id!!,
        userName = this.user.nickname ?: "익명",
        content = this.content,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

data class CreateCommentRequest(val content: String)
data class UpdateCommentRequest(val content: String)

data class CommentResponse(
    val id: Long,
    val targetType: String,
    val targetId: Long,
    val userId: Long,
    val userName: String,
    val content: String,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
