package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.service.PlayerStatsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 플레이어 통계 API 컨트롤러
 * 이터널 리턴 전적/실험체 정보 조회
 */
@Tag(name = "플레이어 통계", description = "이터널 리턴 플레이어 전적/실험체 정보 조회 API")
@RestController
@RequestMapping("/player-stats")
class PlayerStatsController(
    private val playerStatsService: PlayerStatsService
) {

    @Operation(
        summary = "플레이어 종합 통계 조회",
        description = "이터널 리턴 닉네임으로 플레이어의 시즌 통계를 조회합니다. (승률, Top4 비율, 평균 순위 등)"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "플레이어를 찾을 수 없음"),
        ApiResponse(responseCode = "503", description = "API 키가 설정되지 않음")
    )
    @GetMapping("/{nickname}")
    fun getPlayerStats(
        @Parameter(description = "이터널 리턴 인게임 닉네임") 
        @PathVariable nickname: String,
        @Parameter(description = "시즌 ID (미지정 시 현재 시즌)") 
        @RequestParam(required = false) seasonId: Int?,
        @Parameter(description = "팀 모드 (1=솔로, 2=듀오, 3=스쿼드)") 
        @RequestParam(required = false) teamMode: Int?
    ): ResponseEntity<PlayerStatsResponse> {
        if (!playerStatsService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }

        val stats = playerStatsService.getPlayerStats(nickname, seasonId, teamMode)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(PlayerStatsResponse.from(stats))
    }

    @Operation(
        summary = "많이 사용한 실험체 조회",
        description = "플레이어가 많이 사용한 실험체 Top N을 조회합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "플레이어를 찾을 수 없음"),
        ApiResponse(responseCode = "503", description = "API 키가 설정되지 않음")
    )
    @GetMapping("/{nickname}/characters")
    fun getTopCharacters(
        @Parameter(description = "이터널 리턴 인게임 닉네임") 
        @PathVariable nickname: String,
        @Parameter(description = "조회할 실험체 수 (기본: 5)") 
        @RequestParam(defaultValue = "5") limit: Int,
        @Parameter(description = "시즌 ID (미지정 시 현재 시즌)") 
        @RequestParam(required = false) seasonId: Int?
    ): ResponseEntity<List<CharacterStatsResponse>> {
        if (!playerStatsService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }

        val characters = playerStatsService.getTopCharacters(nickname, limit, seasonId)
        if (characters.isEmpty()) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(characters.map { CharacterStatsResponse.from(it) })
    }

    @Operation(
        summary = "최근 게임 기록 조회",
        description = "플레이어의 최근 게임 기록을 조회합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "404", description = "플레이어를 찾을 수 없음"),
        ApiResponse(responseCode = "503", description = "API 키가 설정되지 않음")
    )
    @GetMapping("/{nickname}/games")
    fun getRecentGames(
        @Parameter(description = "이터널 리턴 인게임 닉네임") 
        @PathVariable nickname: String,
        @Parameter(description = "조회할 게임 수 (기본: 10)") 
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<GameRecordResponse>> {
        if (!playerStatsService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }

        val games = playerStatsService.getRecentGames(nickname, limit)
        if (games.isEmpty()) {
            return ResponseEntity.notFound().build()
        }

        return ResponseEntity.ok(games.map { GameRecordResponse.from(it) })
    }

    @Operation(
        summary = "API 설정 상태 확인",
        description = "이터널 리턴 API가 설정되어 있는지 확인합니다."
    )
    @GetMapping("/status")
    fun getApiStatus(): ResponseEntity<Map<String, Boolean>> {
        return ResponseEntity.ok(mapOf("configured" to playerStatsService.isConfigured()))
    }
}
