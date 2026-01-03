package com.lumiaops.lumiacore.exception

/**
 * 도메인 예외 기본 클래스
 * 모든 비즈니스 예외의 상위 클래스
 */
sealed class DomainException(
    override val message: String,
    val errorCode: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 리소스를 찾을 수 없음 (404)
 */
class NotFoundException(
    message: String,
    errorCode: String = "NOT_FOUND",
    cause: Throwable? = null
) : DomainException(message, errorCode, cause) {
    
    companion object {
        fun user(userId: Long) = NotFoundException("사용자를 찾을 수 없습니다: $userId", "USER_NOT_FOUND")
        fun userByEmail(email: String) = NotFoundException("사용자를 찾을 수 없습니다: $email", "USER_NOT_FOUND")
        fun team(teamId: Long) = NotFoundException("팀을 찾을 수 없습니다: $teamId", "TEAM_NOT_FOUND")
        fun teamMember() = NotFoundException("팀 멤버를 찾을 수 없습니다", "TEAM_MEMBER_NOT_FOUND")
        fun strategy(strategyId: Long) = NotFoundException("전략을 찾을 수 없습니다: $strategyId", "STRATEGY_NOT_FOUND")
        fun scrim(scrimId: Long) = NotFoundException("스크림을 찾을 수 없습니다: $scrimId", "SCRIM_NOT_FOUND")
        fun match(matchId: Long) = NotFoundException("매치를 찾을 수 없습니다: $matchId", "MATCH_NOT_FOUND")
        fun matchResult(resultId: Long) = NotFoundException("결과를 찾을 수 없습니다: $resultId", "MATCH_RESULT_NOT_FOUND")
        fun invitation() = NotFoundException("유효하지 않은 초대입니다", "INVITATION_NOT_FOUND")
        fun verificationToken() = NotFoundException("유효하지 않은 인증 토큰입니다", "TOKEN_NOT_FOUND")
    }
}

/**
 * 리소스 중복 (409)
 */
class DuplicateException(
    message: String,
    errorCode: String = "DUPLICATE",
    cause: Throwable? = null
) : DomainException(message, errorCode, cause) {
    
    companion object {
        fun email(email: String) = DuplicateException("이미 존재하는 이메일입니다: $email", "DUPLICATE_EMAIL")
        fun teamMember() = DuplicateException("이미 팀의 멤버입니다", "DUPLICATE_TEAM_MEMBER")
        fun invitation() = DuplicateException("이미 해당 이메일로 대기 중인 초대가 있습니다", "DUPLICATE_INVITATION")
    }
}

/**
 * 잘못된 상태 (400)
 */
class InvalidStateException(
    message: String,
    errorCode: String = "INVALID_STATE",
    cause: Throwable? = null
) : DomainException(message, errorCode, cause) {
    
    companion object {
        fun tokenExpired() = InvalidStateException("만료된 인증 토큰입니다", "TOKEN_EXPIRED")
        fun tokenUsed() = InvalidStateException("이미 사용된 인증 토큰입니다", "TOKEN_USED")
        fun invitationExpired() = InvalidStateException("만료된 초대입니다", "INVITATION_EXPIRED")
        fun invitationNotAcceptable() = InvalidStateException("수락할 수 없는 초대입니다", "INVITATION_NOT_ACCEPTABLE")
        fun invitationNotPending() = InvalidStateException("대기 중인 초대만 재발송할 수 있습니다", "INVITATION_NOT_PENDING")
        fun invitationExpiredResend() = InvalidStateException("만료된 초대는 재발송할 수 없습니다", "INVITATION_EXPIRED_RESEND")
        fun nicknameNotPending() = InvalidStateException("닉네임 설정 대기 상태가 아닙니다", "NICKNAME_NOT_PENDING")
        fun emailNotPending() = InvalidStateException("이메일 인증 대기 상태가 아닙니다", "EMAIL_NOT_PENDING")
        fun notActive() = InvalidStateException("활성 상태의 계정만 이용 가능합니다", "ACCOUNT_NOT_ACTIVE")
    }
}

/**
 * 유효성 검증 실패 (422)
 */
class ValidationException(
    message: String,
    errorCode: String = "VALIDATION_FAILED",
    val field: String? = null,
    cause: Throwable? = null
) : DomainException(message, errorCode, cause) {
    
    companion object {
        fun passwordTooShort() = ValidationException("비밀번호는 8자 이상이어야 합니다", "PASSWORD_TOO_SHORT", "password")
        fun passwordNoNumber() = ValidationException("비밀번호에 숫자가 포함되어야 합니다", "PASSWORD_NO_NUMBER", "password")
        fun passwordNoLetter() = ValidationException("비밀번호에 영문자가 포함되어야 합니다", "PASSWORD_NO_LETTER", "password")
        fun passwordIncorrect() = ValidationException("현재 비밀번호가 올바르지 않습니다", "PASSWORD_INCORRECT", "password")
        fun emailMismatch() = ValidationException("초대받은 이메일과 일치하지 않습니다", "EMAIL_MISMATCH", "email")
    }
}

/**
 * 인증 실패 (401)
 */
class AuthenticationException(
    message: String,
    errorCode: String = "AUTHENTICATION_FAILED",
    cause: Throwable? = null
) : DomainException(message, errorCode, cause) {
    
    companion object {
        fun invalidCredentials() = AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다", "INVALID_CREDENTIALS")
        fun accountLocked() = AuthenticationException("계정이 잠겼습니다. 비밀번호를 재설정해주세요.", "ACCOUNT_LOCKED")
        fun accountDormant() = AuthenticationException("휴면 계정입니다. 계정을 활성화해주세요.", "ACCOUNT_DORMANT")
        fun tokenInvalid() = AuthenticationException("유효하지 않은 토큰입니다", "TOKEN_INVALID")
    }
}

/**
 * 권한 부족 (403)
 */
class ForbiddenException(
    message: String,
    errorCode: String = "FORBIDDEN",
    cause: Throwable? = null
) : DomainException(message, errorCode, cause) {
    
    companion object {
        fun notTeamOwner() = ForbiddenException("팀 소유자만 이 작업을 수행할 수 있습니다", "NOT_TEAM_OWNER")
        fun notTeamMember() = ForbiddenException("팀 멤버만 이 작업을 수행할 수 있습니다", "NOT_TEAM_MEMBER")
    }
}
