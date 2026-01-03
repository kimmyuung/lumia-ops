package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.CreateTeamRequest
import com.lumiaops.lumiaapi.dto.InviteMemberRequest
import com.lumiaops.lumiaapi.dto.UpdateMemberRoleRequest
import com.lumiaops.lumiaapi.dto.UpdateTeamRequest
import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.TeamMember
import com.lumiaops.lumiacore.service.TeamService
import com.lumiaops.lumiacore.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/teams")
class TeamController(
    private val teamService: TeamService,
    private val userService: UserService
) {

    @GetMapping
    fun getTeams(): List<Team> {
        return teamService.findAll()
    }

    @GetMapping("/me")
    fun getMyTeam(@AuthenticationPrincipal userDetails: UserDetails): Team? {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        
        // 현재는 첫 번째 팀만 반환 (추후 다중 팀 지원 시 로직 변경 필요)
        val memberships = teamService.findTeamsByUser(user)
        return memberships.firstOrNull()?.team
    }

    @GetMapping("/{id}")
    fun getTeam(@PathVariable id: Long): Team {
        return teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
    }

    @PostMapping
    fun createTeam(
        @RequestBody @Valid request: CreateTeamRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Team {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        
        val team = teamService.createTeam(request.name, request.description, user.id!!)
        
        // 생성자를 자동으로 멤버로 추가 (OWNER 권한)
        teamService.addMember(team, user, com.lumiaops.lumiacore.domain.TeamRole.OWNER)
        
        return team
    }

    @PatchMapping("/{id}")
    fun updateTeam(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateTeamRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Team {
        // 권한 체크 로직 필요 (현재는 생략)
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
        return teamService.updateTeam(
            id,
            request.name ?: team.name,
            request.description ?: team.description
        )
    }

    @DeleteMapping("/{id}")
    fun deleteTeam(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ) {
        // 권한 체크 로직 필요
        teamService.deleteTeam(id)
    }

    @PostMapping("/{id}/members")
    fun inviteMember(
        @PathVariable id: Long,
        @RequestBody @Valid request: InviteMemberRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): TeamMember {
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
        val targetUser = userService.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User to invite not found currently")

        return teamService.addMember(team, targetUser, request.role)
    }

    @DeleteMapping("/{id}/members/{memberId}")
    fun removeMember(
        @PathVariable id: Long,
        @PathVariable memberId: Long, // TeamMember ID가 아니라 User ID인지 확인 필요. 클라이언트는 memberId라고 보냄.
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

    @PatchMapping("/{id}/members/{memberId}")
    fun updateMemberRole(
        @PathVariable id: Long,
        @PathVariable memberId: Long,
        @RequestBody @Valid request: UpdateMemberRoleRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): TeamMember {
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
        val targetUser = userService.findById(memberId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Member user not found")

        return teamService.updateMemberRole(team, targetUser, request.role)
    }
    
    @PostMapping("/{id}/leave")
    fun leaveTeam(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ) {
        val team = teamService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            
        teamService.removeMember(team, user)
    }
}
