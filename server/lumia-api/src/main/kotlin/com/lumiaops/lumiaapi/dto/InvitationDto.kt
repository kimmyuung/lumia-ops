package com.lumiaops.lumiaapi.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * 초대 생성 요청 DTO
 */
data class CreateInvitationRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    val role: String? = "MEMBER", // MEMBER or ADMIN (LEADER도 가능)

    val message: String? = null
)

/**
 * 초대 응답 DTO
 */
data class InvitationResponse(
    val id: Long,
    val teamId: Long,
    val teamName: String,
    val invitedEmail: String,
    val invitedById: Long,
    val inviterName: String,
    val role: String,
    val status: String,
    val token: String,
    val expiresAt: String,
    val createdAt: String,
    val respondedAt: String?
)

/**
 * 초대 생성 결과 DTO
 */
data class CreateInvitationResponse(
    val invitation: InvitationResponse,
    val emailSent: Boolean
)

/**
 * 초대 수락 결과 DTO
 */
data class AcceptInvitationResponse(
    val teamId: Long,
    val message: String
)
