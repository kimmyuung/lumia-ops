package com.lumiaops.lumiacore.domain

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 사용자 엔티티
 * 이메일을 아이디로 사용하며, 인증 상태 및 보안 관련 필드 포함
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_email", columnList = "email", unique = true),
        Index(name = "idx_user_status", columnList = "status")
    ]
)
class User(
    @Column(nullable = false, unique = true)
    val email: String, // 이메일 = 아이디

    @Column(nullable = false)
    var password: String, // BCrypt 암호화된 비밀번호

    @Column(nullable = true)
    var nickname: String? = null, // 첫 로그인 시 설정

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.USER
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AccountStatus = AccountStatus.PENDING_EMAIL
        protected set

    @Column(nullable = false)
    var loginFailCount: Int = 0
        protected set

    var lastLoginAt: LocalDateTime? = null
        protected set

    var nicknameChangedAt: LocalDateTime? = null
        protected set

    var emailVerifiedAt: LocalDateTime? = null
        protected set

    // 이터널 리턴 인게임 닉네임
    @Column(nullable = true)
    var gameNickname: String? = null
        protected set

    // ==================== 비즈니스 메서드 ====================

    /**
     * 이메일 인증 완료
     */
    fun verifyEmail() {
        require(status == AccountStatus.PENDING_EMAIL) { "이메일 인증 대기 상태가 아닙니다" }
        emailVerifiedAt = LocalDateTime.now()
        status = AccountStatus.PENDING_NICKNAME
    }

    /**
     * 닉네임 설정 (첫 설정)
     */
    fun setInitialNickname(newNickname: String) {
        require(status == AccountStatus.PENDING_NICKNAME) { "닉네임 설정 대기 상태가 아닙니다" }
        nickname = newNickname
        nicknameChangedAt = LocalDateTime.now()
        status = AccountStatus.ACTIVE
    }

    /**
     * 닉네임 변경 (30일 제한)
     * @return 변경 성공 여부
     * @throws IllegalStateException 30일 이내 변경 시도 시
     */
    fun updateNickname(newNickname: String) {
        require(status == AccountStatus.ACTIVE) { "활성 상태의 계정만 닉네임을 변경할 수 있습니다" }

        if (nicknameChangedAt != null) {
            val daysSinceLastChange = java.time.temporal.ChronoUnit.DAYS.between(
                nicknameChangedAt, LocalDateTime.now()
            )
            if (daysSinceLastChange < 30) {
                throw IllegalStateException("닉네임은 30일에 1번만 변경할 수 있습니다. 남은 일수: ${30 - daysSinceLastChange}일")
            }
        }

        nickname = newNickname
        nicknameChangedAt = LocalDateTime.now()
    }

    /**
     * 로그인 성공 처리
     */
    fun loginSuccess() {
        loginFailCount = 0
        lastLoginAt = LocalDateTime.now()
    }

    /**
     * 로그인 실패 처리
     * @return 계정이 잠겼는지 여부
     */
    fun loginFailed(): Boolean {
        loginFailCount++
        if (loginFailCount >= 5) {
            status = AccountStatus.LOCKED
            return true
        }
        return false
    }

    /**
     * 계정 잠금 해제
     */
    fun unlock() {
        require(status == AccountStatus.LOCKED) { "잠긴 상태의 계정만 해제할 수 있습니다" }
        loginFailCount = 0
        status = AccountStatus.ACTIVE
    }

    /**
     * 휴면 계정으로 전환
     */
    fun markAsDormant() {
        status = AccountStatus.DORMANT
    }

    /**
     * 휴면 계정 재활성화
     */
    fun reactivate() {
        require(status == AccountStatus.DORMANT) { "휴면 상태의 계정만 재활성화할 수 있습니다" }
        status = AccountStatus.ACTIVE
        loginFailCount = 0
    }

    /**
     * 비밀번호 변경
     */
    fun changePassword(newEncodedPassword: String) {
        password = newEncodedPassword
        // 잠금 또는 휴면 상태였다면 활성화
        if (status == AccountStatus.LOCKED || status == AccountStatus.DORMANT) {
            status = AccountStatus.ACTIVE
            loginFailCount = 0
        }
    }

    /**
     * 6개월 이상 미로그인 여부 확인
     */
    fun isDormantCandidate(): Boolean {
        if (lastLoginAt == null) return false
        val monthsSinceLastLogin = java.time.temporal.ChronoUnit.MONTHS.between(
            lastLoginAt, LocalDateTime.now()
        )
        return monthsSinceLastLogin >= 6
    }

    /**
     * 닉네임 변경까지 남은 일수
     */
    fun daysUntilNicknameChange(): Long {
        if (nicknameChangedAt == null) return 0
        val daysSince = java.time.temporal.ChronoUnit.DAYS.between(
            nicknameChangedAt, LocalDateTime.now()
        )
        return maxOf(0, 30 - daysSince)
    }

    /**
     * 이터널 리턴 인게임 닉네임 업데이트
     */
    fun updateGameNickname(newGameNickname: String?) {
        gameNickname = newGameNickname?.takeIf { it.isNotBlank() }
    }
}

enum class UserRole { USER, ADMIN }