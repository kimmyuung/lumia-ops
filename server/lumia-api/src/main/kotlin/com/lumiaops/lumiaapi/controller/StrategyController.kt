package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.CreateStrategyRequest
import com.lumiaops.lumiaapi.dto.UpdateStrategyRequest
import com.lumiaops.lumiacore.domain.strategy.Strategy
import com.lumiaops.lumiacore.service.StrategyService
import com.lumiaops.lumiacore.service.TeamService
import com.lumiaops.lumiacore.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Tag(name = "전략", description = "전략 맵 관리 API")
@RestController
@RequestMapping("/api/strategies")
class StrategyController(
    private val strategyService: StrategyService,
    private val userService: UserService,
    private val teamService: TeamService
) {

    @Operation(summary = "전략 목록 조회", description = "전략 목록을 조회합니다. 팀 ID로 필터링 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun getStrategies(@Parameter(description = "팀 ID (필터)") @RequestParam(required = false) teamId: Long?): List<Strategy> {
        if (teamId != null) {
            val team = teamService.findById(teamId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found")
            return strategyService.findByTeam(team)
        }
        return strategyService.findAll()
    }

    @Operation(summary = "전략 상세 조회", description = "ID로 특정 전략의 상세 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "전략을 찾을 수 없음")
    )
    @GetMapping("/{id}")
    fun getStrategy(@Parameter(description = "전략 ID") @PathVariable id: Long): Strategy {
        return strategyService.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy not found")
    }

    @Operation(summary = "전략 생성", description = "새로운 전략 맵을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
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

    @Operation(summary = "전략 수정", description = "전략의 제목이나 맵 데이터를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PatchMapping("/{id}")
    fun updateStrategy(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateStrategyRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): Strategy {
        // 권한 체크 필요 (작성자 또는 팀원)
        return strategyService.updateStrategy(id, request.title, request.mapData)
    }

    @Operation(summary = "전략 삭제", description = "전략을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{id}")
    fun deleteStrategy(@Parameter(description = "전략 ID") @PathVariable id: Long) {
        strategyService.deleteStrategy(id)
    }
}
