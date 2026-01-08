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
        Index(name = "idx_user_status", columnList = "status"),
        Index(name = "idx_user_steam_id", columnList = "steamId", unique = true),
        Index(name = "idx_user_kakao_id", columnList = "kakaoId", unique = true)
    ]
)
class User(
    @Column(nullable = true, unique = true)
    var email: String? = null, // 이메일 (OAuth 사용자는 없을 수 있음)

    @Column(nullable = true)
    var password: String? = null, // BCrypt 암호화된 비밀번호 (OAuth 사용자는 null)

    @Column(nullable = true)
    var nickname: String? = null, // 첫 로그인 시 설정

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.USER,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val authProvider: AuthProvider = AuthProvider.EMAIL // 인증 제공자
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    // OAuth 관련 필드
    @Column(nullable = true, unique = true)
    var steamId: String? = null  // Steam 64비트 ID
        protected set

    @Column(nullable = true, unique = true)
    var kakaoId: Long? = null  // Kakao 사용자 ID
        protected set

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

    // ==================== OAuth 관련 메서드 ====================

    /**
     * Steam ID 설정 (OAuth 로그인 시)
     */
    fun assignSteamId(id: String) {
        require(authProvider == AuthProvider.STEAM) { "Steam 인증 사용자만 Steam ID를 설정할 수 있습니다" }
        steamId = id
    }

    /**
     * Kakao ID 설정 (OAuth 로그인 시)
     */
    fun assignKakaoId(id: Long) {
        require(authProvider == AuthProvider.KAKAO) { "Kakao 인증 사용자만 Kakao ID를 설정할 수 있습니다" }
        kakaoId = id
    }

    /**
     * OAuth 첫 로그인 완료 처리 (이메일 인증 불필요)
     */
    fun completeOAuthRegistration(userNickname: String, userEmail: String?) {
        require(authProvider != AuthProvider.EMAIL) { "OAuth 사용자만 이 메서드를 사용할 수 있습니다" }
        nickname = userNickname
        email = userEmail
        nicknameChangedAt = LocalDateTime.now()
        status = AccountStatus.ACTIVE
        lastLoginAt = LocalDateTime.now()
    }

    /**
     * OAuth 로그인 성공 처리
     */
    fun loginOAuth() {
        require(authProvider != AuthProvider.EMAIL) { "OAuth 사용자만 이 메서드를 사용할 수 있습니다" }
        lastLoginAt = LocalDateTime.now()
    }

    /**
     * 이메일 업데이트 (OAuth 사용자)
     */
    fun updateEmail(newEmail: String?) {
        email = newEmail?.takeIf { it.isNotBlank() }
    }

    companion object {
        /**
         * Steam OAuth 사용자 생성
         */
        fun createSteamUser(steamId: String, nickname: String, email: String? = null): User {
            return User(
                email = email,
                password = null,
                nickname = nickname,
                authProvider = AuthProvider.STEAM
            ).apply {
                this.steamId = steamId
                this.status = AccountStatus.PENDING_NICKNAME
            }
        }

        /**
         * Kakao OAuth 사용자 생성
         */
        fun createKakaoUser(kakaoId: Long, nickname: String, email: String? = null): User {
            return User(
                email = email,
                password = null,
                nickname = nickname,
                authProvider = AuthProvider.KAKAO
            ).apply {
                this.kakaoId = kakaoId
                this.status = AccountStatus.PENDING_NICKNAME
            }
        }
    }
}

enum class UserRole { USER, ADMIN }