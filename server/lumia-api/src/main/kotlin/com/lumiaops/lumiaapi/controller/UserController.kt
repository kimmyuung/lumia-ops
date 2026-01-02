package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 사용자 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    /**
     * 내 정보 조회
     * GET /api/users/me
     */
    @GetMapping("/me")
    fun getMyInfo(
        @RequestHeader("X-User-Id") userId: Long // TODO: 실제 인증으로 교체
    ): ResponseEntity<UserResponse> {
        val user = userService.findById(userId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(UserResponse(
            id = user.id!!,
            email = user.email,
            nickname = user.nickname,
            status = user.status.name,
            daysUntilNicknameChange = user.daysUntilNicknameChange()
        ))
    }

    /**
     * 닉네임 설정 (첫 설정)
     * POST /api/users/me/nickname
     */
    @PostMapping("/me/nickname")
    fun setInitialNickname(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdateNicknameRequest
    ): ResponseEntity<Any> {
        return try {
            val user = userService.setInitialNickname(userId, request.nickname)
            ResponseEntity.ok(UserResponse(
                id = user.id!!,
                email = user.email,
                nickname = user.nickname,
                status = user.status.name,
                daysUntilNicknameChange = user.daysUntilNicknameChange()
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "닉네임 설정에 실패했습니다"))
        }
    }

    /**
     * 닉네임 변경 (30일 제한)
     * PUT /api/users/me/nickname
     */
    @PutMapping("/me/nickname")
    fun updateNickname(
        @RequestHeader("X-User-Id") userId: Long,
        @Valid @RequestBody request: UpdateNicknameRequest
    ): ResponseEntity<Any> {
        return try {
            val user = userService.updateNickname(userId, request.nickname)
            ResponseEntity.ok(UserResponse(
                id = user.id!!,
                email = user.email,
                nickname = user.nickname,
                status = user.status.name,
                daysUntilNicknameChange = user.daysUntilNicknameChange()
            ))
        } catch (e: IllegalStateException) {
            // 30일 제한
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "닉네임 변경에 실패했습니다"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "닉네임 변경에 실패했습니다"))
        }
    }

    /**
     * 닉네임 변경까지 남은 일수 조회
     * GET /api/users/me/nickname/remaining-days
     */
    @GetMapping("/me/nickname/remaining-days")
    fun getNicknameChangeRemainingDays(
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<Map<String, Long>> {
        return try {
            val days = userService.getDaysUntilNicknameChange(userId)
            ResponseEntity.ok(mapOf("daysRemaining" to days))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
