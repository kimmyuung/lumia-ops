package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.AuthProvider
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.external.KakaoApiClient
import com.lumiaops.lumiacore.external.KakaoUserInfo
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * OAuth2 인증 서비스
 * Steam/Kakao OAuth 로그인 처리
 * - 신규 사용자: OAuth 계정 생성
 * - 기존 OAuth 사용자: 로그인 처리
 * - 동일 이메일 존재 시: 기존 계정에 OAuth 연동 (비밀번호 + OAuth 모두 사용 가능)
 */
@Service
@Transactional
class OAuth2AuthService(
    private val userRepository: UserRepository,
    private val kakaoApiClient: KakaoApiClient
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Steam 로그인 처리
     * 1. Steam ID로 기존 사용자 조회 → 있으면 로그인
     * 2. 없으면 신규 사용자 생성
     */
    fun processingSteamLogin(steamId: String, steamNickname: String): User {
        // 1. Steam ID로 기존 사용자 조회
        val existingBySteamId = userRepository.findBySteamId(steamId)
        if (existingBySteamId != null) {
            log.info("Steam 기존 사용자 로그인: steamId=$steamId, nickname=${existingBySteamId.nickname}")
            existingBySteamId.loginOAuth()
            return existingBySteamId
        }

        // 2. 신규 사용자 생성
        log.info("Steam 신규 사용자 등록: steamId=$steamId, steamNickname=$steamNickname")
        val newUser = User.createSteamUser(steamId, steamNickname)
        return userRepository.save(newUser)
    }

    /**
     * Kakao 인가 코드로 로그인 처리 (백엔드에서 토큰 교환)
     */
    fun processingKakaoLoginWithCode(code: String): User {
        val kakaoUserInfo = kakaoApiClient.processKakaoLogin(code)
            ?: throw IllegalArgumentException("Kakao 로그인 처리에 실패했습니다. 인가 코드가 유효하지 않습니다.")
        
        return processingKakaoLogin(
            kakaoId = kakaoUserInfo.kakaoId,
            kakaoNickname = kakaoUserInfo.nickname,
            kakaoEmail = kakaoUserInfo.email
        )
    }

    /**
     * Kakao 로그인 처리
     * 1. Kakao ID로 기존 사용자 조회 → 있으면 로그인
     * 2. 이메일로 기존 이메일 사용자 조회 → 있으면 Kakao 연동
     * 3. 없으면 신규 사용자 생성
     */
    fun processingKakaoLogin(kakaoId: Long, kakaoNickname: String, kakaoEmail: String? = null): User {
        // 1. Kakao ID로 기존 사용자 조회
        val existingByKakaoId = userRepository.findByKakaoId(kakaoId)
        if (existingByKakaoId != null) {
            log.info("Kakao 기존 사용자 로그인: kakaoId=$kakaoId, nickname=${existingByKakaoId.nickname}")
            existingByKakaoId.loginOAuth()
            // 이메일 업데이트 (없었으면 추가)
            if (existingByKakaoId.email == null && kakaoEmail != null) {
                existingByKakaoId.updateEmail(kakaoEmail)
            }
            return existingByKakaoId
        }

        // 2. 이메일로 기존 사용자 조회 (동일 이메일 시 연동)
        if (!kakaoEmail.isNullOrBlank()) {
            val existingByEmail = userRepository.findByEmail(kakaoEmail)
            if (existingByEmail != null) {
                log.info("기존 이메일 계정에 Kakao 연동: email=$kakaoEmail, kakaoId=$kakaoId")
                existingByEmail.linkKakaoAccount(kakaoId)
                existingByEmail.loginOAuth()
                return existingByEmail
            }
        }

        // 3. 신규 사용자 생성
        log.info("Kakao 신규 사용자 등록: kakaoId=$kakaoId, kakaoNickname=$kakaoNickname")
        val newUser = User.createKakaoUser(kakaoId, kakaoNickname, kakaoEmail)
        return userRepository.save(newUser)
    }

    /**
     * Steam 로그인 처리 (이메일 연동 포함)
     * 동일 이메일이 있으면 기존 계정에 Steam 연동
     */
    fun processingSteamLoginWithEmail(steamId: String, steamNickname: String, email: String?): User {
        // 1. Steam ID로 기존 사용자 조회
        val existingBySteamId = userRepository.findBySteamId(steamId)
        if (existingBySteamId != null) {
            log.info("Steam 기존 사용자 로그인: steamId=$steamId")
            existingBySteamId.loginOAuth()
            return existingBySteamId
        }

        // 2. 이메일로 기존 사용자 조회 (동일 이메일 시 연동)
        if (!email.isNullOrBlank()) {
            val existingByEmail = userRepository.findByEmail(email)
            if (existingByEmail != null) {
                log.info("기존 이메일 계정에 Steam 연동: email=$email, steamId=$steamId")
                existingByEmail.linkSteamAccount(steamId)
                existingByEmail.loginOAuth()
                return existingByEmail
            }
        }

        // 3. 신규 사용자 생성
        log.info("Steam 신규 사용자 등록: steamId=$steamId, steamNickname=$steamNickname")
        val newUser = User.createSteamUser(steamId, steamNickname, email)
        return userRepository.save(newUser)
    }

    /**
     * OAuth 사용자 이터널 리턴 닉네임 설정 (첫 로그인 완료)
     */
    fun completeOAuthSetup(userId: Long, gameNickname: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        
        // OAuth 연동 사용자도 허용 (hasLinkedOAuth)
        require(user.authProvider != AuthProvider.EMAIL || user.hasLinkedOAuth()) { 
            "OAuth 사용자만 이 기능을 사용할 수 있습니다" 
        }
        
        user.updateGameNickname(gameNickname)
        user.completeOAuthRegistration(user.nickname ?: "User", user.email)
        
        log.info("OAuth 설정 완료: userId=$userId, gameNickname=$gameNickname")
        return user
    }

    /**
     * OAuth 사용자 여부 확인 (연동 포함)
     */
    fun isOAuthUser(userId: Long): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        return user.authProvider != AuthProvider.EMAIL || user.hasLinkedOAuth()
    }

    /**
     * Steam ID로 사용자 조회
     */
    fun findBySteamId(steamId: String): User? = userRepository.findBySteamId(steamId)

    /**
     * Kakao ID로 사용자 조회
     */
    fun findByKakaoId(kakaoId: Long): User? = userRepository.findByKakaoId(kakaoId)
}
