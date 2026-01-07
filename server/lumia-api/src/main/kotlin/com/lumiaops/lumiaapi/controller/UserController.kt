package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 사용자 관련 REST API 컨트롤러
 */
@Tag(name = "사용자", description = "사용자 정보 관리 API")
@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    )
    @GetMapping("/me")
    fun getMyInfo(
        @RequestHeader("X-User-Id") userId: Long
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
    @Operation(summary = "닉네임 최초 설정", description = "이메일 인증 후 최초 닉네임을 설정합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "설정 성공"),
        ApiResponse(responseCode = "400", description = "설정 실패")
    )
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

    @Operation(summary = "닉네임 변경", description = "닉네임을 변경합니다. (30일 제한)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "변경 성공"),
        ApiResponse(responseCode = "400", description = "변경 실패 (30일 제한 등)")
    )
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

    @Operation(summary = "닉네임 변경 가능 일수 조회", description = "닉네임 변경까지 남은 일수를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    )
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

    @Operation(summary = "게임 닉네임 업데이트", description = "이터널 리턴 인게임 닉네임을 설정합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "업데이트 성공"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    )
    @PutMapping("/me/game-nickname")
    fun updateGameNickname(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody request: UpdateGameNicknameRequest
    ): ResponseEntity<Any> {
        return try {
            val user = userService.updateGameNickname(userId, request.gameNickname)
            ResponseEntity.ok(UserResponse(
                id = user.id!!,
                email = user.email,
                nickname = user.nickname,
                status = user.status.name,
                daysUntilNicknameChange = user.daysUntilNicknameChange(),
                gameNickname = user.gameNickname
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "게임 닉네임 조회", description = "현재 설정된 이터널 리턴 인게임 닉네임을 조회합니다.")
    @GetMapping("/me/game-nickname")
    fun getGameNickname(
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<Map<String, String?>> {
        val user = userService.findById(userId)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(mapOf("gameNickname" to user.gameNickname))
    }
}

