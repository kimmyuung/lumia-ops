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

@Tag(name = "스크림", description = "스크림(연습경기) 관리 API")
@RestController
@RequestMapping("/api/scrims")
class ScrimController(
    private val scrimService: ScrimService,
    private val teamService: TeamService,
    private val userService: UserService
) {

    @Operation(summary = "스크림 목록 조회", description = "전체 스크림 목록을 조회합니다. 상태로 필터링 가능합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    fun getScrims(@Parameter(description = "스크림 상태 필터") @RequestParam(required = false) status: ScrimStatus?): List<ScrimResponse> {
        val scrims = if (status != null) {
            scrimService.findScrimsByStatus(status)
        } else {
            scrimService.findAllScrims()
        }
        return scrims.map { it.toResponse() }
    }

    @Operation(summary = "스크림 상세 조회", description = "ID로 특정 스크림의 상세 정보를 조회합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "스크림을 찾을 수 없음")
    )
    @GetMapping("/{id}")
    fun getScrim(@Parameter(description = "스크림 ID") @PathVariable id: Long): ScrimResponse {
        val scrim = scrimService.findScrimById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Scrim not found")
        return scrim.toResponse()
    }

    @Operation(summary = "스크림 생성", description = "새로운 스크림을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    @PostMapping
    fun createScrim(@RequestBody @Valid request: CreateScrimRequest): ScrimResponse {
        val scrim = scrimService.createScrim(request.title, request.startTime)
        return scrim.toResponse()
    }

    @Operation(summary = "스크림 수정", description = "스크림 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PatchMapping("/{id}")
    fun updateScrim(
        @PathVariable id: Long,
        @RequestBody request: UpdateScrimRequest
    ): ScrimResponse {
        val scrim = scrimService.updateScrim(id, request.title, request.startTime)
        return scrim.toResponse()
    }

    @Operation(summary = "스크림 삭제", description = "스크림을 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteScrim(@Parameter(description = "스크림 ID") @PathVariable id: Long) {
        scrimService.deleteScrim(id)
    }

    @Operation(summary = "스크림 상태 변경", description = "스크림의 상태를 변경합니다. (IN_PROGRESS, FINISHED, CANCELLED)")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "변경 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 상태 변경")
    )
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

    @Operation(summary = "매치 추가", description = "스크림에 새로운 매치를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "추가 성공")
    @PostMapping("/{id}/matches")
    fun addMatch(
        @PathVariable id: Long,
        @RequestParam(required = false) gameId: String?
    ): ScrimMatch {
        return scrimService.addMatch(id, gameId)
    }

    @Operation(summary = "매치 결과 입력", description = "매치의 결과(placement, kills)를 입력합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "입력 성공"),
        ApiResponse(responseCode = "404", description = "스크림/사용자/팀을 찾을 수 없음")
    )
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
