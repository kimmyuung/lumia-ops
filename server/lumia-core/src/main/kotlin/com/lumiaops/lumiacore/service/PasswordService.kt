package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.VerificationType
import com.lumiaops.lumiacore.exception.*
import com.lumiaops.lumiacore.repository.EmailVerificationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 비밀번호 관련 서비스
 * 비밀번호 찾기, 재설정, 변경
 */
@Service
@Transactional(readOnly = true)
class PasswordService(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val emailService: EmailService,
    private val authService: AuthService,
    private val passwordEncoder: PasswordEncoder
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 비밀번호 찾기 요청 (이메일 인증 발송)
     * @return 이메일 발송 성공 여부
     */
    @Transactional
    fun requestPasswordReset(email: String): Boolean {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundException.userByEmail(email)

        return authService.sendVerificationEmail(email, VerificationType.PASSWORD_RESET)
    }

    /**
     * 비밀번호 재설정 (토큰 검증 후)
     */
    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val verification = emailVerificationRepository.findByToken(token)
            ?: throw NotFoundException.verificationToken()

        if (!verification.canVerify()) {
            if (verification.isExpired()) {
                throw InvalidStateException.tokenExpired()
            }
            throw InvalidStateException.tokenUsed()
        }

        // 비밀번호 유효성 검사
        validatePassword(newPassword)

        val user = userRepository.findByEmail(verification.email)
            ?: throw NotFoundException.userByEmail(verification.email)

        // 인증 완료 처리
        verification.verify()

        // 비밀번호 변경 및 계정 상태 복구
        val encodedPassword = passwordEncoder.encode(newPassword)
        user.changePassword(encodedPassword)

        log.info("비밀번호 재설정 완료: email=${verification.email}")
    }

    /**
     * 비밀번호 변경 (로그인 상태에서)
     */
    @Transactional
    fun changePassword(userId: Long, oldPassword: String, newPassword: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { NotFoundException.user(userId) }

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(oldPassword, user.password)) {
            throw ValidationException.passwordIncorrect()
        }

        // 새 비밀번호 유효성 검사
        validatePassword(newPassword)

        // 비밀번호 변경
        val encodedPassword = passwordEncoder.encode(newPassword)
        user.changePassword(encodedPassword)

        log.info("비밀번호 변경 완료: userId=$userId")
    }

    /**
     * 비밀번호 유효성 검사
     */
    private fun validatePassword(password: String) {
        if (password.length < 8) {
            throw ValidationException.passwordTooShort()
        }
        if (!password.any { it.isDigit() }) {
            throw ValidationException.passwordNoNumber()
        }
        if (!password.any { it.isLetter() }) {
            throw ValidationException.passwordNoLetter()
        }
    }
}
