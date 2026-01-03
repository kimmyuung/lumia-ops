package com.lumiaops.lumiaapi.controller

import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.domain.AccountStatus
import com.lumiaops.lumiacore.security.JwtTokenProvider
import com.lumiaops.lumiacore.service.AuthService
import com.lumiaops.lumiacore.service.LoginResult
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 인증 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * 회원가입
     * POST /api/auth/register
     */
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

    /**
     * 이메일 인증
     * POST /api/auth/verify-email
     */
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

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest
    ): ResponseEntity<Any> {
        return when (val result = authService.login(request.email, request.password)) {
            is LoginResult.Success -> {
                val user = result.user
                val token = jwtTokenProvider.generateAccessToken(user.id!!, user.email)
                ResponseEntity.ok(LoginResponse(
                    token = token,
                    userId = user.id!!,
                    email = user.email,
                    nickname = user.nickname,
                    status = user.status.name,
                    needsNickname = false
                ))
            }
            is LoginResult.NeedsNickname -> {
                val user = result.user
                val token = jwtTokenProvider.generateAccessToken(user.id!!, user.email)
                ResponseEntity.ok(LoginResponse(
                    token = token,
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

    /**
     * 아이디 찾기 (인증 없이)
     * POST /api/auth/find-username
     */
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

    /**
     * 인증 이메일 재발송
     * POST /api/auth/resend-verification
     */
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
}
