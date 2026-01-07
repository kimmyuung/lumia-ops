package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.external.*
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

@DisplayName("PlayerStatsService 테스트")
class PlayerStatsServiceTest {

    private lateinit var apiClient: EternalReturnApiClient
    private lateinit var playerStatsService: PlayerStatsService

    @BeforeEach
    fun setUp() {
        apiClient = mockk()
        playerStatsService = PlayerStatsService(apiClient)
    }

    @Nested
    @DisplayName("isConfigured 테스트")
    inner class IsConfiguredTest {

        @Test
        @DisplayName("API 키가 설정되어 있으면 true 반환")
        fun isConfigured_ReturnsTrue() {
            every { apiClient.isConfigured() } returns true

            val result = playerStatsService.isConfigured()

            assertTrue(result)
        }

        @Test
        @DisplayName("API 키가 설정되지 않으면 false 반환")
        fun isConfigured_ReturnsFalse() {
            every { apiClient.isConfigured() } returns false

            val result = playerStatsService.isConfigured()

            assertFalse(result)
        }
    }

    @Nested
    @DisplayName("getPlayerStats 테스트")
    inner class GetPlayerStatsTest {

        @Test
        @DisplayName("닉네임으로 플레이어 통계 조회 성공")
        fun getPlayerStats_Success() {
            val nickname = "TestPlayer"
            val userInfo = ErUserInfo(userNum = 12345L, nickname = nickname)
            val userStats = listOf(
                ErUserStats(
                    seasonId = 1,
                    userNum = 12345L,
                    matchingTeamMode = 3,
                    mmr = 1500,
                    nickname = nickname,
                    rank = 100,
                    totalGames = 100,
                    top1 = 10,
                    top3 = 30,
                    top5 = 50,
                    averageRank = 3.5,
                    averageKills = 2.5
                )
            )

            every { apiClient.getUserByNickname(nickname) } returns userInfo
            every { apiClient.getUserStats(12345L, null) } returns userStats

            val result = playerStatsService.getPlayerStats(nickname)

            assertNotNull(result)
            assertEquals(12345L, result!!.userNum)
            assertEquals(nickname, result.nickname)
            assertEquals(100, result.totalGames)
            assertEquals(10, result.wins)
            assertEquals(30, result.top3Count)
            assertEquals(10.0, result.winRate)  // 10/100 * 100
            assertEquals(30.0, result.top3Rate) // 30/100 * 100
        }

        @Test
        @DisplayName("존재하지 않는 닉네임으로 조회 시 null 반환")
        fun getPlayerStats_UserNotFound() {
            val nickname = "NonExistent"
            
            every { apiClient.getUserByNickname(nickname) } returns null

            val result = playerStatsService.getPlayerStats(nickname)

            assertNull(result)
        }

        @Test
        @DisplayName("통계가 없는 유저 조회 시 기본값 반환")
        fun getPlayerStats_NoStats() {
            val nickname = "NewPlayer"
            val userInfo = ErUserInfo(userNum = 12345L, nickname = nickname)

            every { apiClient.getUserByNickname(nickname) } returns userInfo
            every { apiClient.getUserStats(12345L, null) } returns emptyList()

            val result = playerStatsService.getPlayerStats(nickname)

            assertNotNull(result)
            assertEquals(0, result!!.totalGames)
            assertEquals(0.0, result.winRate)
        }
    }

    @Nested
    @DisplayName("getTopCharacters 테스트")
    inner class GetTopCharactersTest {

        @Test
        @DisplayName("많이 사용한 실험체 Top N 조회 성공")
        fun getTopCharacters_Success() {
            val nickname = "TestPlayer"
            val userInfo = ErUserInfo(userNum = 12345L, nickname = nickname)
            val characterStats = listOf(
                ErCharacterStats(characterCode = 1, totalGames = 50, top1 = 10, top3 = 25, averageRank = 3.0),
                ErCharacterStats(characterCode = 2, totalGames = 30, top1 = 5, top3 = 15, averageRank = 4.0),
                ErCharacterStats(characterCode = 3, totalGames = 20, top1 = 2, top3 = 8, averageRank = 5.0)
            )

            every { apiClient.getUserByNickname(nickname) } returns userInfo
            every { apiClient.getCharacterStats(12345L, null) } returns characterStats

            val result = playerStatsService.getTopCharacters(nickname, 2)

            assertEquals(2, result.size)
            assertEquals(1, result[0].characterCode)  // 가장 많이 사용한 실험체
            assertEquals("재키", result[0].characterName)
            assertEquals(50, result[0].totalGames)
            assertEquals(20.0, result[0].winRate)  // 10/50 * 100
        }
    }

    @Nested
    @DisplayName("getRecentGames 테스트")
    inner class GetRecentGamesTest {

        @Test
        @DisplayName("최근 게임 기록 조회 성공")
        fun getRecentGames_Success() {
            val nickname = "TestPlayer"
            val userInfo = ErUserInfo(userNum = 12345L, nickname = nickname)
            val gameRecords = listOf(
                ErGameRecord(
                    gameId = 1001L,
                    characterNum = 1,
                    gameRank = 1,
                    playerKill = 5,
                    playerAssistant = 2,
                    mmrGain = 15,
                    playTime = 1200,
                    startDtm = "2026-01-07T12:00:00"
                ),
                ErGameRecord(
                    gameId = 1000L,
                    characterNum = 2,
                    gameRank = 3,
                    playerKill = 3,
                    playerAssistant = 1,
                    mmrGain = 5,
                    playTime = 900,
                    startDtm = "2026-01-07T11:00:00"
                )
            )

            every { apiClient.getUserByNickname(nickname) } returns userInfo
            every { apiClient.getUserGames(12345L) } returns gameRecords

            val result = playerStatsService.getRecentGames(nickname, 5)

            assertEquals(2, result.size)
            assertEquals(1001L, result[0].gameId)
            assertEquals("재키", result[0].characterName)
            assertEquals(1, result[0].gameRank)
            assertEquals(5, result[0].kills)
        }
    }
}
