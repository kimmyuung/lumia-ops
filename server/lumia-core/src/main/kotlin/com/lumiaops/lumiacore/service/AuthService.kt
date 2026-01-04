package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.*
import com.lumiaops.lumiacore.repository.EmailVerificationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 인증 관련 서비스
 * 회원가입, 이메일 인증, 로그인, 아이디 찾기 등
 */
@Service
@Transactional(readOnly = true)
class AuthService(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val emailService: EmailService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val passwordEncoder = BCryptPasswordEncoder()

    /**
     * 회원가입 - 이메일 인증 발송
     * @return Pair<User, Boolean> - 생성된 사용자와 이메일 발송 성공 여부
     */
    @Transactional
    fun registerUser(email: String, password: String): Pair<User, Boolean> {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다: $email")
        }

        // 비밀번호 유효성 검사
        validatePassword(password)

        // 사용자 생성 (상태: PENDING_EMAIL)
        val encodedPassword = passwordEncoder.encode(password)
        val user = userRepository.save(
            User(email = email, password = encodedPassword)
        )

        // 이메일 인증 토큰 생성 및 발송
        val emailSent = sendVerificationEmail(email, VerificationType.SIGNUP)

        log.info("회원가입 요청: email=$email, userId=${user.id}")
        return Pair(user, emailSent)
    }

    /**
     * 이메일 인증 완료
     */
    @Transactional
    fun verifyEmail(token: String): User {
        val verification = emailVerificationRepository.findByToken(token)
            ?: throw IllegalArgumentException("유효하지 않은 인증 토큰입니다")

        if (!verification.canVerify()) {
            if (verification.isExpired()) {
                throw IllegalArgumentException("만료된 인증 토큰입니다")
            }
            throw IllegalArgumentException("이미 사용된 인증 토큰입니다")
        }

        verification.verify()

        val user = userRepository.findByEmail(verification.email)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다")

        when (verification.type) {
            VerificationType.SIGNUP -> user.verifyEmail()
            VerificationType.PASSWORD_RESET,
            VerificationType.UNLOCK_ACCOUNT,
            VerificationType.DORMANT_REACTIVATION -> {
                // 비밀번호 재설정 페이지로 이동 가능하도록 처리
            }
        }

        log.info("이메일 인증 완료: email=${verification.email}, type=${verification.type}")
        return user
    }

    /**
     * 로그인
     */
    @Transactional
    fun login(email: String, password: String): LoginResult {
        val user = userRepository.findByEmail(email)
            ?: return LoginResult.Failure("이메일 또는 비밀번호가 올바르지 않습니다")

        // 계정 상태 확인
        when (user.status) {
            AccountStatus.PENDING_EMAIL -> 
                return LoginResult.Failure("이메일 인증이 필요합니다")
            AccountStatus.PENDING_NICKNAME ->
                return LoginResult.NeedsNickname(user)
            AccountStatus.LOCKED -> 
                return LoginResult.Locked("계정이 잠겼습니다. 이메일 인증 후 비밀번호를 재설정해주세요")
            AccountStatus.DORMANT -> 
                return LoginResult.Dormant("휴면 계정입니다. 이메일 인증 후 비밀번호를 재설정해주세요")
            AccountStatus.ACTIVE -> { /* 진행 */ }
        }

        // 6개월 이상 미로그인 체크
        if (user.isDormantCandidate()) {
            user.markAsDormant()
            sendVerificationEmail(email, VerificationType.DORMANT_REACTIVATION)
            return LoginResult.Dormant("6개월 이상 로그인하지 않아 휴면 계정으로 전환되었습니다. 이메일을 확인해주세요")
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.password)) {
            val locked = user.loginFailed()
            if (locked) {
                sendVerificationEmail(email, VerificationType.UNLOCK_ACCOUNT)
                return LoginResult.Locked("비밀번호 5회 오류로 계정이 잠겼습니다. 이메일을 확인해주세요")
            }
            return LoginResult.Failure("이메일 또는 비밀번호가 올바르지 않습니다 (${5 - user.loginFailCount}회 남음)")
        }

        // 로그인 성공
        user.loginSuccess()
        log.info("로그인 성공: email=$email, userId=${user.id}")
        return LoginResult.Success(user)
    }

    /**
     * 아이디 찾기 (인증 없이 이메일 존재 여부만 확인)
     */
    fun findUsername(email: String): String? {
        val user = userRepository.findByEmail(email)
        return user?.email // 이메일 = 아이디
    }

    /**
     * ID로 사용자 조회 (토큰 갱신용)
     */
    fun findUserById(userId: Long): User? {
        return userRepository.findById(userId).orElse(null)
    }

    /**
     * 인증 이메일 발송
     */
    @Transactional
    fun sendVerificationEmail(email: String, type: VerificationType): Boolean {
        // 기존 미사용 토큰 삭제
        emailVerificationRepository.deleteByEmailAndTypeAndVerifiedFalse(email, type)

        // 새 토큰 생성
        val verification = emailVerificationRepository.save(
            EmailVerification(email = email, type = type)
        )

        // 이메일 발송
        return emailService.sendVerificationEmail(verification)
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

/**
 * 로그인 결과
 */
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class NeedsNickname(val user: User) : LoginResult()
    data class Failure(val message: String) : LoginResult()
    data class Locked(val message: String) : LoginResult()
    data class Dormant(val message: String) : LoginResult()
}
