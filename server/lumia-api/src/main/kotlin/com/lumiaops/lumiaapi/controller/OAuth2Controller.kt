package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.security.JwtTokenProvider
import com.lumiaops.lumiacore.service.OAuth2AuthService
import com.lumiaops.lumiacore.service.PlayerStatsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * OAuth2 인증 컨트롤러
 * Steam/Kakao OAuth 로그인 처리
 */
@Tag(name = "OAuth2 인증", description = "Steam/Kakao OAuth 로그인 API")
@RestController
@RequestMapping("/auth/oauth2")
class OAuth2Controller(
    private val oAuth2AuthService: OAuth2AuthService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val playerStatsService: PlayerStatsService
) {

    @Operation(
        summary = "Steam 로그인",
        description = "Steam OpenID 인증 후 콜백 처리. Steam ID와 닉네임으로 로그인/가입합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청")
    )
    @PostMapping("/steam/callback")
    fun steamCallback(
        @RequestBody request: SteamLoginRequest
    ): ResponseEntity<OAuth2LoginResponse> {
        val user = oAuth2AuthService.processingSteamLogin(
            steamId = request.steamId,
            steamNickname = request.steamNickname
        )

        val token = jwtTokenProvider.generateAccessToken(user.id!!, user.email ?: "")
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)

        return ResponseEntity.ok(OAuth2LoginResponse(
            token = token,
            refreshToken = refreshToken,
            userId = user.id!!,
            nickname = user.nickname,
            gameNickname = user.gameNickname,
            needsGameNickname = user.gameNickname == null,
            authProvider = user.authProvider.name
        ))
    }

    @Operation(
        summary = "Kakao 로그인",
        description = "Kakao OAuth2 인증 후 콜백 처리. Kakao ID와 닉네임으로 로그인/가입합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청")
    )
    @PostMapping("/kakao/callback")
    fun kakaoCallback(
        @RequestBody request: KakaoLoginRequest
    ): ResponseEntity<OAuth2LoginResponse> {
        val user = oAuth2AuthService.processingKakaoLogin(
            kakaoId = request.kakaoId,
            kakaoNickname = request.kakaoNickname,
            kakaoEmail = request.kakaoEmail
        )

        val token = jwtTokenProvider.generateAccessToken(user.id!!, user.email ?: "")
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)

        return ResponseEntity.ok(OAuth2LoginResponse(
            token = token,
            refreshToken = refreshToken,
            userId = user.id!!,
            nickname = user.nickname,
            gameNickname = user.gameNickname,
            needsGameNickname = user.gameNickname == null,
            authProvider = user.authProvider.name
        ))
    }

    @Operation(
        summary = "Kakao 로그인 (인가 코드)",
        description = "Kakao 인가 코드를 백엔드에서 처리합니다. 토큰 교환 및 사용자 정보 조회를 서버에서 수행합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 인가 코드")
    )
    @PostMapping("/kakao/code")
    fun kakaoCodeCallback(
        @RequestBody request: KakaoCodeRequest
    ): ResponseEntity<OAuth2LoginResponse> {
        val user = oAuth2AuthService.processingKakaoLoginWithCode(request.code)

        val token = jwtTokenProvider.generateAccessToken(user.id!!, user.email ?: "")
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)

        return ResponseEntity.ok(OAuth2LoginResponse(
            token = token,
            refreshToken = refreshToken,
            userId = user.id!!,
            nickname = user.nickname,
            gameNickname = user.gameNickname,
            needsGameNickname = user.gameNickname == null,
            authProvider = user.authProvider.name
        ))
    }

    @Operation(
        summary = "이터널 리턴 닉네임 설정",
        description = "OAuth 로그인 후 이터널 리턴 인게임 닉네임을 설정합니다. 닉네임이 유효한지 API로 검증합니다."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "설정 성공"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임"),
        ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    )
    @PostMapping("/setup-game-nickname")
    fun setupGameNickname(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody request: SetupGameNicknameRequest
    ): ResponseEntity<Any> {
        // 이터널 리턴 API로 닉네임 검증
        val playerStats = playerStatsService.getPlayerStats(request.gameNickname)
        if (playerStats == null) {
            return ResponseEntity.badRequest().body(mapOf(
                "error" to "INVALID_NICKNAME",
                "message" to "이터널 리턴에서 해당 닉네임을 찾을 수 없습니다: ${request.gameNickname}"
            ))
        }

        val user = oAuth2AuthService.completeOAuthSetup(userId, request.gameNickname)

        return ResponseEntity.ok(mapOf(
            "success" to true,
            "gameNickname" to user.gameNickname,
            "message" to "이터널 리턴 닉네임이 설정되었습니다."
        ))
    }

    @Operation(summary = "OAuth 상태 확인", description = "현재 사용자가 OAuth 사용자인지 확인합니다.")
    @GetMapping("/status")
    fun getOAuthStatus(
        @RequestHeader("X-User-Id") userId: Long
    ): ResponseEntity<Map<String, Boolean>> {
        val isOAuth = oAuth2AuthService.isOAuthUser(userId)
        return ResponseEntity.ok(mapOf("isOAuthUser" to isOAuth))
    }
}
