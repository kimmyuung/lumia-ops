package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.service.MetaAnalysisService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 메타 분석 API 컨트롤러
 * 상위 랭커 분석, 메타 실험체 조회, 조합 추천
 */
@Tag(name = "메타 분석", description = "상위 랭커 분석 및 실험체 추천 API")
@RestController
@RequestMapping("/meta")
class MetaController(
    private val metaAnalysisService: MetaAnalysisService
) {

    @Operation(
        summary = "메타 실험체 조회",
        description = "상위 랭커들이 많이 사용하는 실험체 TOP N을 조회합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "503", description = "API 키가 설정되지 않음")
    )
    @GetMapping("/top-characters")
    fun getMetaCharacters(
        @Parameter(description = "시즌 ID") 
        @RequestParam seasonId: Int,
        @Parameter(description = "팀 모드 (1=솔로, 2=듀오, 3=스쿼드)") 
        @RequestParam(defaultValue = "3") teamMode: Int,
        @Parameter(description = "조회할 실험체 수") 
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<MetaCharacterResponse>> {
        if (!metaAnalysisService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }

        val characters = metaAnalysisService.getMetaCharacters(seasonId, teamMode, limit)
        return ResponseEntity.ok(characters.map { MetaCharacterResponse.from(it) })
    }

    @Operation(
        summary = "상위 랭커 목록 조회",
        description = "시즌별 상위 랭커 목록을 조회합니다. 각 랭커의 주력 실험체 정보를 포함합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "조회 성공"),
        ApiResponse(responseCode = "503", description = "API 키가 설정되지 않음")
    )
    @GetMapping("/top-rankers")
    fun getTopRankers(
        @Parameter(description = "시즌 ID") 
        @RequestParam seasonId: Int,
        @Parameter(description = "팀 모드 (1=솔로, 2=듀오, 3=스쿼드)") 
        @RequestParam(defaultValue = "3") teamMode: Int,
        @Parameter(description = "조회할 랭커 수") 
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<TopRankerResponse>> {
        if (!metaAnalysisService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }

        val rankers = metaAnalysisService.getTopRankers(seasonId, teamMode, limit)
        return ResponseEntity.ok(rankers.map { TopRankerResponse.from(it) })
    }

    @Operation(
        summary = "팀 조합 추천",
        description = "팀원들의 게임 닉네임을 기반으로 최적의 실험체 조합을 추천합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "추천 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청 (팀원 닉네임 필요)"),
        ApiResponse(responseCode = "503", description = "API 키가 설정되지 않음")
    )
    @GetMapping("/recommend")
    fun recommendComposition(
        @Parameter(description = "팀원들의 게임 닉네임 (쉼표로 구분)") 
        @RequestParam members: String,
        @Parameter(description = "시즌 ID (선택)") 
        @RequestParam(required = false) seasonId: Int?
    ): ResponseEntity<List<CompositionRecommendationResponse>> {
        if (!metaAnalysisService.isConfigured()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }

        val memberList = members.split(",").map { it.trim() }.filter { it.isNotBlank() }
        if (memberList.isEmpty()) {
            return ResponseEntity.badRequest().build()
        }

        val recommendations = metaAnalysisService.recommendComposition(memberList, seasonId)
        return ResponseEntity.ok(recommendations.map { CompositionRecommendationResponse.from(it) })
    }

    @Operation(
        summary = "API 설정 상태 확인",
        description = "이터널 리턴 API가 설정되어 있는지 확인합니다."
    )
    @GetMapping("/status")
    fun getApiStatus(): ResponseEntity<Map<String, Boolean>> {
        return ResponseEntity.ok(mapOf("configured" to metaAnalysisService.isConfigured()))
    }
}
