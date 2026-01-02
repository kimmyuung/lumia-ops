package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.service.PasswordService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 비밀번호 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/auth/password")
class PasswordController(
    private val passwordService: PasswordService
) {

    /**
     * 비밀번호 찾기 요청 (이메일 발송)
     * POST /api/auth/password/forgot
     */
    @PostMapping("/forgot")
    fun forgotPassword(
        @Valid @RequestBody request: PasswordResetRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            val sent = passwordService.requestPasswordReset(request.email)
            ResponseEntity.ok(MessageResponse(
                success = sent,
                message = if (sent) {
                    "비밀번호 재설정 이메일을 발송했습니다. 이메일을 확인해주세요."
                } else {
                    "이메일 발송에 실패했습니다. 다시 시도해주세요."
                }
            ))
        } catch (e: IllegalArgumentException) {
            // 보안상 존재하지 않는 이메일도 동일한 메시지 반환
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "등록된 이메일이라면 비밀번호 재설정 링크가 발송됩니다."
            ))
        }
    }

    /**
     * 비밀번호 재설정 (토큰 검증 후)
     * POST /api/auth/password/reset
     */
    @PostMapping("/reset")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            passwordService.resetPassword(request.token, request.newPassword)
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해주세요."
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "비밀번호 재설정에 실패했습니다"))
        }
    }

    /**
     * 비밀번호 변경 (로그인 상태에서)
     * PUT /api/auth/password/change
     */
    @PutMapping("/change")
    fun changePassword(
        @RequestHeader("X-User-Id") userId: Long, // TODO: 실제 인증으로 교체
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            passwordService.changePassword(userId, request.oldPassword, request.newPassword)
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "비밀번호가 변경되었습니다."
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "비밀번호 변경에 실패했습니다"))
        }
    }
}
