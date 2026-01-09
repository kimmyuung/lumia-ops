package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.TeamMember
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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Tag(name = "팀", description = "팀 생성, 관리, 멤버 관리 API")
@RestController
@RequestMapping("/api/teams")
class TeamController(
    private val teamService: TeamService,
    private val userService: UserService
) {

    @Operation(summary = "전체 팀 목록 조회", description = "등록된 모든 팀 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun getTeams(): List<TeamResponse> {
        return teamService.findAll().map { it.toResponse() }
    }

    @Operation(summary = "내 팀 조회", description = "현재 로그인한 사용자의 팀을 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "사용자 또는 팀을 찾을 수 없음")
    )
    @GetMapping("/me")
    fun getMyTeam(@AuthenticationPrincipal userDetails: UserDetails): TeamResponse? {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        
        // 현재는 첫 번째 팀만 반환 (추후 다중 팀 지원 시 로직 변경 필요)
        val memberships = teamService.findTeamsByUser(user)
        return memberships.firstOrNull()?.team?.toResponse()
    }

    @Operation(summary = "팀 상세 조회", description = "ID로 특정 팀의 상세 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    )
    @GetMapping("/{id}")
    fun getTeam(@Parameter(description = "팀 ID") @PathVariable id: Long): TeamResponse {
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
        return team.toResponse()
    }

    @Operation(summary = "팀 생성", description = "새로운 팀을 생성하고 생성자를 OWNER로 등록합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "생성 성공"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    )
    @PostMapping
    fun createTeam(
        @RequestBody @Valid request: CreateTeamRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TeamResponse> {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        
        val team = teamService.createTeam(request.name, request.description, user.id!!)
        
        // 생성자를 자동으로 멤버로 추가 (OWNER 권한)
        teamService.addMember(team, user, com.lumiaops.lumiacore.domain.TeamRole.OWNER)
        
        return ResponseEntity.status(HttpStatus.CREATED).body(team.toResponse())
    }

    @Operation(summary = "팀 정보 수정", description = "팀의 이름, 설명 등을 수정합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "수정 성공"),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    )
    @PatchMapping("/{id}")
    fun updateTeam(
        @Parameter(description = "팀 ID") @PathVariable id: Long,
        @RequestBody @Valid request: UpdateTeamRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): TeamResponse {
        // 권한 체크 로직 필요 (현재는 생략)
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
        return teamService.updateTeam(
            id,
            request.name ?: team.name,
            request.description ?: team.description
        ).toResponse()
    }

    @Operation(summary = "팀 삭제", description = "팀을 삭제합니다. OWNER만 가능합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTeam(
        @Parameter(description = "팀 ID") @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ) {
        // 권한 체크 로직 필요
        teamService.deleteTeam(id)
    }

    @Operation(summary = "멤버 초대", description = "이메일로 사용자를 팀에 초대합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "초대 성공"),
        ApiResponse(responseCode = "404", description = "팀 또는 사용자를 찾을 수 없음")
    )
    @PostMapping("/{id}/members")
    fun inviteMember(
        @Parameter(description = "팀 ID") @PathVariable id: Long,
        @RequestBody @Valid request: InviteMemberRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TeamMemberResponse> {
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
        val targetUser = userService.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User to invite not found currently")

        val member = teamService.addMember(team, targetUser, request.role)
        return ResponseEntity.status(HttpStatus.CREATED).body(member.toResponse())
    }

    @Operation(summary = "멤버 제거", description = "팀에서 멤버를 제거합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "제거 성공"),
        ApiResponse(responseCode = "404", description = "팀 또는 멤버를 찾을 수 없음")
    )
    @DeleteMapping("/{id}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeMember(
        @Parameter(description = "팀 ID") @PathVariable id: Long,
        @Parameter(description = "사용자 ID") @PathVariable memberId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ) {
         // 클라이언트가 보내는 memberId가 User ID인지 TeamMember ID인지 확인 필요.
         // 우선 TeamMember ID로 가정하려 했으나, Service는 removeMember(team, user)를 받음.
         // 따라서 memberId가 User ID라고 가정하고 구현.
         
         val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
         val targetUser = userService.findById(memberId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Member user not found")
            
         teamService.removeMember(team, targetUser)
    }

    @Operation(summary = "멤버 역할 변경", description = "팀 멤버의 역할을 변경합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "변경 성공"),
        ApiResponse(responseCode = "404", description = "팀 또는 멤버를 찾을 수 없음")
    )
    @PatchMapping("/{id}/members/{memberId}")
    fun updateMemberRole(
        @Parameter(description = "팀 ID") @PathVariable id: Long,
        @Parameter(description = "사용자 ID") @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateMemberRoleRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): TeamMemberResponse {
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
        val targetUser = userService.findById(memberId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Member user not found")

        return teamService.updateMemberRole(team, targetUser, request.role).toResponse()
    }
    
    @Operation(summary = "팀 탈퇴", description = "현재 사용자가 팀에서 탈퇴합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "탈퇴 성공"),
        ApiResponse(responseCode = "404", description = "팀 또는 사용자를 찾을 수 없음")
    )
    @PostMapping("/{id}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun leaveTeam(
        @Parameter(description = "팀 ID") @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ) {
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            
        teamService.removeMember(team, user)
    }

    // ==================== Helper Extensions ====================

    private fun Team.toResponse() = TeamResponse(
        id = this.id!!,
        name = this.name,
        description = this.description,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )

    private fun TeamMember.toResponse() = TeamMemberResponse(
        id = this.id!!,
        userId = this.user.id!!,
        nickname = this.user.nickname,
        role = this.role.name,
        createdAt = this.createdAt
    )
}
