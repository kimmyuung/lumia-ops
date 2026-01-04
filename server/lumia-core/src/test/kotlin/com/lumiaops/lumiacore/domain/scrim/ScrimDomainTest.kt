package com.lumiaops.lumiacore.domain.scrim

import com.lumiaops.lumiacore.domain.Team
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("Scrim 도메인 엔티티 단위 테스트")
class ScrimDomainTest {

    @Nested
    @DisplayName("Scrim 엔티티 테스트")
    inner class ScrimTest {

        @Test
        @DisplayName("Scrim 객체 생성 성공")
        fun `should create scrim with valid parameters`() {
            // given
            val title = "테스트 스크림"
            val startTime = LocalDateTime.now().plusDays(1)

            // when
            val scrim = Scrim(title = title, startTime = startTime)

            // then
            assertEquals(title, scrim.title)
            assertEquals(startTime, scrim.startTime)
            assertEquals(ScrimStatus.SCHEDULED, scrim.status)
            assertFalse(scrim.isFinished)
        }

        @Test
        @DisplayName("기본 status는 SCHEDULED")
        fun `should have status as SCHEDULED by default`() {
            // given & when
            val scrim = Scrim(title = "스크림", startTime = LocalDateTime.now())

            // then
            assertEquals(ScrimStatus.SCHEDULED, scrim.status)
            assertFalse(scrim.isFinished)
        }

        @Test
        @DisplayName("스크림 시작 처리")
        fun `should start scrim successfully`() {
            // given
            val scrim = Scrim(title = "스크림", startTime = LocalDateTime.now())

            // when
            scrim.start()

            // then
            assertEquals(ScrimStatus.IN_PROGRESS, scrim.status)
        }

        @Test
        @DisplayName("스크림 완료 처리")
        fun `should finish scrim successfully`() {
            // given
            val scrim = Scrim(title = "스크림", startTime = LocalDateTime.now())

            // when
            scrim.finish()

            // then
            assertEquals(ScrimStatus.FINISHED, scrim.status)
            assertTrue(scrim.isFinished)
        }

        @Test
        @DisplayName("스크림 취소 처리")
        fun `should cancel scrim successfully`() {
            // given
            val scrim = Scrim(title = "스크림", startTime = LocalDateTime.now())

            // when
            scrim.cancel()

            // then
            assertEquals(ScrimStatus.CANCELLED, scrim.status)
        }

        @Test
        @DisplayName("완료된 스크림은 취소 불가")
        fun `should not cancel finished scrim`() {
            // given
            val scrim = Scrim(title = "스크림", startTime = LocalDateTime.now())
            scrim.finish()

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                scrim.cancel()
            }
            assertTrue(exception.message!!.contains("종료된 스크림"))
        }

        @Test
        @DisplayName("스크림에 매치 추가")
        fun `should add match to scrim`() {
            // given
            val scrim = Scrim(title = "스크림", startTime = LocalDateTime.now())

            // when
            val match = scrim.addMatch("game-123")

            // then
            assertEquals(1, scrim.matchCount())
            assertEquals(1, match.roundNumber)
            assertEquals("game-123", match.gameId)
        }

        @Test
        @DisplayName("완료된 스크림에는 매치 추가 불가")
        fun `should not add match to finished scrim`() {
            // given
            val scrim = Scrim(title = "스크림", startTime = LocalDateTime.now())
            scrim.finish()

            // when & then
            assertThrows<IllegalArgumentException> {
                scrim.addMatch()
            }
        }

        @Test
        @DisplayName("스크림 정보 수정")
        fun `should update scrim info`() {
            // given
            val scrim = Scrim(title = "기존 제목", startTime = LocalDateTime.now())
            val newTitle = "새 제목"
            val newTime = LocalDateTime.now().plusDays(2)

            // when
            scrim.update(newTitle, newTime)

            // then
            assertEquals(newTitle, scrim.title)
            assertEquals(newTime, scrim.startTime)
        }
    }

    @Nested
    @DisplayName("ScrimMatch 엔티티 테스트")
    inner class ScrimMatchTest {

        private val scrim = Scrim(title = "테스트 스크림", startTime = LocalDateTime.now())

        @Test
        @DisplayName("ScrimMatch 객체 생성 성공")
        fun `should create scrim match with valid parameters`() {
            // given
            val roundNumber = 1

            // when
            val match = ScrimMatch(scrim = scrim, roundNumber = roundNumber)

            // then
            assertEquals(scrim, match.scrim)
            assertEquals(roundNumber, match.roundNumber)
            assertNull(match.gameId)
        }

        @Test
        @DisplayName("gameId를 포함하여 ScrimMatch 생성")
        fun `should create scrim match with game id`() {
            // given
            val roundNumber = 1
            val gameId = "game-12345"

            // when
            val match = ScrimMatch(scrim = scrim, roundNumber = roundNumber, gameId = gameId)

            // then
            assertEquals(gameId, match.gameId)
        }

        @Test
        @DisplayName("gameId 업데이트")
        fun `should update game id`() {
            // given
            val match = ScrimMatch(scrim = scrim, roundNumber = 1)
            val newGameId = "new-game-id"

            // when
            match.updateGameId(newGameId)

            // then
            assertEquals(newGameId, match.gameId)
        }
    }

    @Nested
    @DisplayName("MatchResult 엔티티 테스트")
    inner class MatchResultTest {

        private val scrim = Scrim(title = "테스트 스크림", startTime = LocalDateTime.now())
        private val match = ScrimMatch(scrim = scrim, roundNumber = 1)
        private val team = Team(name = "테스트팀", description = null, ownerId = 1L)

        @Test
        @DisplayName("MatchResult 객체 생성 성공")
        fun `should create match result with valid parameters`() {
            // given
            val rank = 1
            val killCount = 10
            val totalScore = 100

            // when
            val result = MatchResult(
                match = match,
                team = team,
                rank = rank,
                killCount = killCount,
                totalScore = totalScore
            )

            // then
            assertEquals(match, result.match)
            assertEquals(team, result.team)
            assertEquals(rank, result.rank)
            assertEquals(killCount, result.killCount)
            assertEquals(totalScore, result.totalScore)
        }

        @Test
        @DisplayName("순위 업데이트")
        fun `should update rank`() {
            // given
            val result = MatchResult(
                match = match,
                team = team,
                rank = 2,
                killCount = 5,
                totalScore = 50
            )

            // when
            result.rank = 1

            // then
            assertEquals(1, result.rank)
        }

        @Test
        @DisplayName("킬 수 업데이트")
        fun `should update kill count`() {
            // given
            val result = MatchResult(
                match = match,
                team = team,
                rank = 1,
                killCount = 5,
                totalScore = 50
            )

            // when
            result.killCount = 10

            // then
            assertEquals(10, result.killCount)
        }

        @Test
        @DisplayName("총점 업데이트")
        fun `should update total score`() {
            // given
            val result = MatchResult(
                match = match,
                team = team,
                rank = 1,
                killCount = 5,
                totalScore = 50
            )

            // when
            result.totalScore = 100

            // then
            assertEquals(100, result.totalScore)
        }
    }
}
