package com.lumiaops.lumiacore.external

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * Kakao OAuth2 API 클라이언트
 * 인가 코드 → 토큰 교환 및 사용자 정보 조회
 */
@Component
class KakaoApiClient(
    @Value("\${oauth.kakao.client-id:}") private val clientId: String,
    @Value("\${oauth.kakao.client-secret:}") private val clientSecret: String,
    @Value("\${oauth.kakao.redirect-uri:http://localhost:5173/auth/oauth2/kakao/callback}") private val redirectUri: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val restTemplate = RestTemplate()

    companion object {
        private const val TOKEN_URL = "https://kauth.kakao.com/oauth/token"
        private const val USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
    }

    /**
     * 인가 코드로 액세스 토큰 교환
     */
    fun exchangeToken(code: String): KakaoTokenResponse? {
        try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }

            val params = LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "authorization_code")
                add("client_id", clientId)
                add("redirect_uri", redirectUri)
                add("code", code)
                if (clientSecret.isNotBlank()) {
                    add("client_secret", clientSecret)
                }
            }

            val request = HttpEntity(params, headers)
            val response = restTemplate.postForEntity(TOKEN_URL, request, KakaoTokenResponse::class.java)

            log.info("Kakao 토큰 교환 성공")
            return response.body
        } catch (e: Exception) {
            log.error("Kakao 토큰 교환 실패: ${e.message}", e)
            return null
        }
    }

    /**
     * 액세스 토큰으로 사용자 정보 조회
     */
    fun getUserInfo(accessToken: String): KakaoUserInfo? {
        try {
            val headers = HttpHeaders().apply {
                setBearerAuth(accessToken)
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }

            val request = HttpEntity<String>(null, headers)
            val response = restTemplate.postForEntity(USER_INFO_URL, request, KakaoUserResponse::class.java)

            val body = response.body ?: return null
            
            log.info("Kakao 사용자 정보 조회 성공: kakaoId=${body.id}")
            
            return KakaoUserInfo(
                kakaoId = body.id,
                nickname = body.kakaoAccount?.profile?.nickname ?: "KakaoUser",
                email = body.kakaoAccount?.email
            )
        } catch (e: Exception) {
            log.error("Kakao 사용자 정보 조회 실패: ${e.message}", e)
            return null
        }
    }

    /**
     * 인가 코드로 로그인 처리 (토큰 교환 + 사용자 정보 조회)
     */
    fun processKakaoLogin(code: String): KakaoUserInfo? {
        val tokenResponse = exchangeToken(code) ?: return null
        return getUserInfo(tokenResponse.accessToken)
    }
}

/**
 * Kakao 토큰 응답
 */
data class KakaoTokenResponse(
    val access_token: String,
    val token_type: String,
    val refresh_token: String?,
    val expires_in: Int,
    val scope: String?,
    val refresh_token_expires_in: Int?
) {
    val accessToken: String get() = access_token
}

/**
 * Kakao 사용자 정보 (Internal)
 */
data class KakaoUserResponse(
    val id: Long,
    val connected_at: String?,
    val kakao_account: KakaoAccountResponse?
) {
    val kakaoAccount: KakaoAccountResponse? get() = kakao_account
}

data class KakaoAccountResponse(
    val profile_nickname_needs_agreement: Boolean?,
    val profile: KakaoProfileResponse?,
    val email_needs_agreement: Boolean?,
    val is_email_valid: Boolean?,
    val is_email_verified: Boolean?,
    val email: String?
)

data class KakaoProfileResponse(
    val nickname: String?
)

/**
 * 외부로 반환하는 Kakao 사용자 정보
 */
data class KakaoUserInfo(
    val kakaoId: Long,
    val nickname: String,
    val email: String?
)
