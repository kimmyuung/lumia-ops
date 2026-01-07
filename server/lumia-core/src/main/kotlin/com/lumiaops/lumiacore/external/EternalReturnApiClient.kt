package com.lumiaops.lumiacore.external

import com.lumiaops.lumiacore.config.EternalReturnConfig
import com.lumiaops.lumiacore.config.EternalReturnProperties
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

/**
 * 이터널 리턴 Open API 클라이언트
 * 
 * API 문서: https://developer.eternalreturn.io/
 */
@Component
class EternalReturnApiClient(
    private val restTemplate: RestTemplate,
    private val properties: EternalReturnProperties
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 닉네임으로 유저 번호 조회
     */
    fun getUserByNickname(nickname: String): ErUserInfo? {
        val url = "${properties.baseUrl}/v1/user/nickname?query=$nickname"
        
        return try {
            val response = executeGet<ErApiResponse<ErUserInfo>>(url)
            if (response?.code == 200) {
                response.data
            } else {
                logger.warn("Failed to get user by nickname: ${response?.message}")
                null
            }
        } catch (e: HttpClientErrorException.NotFound) {
            logger.info("User not found: $nickname")
            null
        } catch (e: Exception) {
            logger.error("Error getting user by nickname: $nickname", e)
            null
        }
    }

    /**
     * 유저 시즌 통계 조회
     * @param userNum 유저 번호
     * @param seasonId 시즌 ID (미지정 시 현재 시즌)
     */
    fun getUserStats(userNum: Long, seasonId: Int? = null): List<ErUserStats> {
        val seasonParam = seasonId?.let { "&seasonId=$it" } ?: ""
        val url = "${properties.baseUrl}/v1/user/stats/$userNum$seasonParam"
        
        return try {
            val response = executeGet<ErApiResponse<List<ErUserStats>>>(url)
            if (response?.code == 200) {
                response.data ?: emptyList()
            } else {
                logger.warn("Failed to get user stats: ${response?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Error getting user stats: $userNum", e)
            emptyList()
        }
    }

    /**
     * 유저 실험체별 통계 조회
     */
    fun getCharacterStats(userNum: Long, seasonId: Int? = null): List<ErCharacterStats> {
        val seasonParam = seasonId?.let { "&seasonId=$it" } ?: ""
        val url = "${properties.baseUrl}/v1/user/characterStats/$userNum$seasonParam"
        
        return try {
            val response = executeGet<ErApiResponse<ErCharacterStatsWrapper>>(url)
            if (response?.code == 200) {
                response.data?.characterStats ?: emptyList()
            } else {
                logger.warn("Failed to get character stats: ${response?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Error getting character stats: $userNum", e)
            emptyList()
        }
    }

    /**
     * 유저 최근 게임 기록 조회
     * @param userNum 유저 번호
     * @param next 다음 페이지 커서 (게임 ID)
     */
    fun getUserGames(userNum: Long, next: Long? = null): List<ErGameRecord> {
        val nextParam = next?.let { "&next=$it" } ?: ""
        val url = "${properties.baseUrl}/v1/user/games/$userNum$nextParam"
        
        return try {
            val response = executeGet<ErApiResponse<List<ErGameRecord>>>(url)
            if (response?.code == 200) {
                response.data ?: emptyList()
            } else {
                logger.warn("Failed to get user games: ${response?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            logger.error("Error getting user games: $userNum", e)
            emptyList()
        }
    }

    /**
     * API 키가 설정되어 있는지 확인
     */
    fun isConfigured(): Boolean = properties.key.isNotBlank()

    /**
     * GET 요청 실행
     */
    private inline fun <reified T> executeGet(url: String): T? {
        if (!isConfigured()) {
            logger.warn("Eternal Return API key is not configured")
            return null
        }

        val headers = HttpHeaders().apply {
            set(EternalReturnConfig.API_KEY_HEADER, properties.key)
            accept = listOf(MediaType.APPLICATION_JSON)
        }
        
        val entity = HttpEntity<Any>(headers)
        
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            object : ParameterizedTypeReference<T>() {}
        )
        
        return response.body
    }
}
