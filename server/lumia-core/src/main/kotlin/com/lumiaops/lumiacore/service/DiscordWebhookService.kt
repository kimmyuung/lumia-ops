package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * 디스코드 웹훅 서비스
 * 팀에 설정된 디스코드 웹훅으로 알림 전송
 */
@Service
class DiscordWebhookService(
    private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 디스코드로 메시지 전송
     */
    @Async
    fun sendMessage(team: Team, title: String, description: String, color: Int = 0x5865F2) {
        val webhookUrl = team.discordWebhookUrl
        if (webhookUrl.isNullOrBlank()) {
            log.debug("팀 ${team.name}에 디스코드 웹훅이 설정되지 않음")
            return
        }

        try {
            val embed = mapOf(
                "title" to title,
                "description" to description,
                "color" to color,
                "footer" to mapOf("text" to "Lumia Ops")
            )

            val payload = mapOf(
                "embeds" to listOf(embed)
            )

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val request = HttpEntity(payload, headers)
            restTemplate.postForEntity(webhookUrl, request, String::class.java)

            log.info("디스코드 웹훅 전송 성공: team=${team.name}, title=$title")
        } catch (e: Exception) {
            log.error("디스코드 웹훅 전송 실패: team=${team.name}, error=${e.message}")
        }
    }

    /**
     * 스크림 시작 알림
     */
    fun notifyScrimStarted(team: Team, scrimTitle: String) {
        sendMessage(
            team = team,
            title = "🎮 스크림 시작",
            description = "**$scrimTitle** 스크림이 시작되었습니다!",
            color = 0x57F287 // Green
        )
    }

    /**
     * 스크림 종료 알림
     */
    fun notifyScrimFinished(team: Team, scrimTitle: String) {
        sendMessage(
            team = team,
            title = "🏁 스크림 종료",
            description = "**$scrimTitle** 스크림이 종료되었습니다.",
            color = 0xFEE75C // Yellow
        )
    }

    /**
     * 새 멤버 가입 알림
     */
    fun notifyMemberJoined(team: Team, memberName: String) {
        sendMessage(
            team = team,
            title = "👋 새 멤버 가입",
            description = "**$memberName**님이 팀에 합류했습니다!",
            color = 0x5865F2 // Blurple
        )
    }

    /**
     * 매치 결과 등록 알림
     */
    fun notifyMatchResult(team: Team, scrimTitle: String, rank: Int, kills: Int) {
        val medal = when (rank) {
            1 -> "🥇"
            2 -> "🥈"
            3 -> "🥉"
            else -> "📊"
        }

        sendMessage(
            team = team,
            title = "$medal 매치 결과",
            description = "**$scrimTitle**\n순위: ${rank}위 | 킬: ${kills}킬",
            color = if (rank <= 3) 0x57F287 else 0x5865F2
        )
    }
}
