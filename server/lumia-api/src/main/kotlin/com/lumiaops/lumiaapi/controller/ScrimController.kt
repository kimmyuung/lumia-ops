package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.AddMatchResultRequest
import com.lumiaops.lumiaapi.dto.CreateScrimRequest
import com.lumiaops.lumiaapi.dto.UpdateScrimRequest
import com.lumiaops.lumiaapi.dto.UpdateScrimStatusRequest
import com.lumiaops.lumiacore.domain.scrim.MatchResult
import com.lumiaops.lumiacore.domain.scrim.Scrim
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
    fun getScrims(): List<Scrim> {
        return scrimService.findAllScrims()
    }

    @GetMapping("/{id}")
    fun getScrim(@PathVariable id: Long): Scrim {
        return scrimService.findScrimById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Scrim not found")
    }

    @PostMapping
    fun createScrim(
        @RequestBody @Valid request: CreateScrimRequest
    ): Scrim {
        return scrimService.createScrim(request.title, request.startTime)
    }

    @PatchMapping("/{id}")
    fun updateScrim(
        @PathVariable id: Long,
        @RequestBody request: UpdateScrimRequest
    ): Scrim {
        return scrimService.updateScrim(id, request.title, request.startTime)
    }

    @DeleteMapping("/{id}")
    fun deleteScrim(@PathVariable id: Long) {
        scrimService.deleteScrim(id)
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateScrimStatusRequest
    ): Scrim {
        // ScrimStatus에 따라 로직 분기. 서비스엔 finishScrim만 있음.
        if (request.status == com.lumiaops.lumiaapi.dto.ScrimStatus.FINISHED) {
             return scrimService.finishScrim(id)
        }
        return scrimService.findScrimById(id)!!
    }

    @PostMapping("/{id}/results")
    fun addResult(
        @PathVariable id: Long,
        @RequestBody @Valid request: AddMatchResultRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): MatchResult {
        val scrim = scrimService.findScrimById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Scrim not found")

        // 현재 유저의 팀을 찾아서 결과에 연결해야 함
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        val team = teamService.findTeamsByUser(user).firstOrNull()?.team
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no team")

        // 매치 생성 또는 조회 (라운드별)
        // ScrimService에 findMatchByScrimAndRound 같은게 필요하지만, 없으면 addMatch로 생성
        val matches = scrimService.findMatchesByScrim(scrim)
        var match = matches.find { it.roundNumber == request.round }
        
        if (match == null) {
            match = scrimService.addMatch(scrim, request.round)
        }

        // 점수 계산 (kill * 1 + placement based score) - 기본 로직
        val totalScore = request.kills + (9 - request.placement) // 예시 점수

        return scrimService.addMatchResult(match, team, request.placement, request.kills, totalScore)
    }
}
