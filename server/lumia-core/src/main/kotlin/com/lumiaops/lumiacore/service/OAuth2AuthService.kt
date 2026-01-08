package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.AuthProvider
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * OAuth2 인증 서비스
 * Steam/Kakao OAuth 로그인 처리
 */
@Service
@Transactional
class OAuth2AuthService(
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Steam 로그인 처리
     * @param steamId Steam 64비트 ID
     * @param steamNickname Steam 닉네임 (프로필 이름)
     * @return 사용자 정보 (신규 or 기존)
     */
    fun processingSteamLogin(steamId: String, steamNickname: String): User {
        val existingUser = userRepository.findBySteamId(steamId)
        
        return if (existingUser != null) {
            log.info("Steam 기존 사용자 로그인: steamId=$steamId, nickname=${existingUser.nickname}")
            existingUser.loginOAuth()
            existingUser
        } else {
            log.info("Steam 신규 사용자 등록: steamId=$steamId, steamNickname=$steamNickname")
            val newUser = User.createSteamUser(steamId, steamNickname)
            userRepository.save(newUser)
        }
    }

    /**
     * Kakao 로그인 처리
     * @param kakaoId Kakao 사용자 ID
     * @param kakaoNickname Kakao 닉네임
     * @param kakaoEmail Kakao 이메일 (선택)
     * @return 사용자 정보 (신규 or 기존)
     */
    fun processingKakaoLogin(kakaoId: Long, kakaoNickname: String, kakaoEmail: String? = null): User {
        val existingUser = userRepository.findByKakaoId(kakaoId)
        
        return if (existingUser != null) {
            log.info("Kakao 기존 사용자 로그인: kakaoId=$kakaoId, nickname=${existingUser.nickname}")
            existingUser.loginOAuth()
            // 이메일 업데이트 (없었으면 추가)
            if (existingUser.email == null && kakaoEmail != null) {
                existingUser.updateEmail(kakaoEmail)
            }
            existingUser
        } else {
            log.info("Kakao 신규 사용자 등록: kakaoId=$kakaoId, kakaoNickname=$kakaoNickname")
            val newUser = User.createKakaoUser(kakaoId, kakaoNickname, kakaoEmail)
            userRepository.save(newUser)
        }
    }

    /**
     * OAuth 사용자 이터널 리턴 닉네임 설정 (첫 로그인 완료)
     */
    fun completeOAuthSetup(userId: Long, gameNickname: String): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }
        
        require(user.authProvider != AuthProvider.EMAIL) { 
            "OAuth 사용자만 이 기능을 사용할 수 있습니다" 
        }
        
        user.updateGameNickname(gameNickname)
        user.completeOAuthRegistration(user.nickname ?: "User", user.email)
        
        log.info("OAuth 설정 완료: userId=$userId, gameNickname=$gameNickname")
        return user
    }

    /**
     * OAuth 사용자 여부 확인
     */
    fun isOAuthUser(userId: Long): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        return user.authProvider != AuthProvider.EMAIL
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
