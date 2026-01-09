package com.lumiaops.lumiaapi.dto

import com.lumiaops.lumiacore.domain.TeamRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTeamRequest(
    @field:NotBlank(message = "팀 이름은 필수입니다")
    @field:Size(min = 2, max = 30, message = "팀 이름은 2자 이상 30자 이하여야 합니다")
    val name: String,

    @field:Size(max = 500, message = "설명은 500자 이하여야 합니다")
    val description: String? = null
)

data class UpdateTeamRequest(
    @field:Size(min = 2, max = 30, message = "팀 이름은 2자 이상 30자 이하여야 합니다")
    val name: String?,

    @field:Size(max = 500, message = "설명은 500자 이하여야 합니다")
    val description: String? = null
)

data class InviteMemberRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    val role: TeamRole = TeamRole.MEMBER
)

data class UpdateMemberRoleRequest(
    val role: TeamRole
)

/**
 * 팀 응답 DTO
 */
data class TeamResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: java.time.LocalDateTime?,
    val updatedAt: java.time.LocalDateTime?
)

/**
 * 팀 멤버 응답 DTO
 */
data class TeamMemberResponse(
    val id: Long,
    val userId: Long,
    val nickname: String?,
    val role: String,
    val createdAt: java.time.LocalDateTime?
)
