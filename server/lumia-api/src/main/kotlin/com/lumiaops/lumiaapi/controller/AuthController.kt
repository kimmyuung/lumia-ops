package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.domain.AccountStatus
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.security.JwtTokenProvider
import com.lumiaops.lumiacore.service.AuthService
import com.lumiaops.lumiacore.service.LoginResult
import com.lumiaops.lumiacore.service.TokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * 인증 관련 REST API 컨트롤러
 */
@Tag(name = "인증", description = "회원가입, 로그인, 토큰 관리 API")
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenService: TokenService
) {

    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 회원가입합니다. 인증 이메일이 발송됩니다.")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "회원가입 성공"),
        ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복 등)")
    )
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            val (user, emailSent) = authService.registerUser(request.email, request.password)
            
            val message = if (emailSent) {
                "회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요."
            } else {
                "회원가입은 완료되었으나 인증 이메일 발송에 실패했습니다. 다시 시도해주세요."
            }
            
            ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse(success = emailSent, message = message))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "회원가입에 실패했습니다"))
        }
    }

    @Operation(summary = "이메일 인증", description = "이메일로 받은 토큰으로 이메일 인증을 완료합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "인증 성공"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
    )
    @PostMapping("/verify-email")
    fun verifyEmail(
        @Valid @RequestBody request: VerifyEmailRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            val user = authService.verifyEmail(request.token)
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "이메일 인증이 완료되었습니다. 로그인하여 닉네임을 설정해주세요."
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "인증에 실패했습니다"))
        }
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. JWT 토큰을 반환합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패"),
        ApiResponse(responseCode = "403", description = "계정 잠금 또는 휴면")
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<Any> {
        return when (val result = authService.login(request.email, request.password)) {
            is LoginResult.Success -> {
                val user = result.user
                val token = jwtTokenProvider.generateAccessToken(user.id!!, user.email ?: "")
                val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)
                
                // 세션 제한 적용 (기존 세션이 최대치면 가장 오래된 것 폐기)
                tokenService.enforceSessionLimit(user.id!!)
                
                // Refresh Token DB에 저장
                tokenService.saveRefreshToken(user.id!!, refreshToken)
                
                ResponseEntity.ok(LoginResponse(
                    token = token,
                    refreshToken = refreshToken,
                    userId = user.id!!,
                    email = user.email,
                    nickname = user.nickname,
                    status = user.status.name,
                    needsNickname = false
                ))
            }
            is LoginResult.NeedsNickname -> {
                val user = result.user
                val token = jwtTokenProvider.generateAccessToken(user.id!!, user.email ?: "")
                val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)
                
                // 세션 제한 적용 (기존 세션이 최대치면 가장 오래된 것 폐기)
                tokenService.enforceSessionLimit(user.id!!)
                
                // Refresh Token DB에 저장
                tokenService.saveRefreshToken(user.id!!, refreshToken)
                
                ResponseEntity.ok(LoginResponse(
                    token = token,
                    refreshToken = refreshToken,
                    userId = user.id!!,
                    email = user.email,
                    nickname = null,
                    status = user.status.name,
                    needsNickname = true,
                    message = "닉네임을 설정해주세요"
                ))
            }
            is LoginResult.Failure -> {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse(success = false, message = result.message))
            }
            is LoginResult.Locked -> {
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(MessageResponse(success = false, message = result.message))
            }
            is LoginResult.Dormant -> {
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(MessageResponse(success = false, message = result.message))
            }
        }
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    )
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: RefreshTokenRequest
    ): ResponseEntity<Any> {
        return try {
            // JWT 형식 검증
            if (!jwtTokenProvider.validateToken(request.refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse(success = false, message = "유효하지 않은 리프레시 토큰입니다"))
            }
            
            if (!jwtTokenProvider.isRefreshToken(request.refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse(success = false, message = "리프레시 토큰이 아닙니다"))
            }
            
            // DB에서 Refresh Token 검증 (서버 저장 확인)
            val storedToken = tokenService.validateRefreshToken(request.refreshToken)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse(success = false, message = "리프레시 토큰이 만료되었거나 폐기되었습니다"))
            
            // 사용자 조회
            val user = authService.findUserById(storedToken.userId)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MessageResponse(success = false, message = "사용자를 찾을 수 없습니다"))
            
            // 기존 Refresh Token 폐기 (Token Rotation)
            tokenService.revokeRefreshToken(request.refreshToken)
            
            // 새 토큰 발급
            val newAccessToken = jwtTokenProvider.generateAccessToken(user.id!!, user.email ?: "")
            val newRefreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)
            
            // 새 Refresh Token DB에 저장
            tokenService.saveRefreshToken(user.id!!, newRefreshToken)
            
            ResponseEntity.ok(TokenResponse(
                token = newAccessToken,
                refreshToken = newRefreshToken
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MessageResponse(success = false, message = "토큰 갱신에 실패했습니다"))
        }
    }

    @Operation(summary = "아이디 찾기", description = "이메일로 가입된 계정이 있는지 확인합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @PostMapping("/find-username")
    fun findUsername(
        @Valid @RequestBody request: FindUsernameRequest
    ): ResponseEntity<FindUsernameResponse> {
        val email = authService.findUsername(request.email)
        return ResponseEntity.ok(FindUsernameResponse(
            email = email,
            exists = email != null
        ))
    }

    @Operation(summary = "인증 이메일 재발송", description = "인증 이메일을 다시 발송합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "발송 성공"),
        ApiResponse(responseCode = "400", description = "발송 실패")
    )
    @PostMapping("/resend-verification")
    fun resendVerification(
        @Valid @RequestBody request: FindUsernameRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            val sent = authService.sendVerificationEmail(
                request.email, 
                com.lumiaops.lumiacore.domain.VerificationType.SIGNUP
            )
            ResponseEntity.ok(MessageResponse(
                success = sent,
                message = if (sent) "인증 이메일을 재발송했습니다" else "이메일 발송에 실패했습니다"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "이메일 발송에 실패했습니다"))
        }
    }

    @Operation(summary = "로그아웃", description = "현재 토큰을 무효화합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        ApiResponse(responseCode = "401", description = "인증 필요")
    )
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal user: User,
        @RequestBody(required = false) request: LogoutRequest?,
        httpRequest: HttpServletRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            // Access Token 블랙리스트에 추가
            val authHeader = httpRequest.getHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val accessToken = authHeader.substring(7)
                // Access Token 만료 시간 추출하여 블랙리스트에 추가
                val expiresAt = LocalDateTime.now().plusHours(1) // 기본 1시간
                tokenService.blacklistAccessToken(accessToken, expiresAt)
            }
            
            // Refresh Token도 폐기 (전달된 경우)
            request?.refreshToken?.let { refreshToken ->
                tokenService.revokeRefreshToken(refreshToken)
            }
            
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "로그아웃되었습니다"
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "로그아웃되었습니다"
            ))
        }
    }
}

