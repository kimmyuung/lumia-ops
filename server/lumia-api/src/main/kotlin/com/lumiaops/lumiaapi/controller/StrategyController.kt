package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.CreateStrategyRequest
import com.lumiaops.lumiaapi.dto.UpdateStrategyRequest
import com.lumiaops.lumiacore.domain.strategy.Strategy
import com.lumiaops.lumiacore.service.StrategyService
import com.lumiaops.lumiacore.service.TeamService
import com.lumiaops.lumiacore.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/strategies")
class StrategyController(
    private val strategyService: StrategyService,
    private val userService: UserService,
    private val teamService: TeamService
) {

    @GetMapping
    fun getStrategies(@RequestParam(required = false) teamId: Long?): List<Strategy> {
        if (teamId != null) {
            val team = teamService.findById(teamId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            return strategyService.findByTeam(team)
        }
        return strategyService.findAll()
    }

    @GetMapping("/{id}")
    fun getStrategy(@PathVariable id: Long): Strategy {
        return strategyService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy not found")
    }

    @PostMapping
    fun createStrategy(
        @RequestBody @Valid request: CreateStrategyRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Strategy {
        val user = userService.findByEmail(userDetails.username)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        
        // 현재는 개인 전략으로 생성하거나, 사용자의 첫 번째 팀에 할당
        val team = teamService.findTeamsByUser(user).firstOrNull()?.team
        
        return strategyService.createStrategy(request.title, request.mapData, team, user.id!!)
    }

    @PatchMapping("/{id}")
    fun updateStrategy(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateStrategyRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Strategy {
        // 권한 체크 필요 (작성자 또는 팀원)
        return strategyService.updateStrategy(id, request.title, request.mapData)
    }

    @DeleteMapping("/{id}")
    fun deleteStrategy(@PathVariable id: Long) {
        strategyService.deleteStrategy(id)
    }
}
