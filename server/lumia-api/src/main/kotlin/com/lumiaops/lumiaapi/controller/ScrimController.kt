package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.domain.scrim.Scrim
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import com.lumiaops.lumiacore.domain.scrim.ScrimStatus
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.exception.NotFoundException
import com.lumiaops.lumiacore.service.ScrimService
import com.lumiaops.lumiacore.service.TeamService
import com.lumiaops.lumiacore.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/scrims")
class ScrimController(
    private val scrimService: ScrimService,
    private val teamService: TeamService,
    private val userService: UserService
) {

    @GetMapping
    fun getScrims(@RequestParam(required = false) status: ScrimStatus?): List<ScrimResponse> {
        val scrims = if (status != null) {
            scrimService.findScrimsByStatus(status)
        } else {
            scrimService.findAllScrims()
        }
        return scrims.map { it.toResponse() }
    }

    @GetMapping("/{id}")
    fun getScrim(@PathVariable id: Long): ScrimResponse {
        val scrim = scrimService.findScrimById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Scrim not found")
        return scrim.toResponse()
    }

    @PostMapping
    fun createScrim(@RequestBody @Valid request: CreateScrimRequest): ScrimResponse {
        val scrim = scrimService.createScrim(request.title, request.startTime)
        return scrim.toResponse()
    }

    @PatchMapping("/{id}")
    fun updateScrim(
        @PathVariable id: Long,
        @RequestBody request: UpdateScrimRequest
    ): ScrimResponse {
        val scrim = scrimService.updateScrim(id, request.title, request.startTime)
        return scrim.toResponse()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteScrim(@PathVariable id: Long) {
        scrimService.deleteScrim(id)
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateScrimStatusRequest
    ): ScrimResponse {
        val scrim = when (request.status) {
            ScrimStatus.IN_PROGRESS -> scrimService.startScrim(id)
            ScrimStatus.FINISHED -> scrimService.finishScrim(id)
            ScrimStatus.CANCELLED -> scrimService.cancelScrim(id)
            ScrimStatus.SCHEDULED -> throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Cannot change status back to SCHEDULED"
            )
        }
        return scrim.toResponse()
    }

    @PostMapping("/{id}/matches")
    fun addMatch(
        @PathVariable id: Long,
        @RequestParam(required = false) gameId: String?
    ): ScrimMatch {
        return scrimService.addMatch(id, gameId)
    }

    @PostMapping("/{id}/results")
    fun addResult(
        @PathVariable id: Long,
        @RequestBody @Valid request: AddMatchResultRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): MatchResult {
        val scrim = scrimService.findScrimById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Scrim not found")

        // 현재 유저의 팀을 찾아서 결과에 연결
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        val team = teamService.findTeamsByUser(user).firstOrNull()?.team
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no team")

        // 매치 조회 또는 생성
        var match = scrim.getMatch(request.round)
        if (match == null) {
            match = scrimService.addMatch(id)
        }

        // 결과 추가 (도메인 메서드 사용)
        return match.addResult(team, request.placement, request.kills)
    }

    // ==================== Helper Extensions ====================

    private fun Scrim.toResponse() = ScrimResponse(
        id = this.id!!,
        title = this.title,
        startTime = this.startTime,
        status = this.status,
        matchCount = this.matchCount(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
