package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiacore.domain.Notification
import com.lumiaops.lumiacore.service.NotificationService
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
 * 알림 API 컨트롤러
 */
@Tag(name = "알림", description = "사용자 알림 관리 API")
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val userService: UserService
) {

    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun getNotifications(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<NotificationResponse>> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val notifications = notificationService.getNotifications(user.id!!, page, size)
        return ResponseEntity.ok(notifications.map { it.toResponse() })
    }

    @Operation(summary = "읽지 않은 알림 조회", description = "읽지 않은 알림 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/unread")
    fun getUnreadNotifications(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<NotificationResponse>> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val notifications = notificationService.getUnreadNotifications(user.id!!)
        return ResponseEntity.ok(notifications.map { it.toResponse() })
    }

    @Operation(summary = "읽지 않은 알림 개수", description = "읽지 않은 알림 개수를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/unread/count")
    fun getUnreadCount(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, Long>> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val count = notificationService.getUnreadCount(user.id!!)
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @PatchMapping("/{id}/read")
    fun markAsRead(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, Boolean>> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val success = notificationService.markAsRead(id, user.id!!)
        return ResponseEntity.ok(mapOf("success" to success))
    }

    @Operation(summary = "모든 알림 읽음 처리", description = "모든 알림을 읽음 처리합니다.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @PatchMapping("/read-all")
    fun markAllAsRead(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<String, Int>> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val count = notificationService.markAllAsRead(user.id!!)
        return ResponseEntity.ok(mapOf("updated" to count))
    }

    // DTO 변환
    private fun Notification.toResponse() = NotificationResponse(
        id = this.id!!,
        type = this.type.name,
        title = this.title,
        message = this.message,
        relatedId = this.relatedId,
        relatedType = this.relatedType,
        isRead = this.isRead,
        createdAt = this.createdAt
    )
}

/**
 * 알림 응답 DTO
 */
data class NotificationResponse(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val relatedId: Long?,
    val relatedType: String?,
    val isRead: Boolean,
    val createdAt: LocalDateTime?
)
