package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.VerificationType
import com.lumiaops.lumiacore.repository.EmailVerificationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
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
    private val authService: AuthService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val passwordEncoder = BCryptPasswordEncoder()

    /**
     * 비밀번호 찾기 요청 (이메일 인증 발송)
     * @return 이메일 발송 성공 여부
     */
    @Transactional
    fun requestPasswordReset(email: String): Boolean {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("등록되지 않은 이메일입니다")

        return authService.sendVerificationEmail(email, VerificationType.PASSWORD_RESET)
    }

    /**
     * 비밀번호 재설정 (토큰 검증 후)
     */
    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val verification = emailVerificationRepository.findByToken(token)
            ?: throw IllegalArgumentException("유효하지 않은 인증 토큰입니다")

        if (!verification.canVerify()) {
            if (verification.isExpired()) {
                throw IllegalArgumentException("만료된 인증 토큰입니다")
            }
            throw IllegalArgumentException("이미 사용된 인증 토큰입니다")
        }

        // 비밀번호 유효성 검사
        validatePassword(newPassword)

        val user = userRepository.findByEmail(verification.email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다")

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
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다") }

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(oldPassword, user.password)) {
            throw IllegalArgumentException("현재 비밀번호가 올바르지 않습니다")
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
            throw IllegalArgumentException("비밀번호는 8자 이상이어야 합니다")
        }
        if (!password.any { it.isDigit() }) {
            throw IllegalArgumentException("비밀번호에 숫자가 포함되어야 합니다")
        }
        if (!password.any { it.isLetter() }) {
            throw IllegalArgumentException("비밀번호에 영문자가 포함되어야 합니다")
        }
    }
}
