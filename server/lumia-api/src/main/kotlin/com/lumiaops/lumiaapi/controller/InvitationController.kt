package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.domain.TeamRole
import com.lumiaops.lumiacore.service.InvitationService
import com.lumiaops.lumiacore.service.TeamService
import com.lumiaops.lumiacore.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeFormatter

/**
 * 팀 초대 관련 REST API 컨트롤러
 */
@Tag(name = "초대", description = "팀 초대 생성/수락/거절 API")
@RestController
@RequestMapping("/api")
class InvitationController(
    private val invitationService: InvitationService,
    private val teamService: TeamService,
    private val userService: UserService
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // ==================== 팀 관련 초대 APIs ====================

    @Operation(summary = "팀에 멤버 초대", description = "이메일로 사용자를 팀에 초대합니다. 초대 이메일이 발송됩니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "초대 생성 성공"),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음"),
        ApiResponse(responseCode = "401", description = "인증 실패")
    )
    @PostMapping("/teams/{teamId}/invitations")
    fun createInvitation(
        @Parameter(description = "팀 ID") @PathVariable teamId: Long,
        @Valid @RequestBody request: CreateInvitationRequest,
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<CreateInvitationResponse> {
        val team = teamService.findById(teamId)
            ?: return ResponseEntity.notFound().build()

        val inviter = userService.findById(userId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val role = when (request.role?.uppercase()) {
            "ADMIN", "LEADER" -> TeamRole.LEADER
            else -> TeamRole.MEMBER
        }

        val (invitation, emailSent) = invitationService.createInvitation(
            team = team,
            invitedEmail = request.email,
            inviter = inviter,
            role = role,
            message = request.message
        )

        val response = CreateInvitationResponse(
            invitation = toInvitationResponse(invitation),
            emailSent = emailSent
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(summary = "팀의 대기 중인 초대 목록", description = "팀에서 발송한 대기 중인 초대 목록을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    )
    @GetMapping("/teams/{teamId}/invitations")
    fun getTeamInvitations(
        @PathVariable teamId: Long
    ): ResponseEntity<List<InvitationResponse>> {
        val team = teamService.findById(teamId)
            ?: return ResponseEntity.notFound().build()

        val invitations = invitationService.getTeamInvitations(team)
            .map { toInvitationResponse(it) }

        return ResponseEntity.ok(invitations)
    }

    @Operation(summary = "초대 취소", description = "초대를 취소합니다.")
    @ApiResponse(responseCode = "204", description = "취소 성공")
    @DeleteMapping("/teams/{teamId}/invitations/{invitationId}")
    fun cancelInvitation(
        @PathVariable teamId: Long,
        @PathVariable invitationId: Long
    ): ResponseEntity<Void> {
        invitationService.cancelInvitation(invitationId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "초대 재발송", description = "초대 이메일을 다시 발송합니다.")
    @ApiResponse(responseCode = "200", description = "발송 결과")
    @PostMapping("/teams/{teamId}/invitations/{invitationId}/resend")
    fun resendInvitation(
        @PathVariable teamId: Long,
        @PathVariable invitationId: Long
    ): ResponseEntity<Map<String, Boolean>> {
        val success = invitationService.resendInvitation(invitationId)
        return ResponseEntity.ok(mapOf("emailSent" to success))
    }

    // ==================== 초대 수락/거절 APIs ====================

    @Operation(summary = "내 초대 목록 조회", description = "내가 받은 대기 중인 초대 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/invitations/pending")
    fun getMyPendingInvitations(
        @RequestHeader("X-User-Email") email: String // TODO: 실제 인증으로 교체
    ): ResponseEntity<List<InvitationResponse>> {
        val invitations = invitationService.getMyPendingInvitations(email)
            .map { toInvitationResponse(it) }

        return ResponseEntity.ok(invitations)
    }

    @Operation(summary = "토큰으로 초대 조회", description = "토큰으로 초대 상세 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "초대를 찾을 수 없음")
    )
    @GetMapping("/invitations/{token}")
    fun getInvitationByToken(
        @PathVariable token: String
    ): ResponseEntity<InvitationResponse> {
        return try {
            val invitation = invitationService.getInvitationByToken(token)
            ResponseEntity.ok(toInvitationResponse(invitation))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(summary = "초대 수락", description = "토큰으로 초대를 수락하고 팀에 가입합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수락 성공"),
        ApiResponse(responseCode = "400", description = "수락 실패")
    )
    @PostMapping("/invitations/{token}/accept")
    fun acceptInvitation(
        @PathVariable token: String,
        @RequestHeader("X-User-Id") userId: Long // TODO: 실제 인증으로 교체
    ): ResponseEntity<AcceptInvitationResponse> {
        val user = userService.findById(userId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val member = invitationService.acceptInvitation(token, user)
            val response = AcceptInvitationResponse(
                teamId = member.team.id!!,
                message = "${member.team.name} 팀에 성공적으로 참여했습니다"
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @Operation(summary = "초대 거절", description = "토큰으로 초대를 거절합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "거절 성공"),
        ApiResponse(responseCode = "400", description = "거절 실패")
    )
    @PostMapping("/invitations/{token}/decline")
    fun declineInvitation(
        @PathVariable token: String
    ): ResponseEntity<Void> {
        return try {
            invitationService.declineInvitation(token)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    // ==================== Helper Methods ====================

    private fun toInvitationResponse(invitation: com.lumiaops.lumiacore.domain.TeamInvitation): InvitationResponse {
        return InvitationResponse(
            id = invitation.id!!,
            teamId = invitation.team.id!!,
            teamName = invitation.team.name,
            invitedEmail = invitation.invitedEmail,
            invitedById = invitation.invitedBy.id!!,
            inviterName = invitation.invitedBy.nickname ?: "알 수 없음",
            role = when (invitation.role) {
                TeamRole.OWNER -> "ADMIN"
                TeamRole.LEADER -> "ADMIN"
                TeamRole.MEMBER -> "MEMBER"
            },
            status = invitation.status.name,
            token = invitation.token,
            expiresAt = invitation.expiresAt.format(formatter),
            createdAt = invitation.createdAt.format(formatter),
            respondedAt = invitation.respondedAt?.format(formatter)
        )
    }

    // ==================== Exception Handler ====================

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<Map<String, String>> {
        return ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "잘못된 요청입니다")))
    }
}
