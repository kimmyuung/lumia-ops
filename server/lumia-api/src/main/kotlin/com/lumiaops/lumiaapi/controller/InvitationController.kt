package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.domain.TeamRole
import com.lumiaops.lumiacore.service.InvitationService
import com.lumiaops.lumiacore.service.TeamService
import com.lumiaops.lumiacore.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.format.DateTimeFormatter

/**
 * 팀 초대 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api")
class InvitationController(
    private val invitationService: InvitationService,
    private val teamService: TeamService,
    private val userService: UserService
) {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // ==================== 팀 관련 초대 APIs ====================

    /**
     * 팀에 멤버 초대 (초대 생성 및 이메일 발송)
     * POST /api/teams/{teamId}/invitations
     */
    @PostMapping("/teams/{teamId}/invitations")
    fun createInvitation(
        @PathVariable teamId: Long,
        @Valid @RequestBody request: CreateInvitationRequest,
        @RequestHeader("X-User-Id") userId: Long // TODO: 실제 인증으로 교체
    ): ResponseEntity<CreateInvitationResponse> {
        val team = teamService.findById(teamId)
            ?: return ResponseEntity.notFound().build()

        val inviter = userService.findById(userId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // 권한 확인 (팀장 또는 OWNER/LEADER만 초대 가능)
        // TODO: 실제 권한 체크 로직 강화

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

    /**
     * 팀의 대기 중인 초대 목록 조회
     * GET /api/teams/{teamId}/invitations
     */
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

    /**
     * 초대 취소
     * DELETE /api/teams/{teamId}/invitations/{invitationId}
     */
    @DeleteMapping("/teams/{teamId}/invitations/{invitationId}")
    fun cancelInvitation(
        @PathVariable teamId: Long,
        @PathVariable invitationId: Long
    ): ResponseEntity<Void> {
        invitationService.cancelInvitation(invitationId)
        return ResponseEntity.noContent().build()
    }

    /**
     * 초대 재발송
     * POST /api/teams/{teamId}/invitations/{invitationId}/resend
     */
    @PostMapping("/teams/{teamId}/invitations/{invitationId}/resend")
    fun resendInvitation(
        @PathVariable teamId: Long,
        @PathVariable invitationId: Long
    ): ResponseEntity<Map<String, Boolean>> {
        val success = invitationService.resendInvitation(invitationId)
        return ResponseEntity.ok(mapOf("emailSent" to success))
    }

    // ==================== 초대 수락/거절 APIs ====================

    /**
     * 내가 받은 대기 중인 초대 목록 조회
     * GET /api/invitations/pending
     */
    @GetMapping("/invitations/pending")
    fun getMyPendingInvitations(
        @RequestHeader("X-User-Email") email: String // TODO: 실제 인증으로 교체
    ): ResponseEntity<List<InvitationResponse>> {
        val invitations = invitationService.getMyPendingInvitations(email)
            .map { toInvitationResponse(it) }

        return ResponseEntity.ok(invitations)
    }

    /**
     * 토큰으로 초대 상세 조회
     * GET /api/invitations/{token}
     */
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

    /**
     * 초대 수락
     * POST /api/invitations/{token}/accept
     */
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

    /**
     * 초대 거절
     * POST /api/invitations/{token}/decline
     */
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
