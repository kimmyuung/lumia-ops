package com.lumiaops.lumiaapi.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 회원가입 요청 DTO
 */
data class RegisterRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    val password: String
)

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    val password: String
)

/**
 * 로그인 응답 DTO
 */
data class LoginResponse(
    val token: String,  // JWT 액세스 토큰
    val refreshToken: String,  // JWT 리프레시 토큰
    val userId: Long,
    val email: String,
    val nickname: String?,
    val status: String,
    val needsNickname: Boolean,
    val message: String? = null
)

/**
 * 이메일 인증 요청 DTO
 */
data class VerifyEmailRequest(
    @field:NotBlank(message = "토큰은 필수입니다")
    val token: String
)

/**
 * 아이디 찾기 요청 DTO
 */
data class FindUsernameRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String
)

/**
 * 아이디 찾기 응답 DTO
 */
data class FindUsernameResponse(
    val email: String?,
    val exists: Boolean
)

/**
 * 비밀번호 재설정 요청 DTO
 */
data class PasswordResetRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String
)

/**
 * 비밀번호 재설정 실행 DTO
 */
data class ResetPasswordRequest(
    @field:NotBlank(message = "토큰은 필수입니다")
    val token: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    val newPassword: String
)

/**
 * 비밀번호 변경 DTO
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다")
    val oldPassword: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    @field:Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    val newPassword: String
)

/**
 * 닉네임 변경 DTO
 */
data class UpdateNicknameRequest(
    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = 2, max = 20, message = "닉네임은 2-20자 사이여야 합니다")
    val nickname: String
)

/**
 * 사용자 정보 응답 DTO
 */
data class UserResponse(
    val id: Long,
    val email: String,
    val nickname: String?,
    val status: String,
    val daysUntilNicknameChange: Long,
    val gameNickname: String? = null
)

/**
 * 일반 응답 DTO
 */
data class MessageResponse(
    val success: Boolean,
    val message: String
)

/**
 * 토큰 갱신 요청 DTO
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "리프레시 토큰은 필수입니다")
    val refreshToken: String
)

/**
 * 토큰 갱신 응답 DTO
 */
data class TokenResponse(
    val token: String,
    val refreshToken: String
)

/**
 * 로그아웃 요청 DTO
 */
data class LogoutRequest(
    val refreshToken: String? = null  // optional - 리프레시 토큰도 함께 무효화
)
