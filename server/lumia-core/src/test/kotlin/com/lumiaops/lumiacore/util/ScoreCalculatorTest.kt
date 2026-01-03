package com.lumiaops.lumiacore.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@DisplayName("ScoreCalculator 테스트")
class ScoreCalculatorTest {

    @Nested
    @DisplayName("점수 계산")
    inner class CalculateScore {

        @Test
        @DisplayName("1위 + 10킬 = 10 + 10 = 20점")
        fun calculateScore_FirstPlaceWith10Kills() {
            val score = ScoreCalculator.calculateScore(rank = 1, kills = 10)
            assertEquals(20, score)
        }

        @Test
        @DisplayName("2위 + 5킬 = 6 + 5 = 11점")
        fun calculateScore_SecondPlaceWith5Kills() {
            val score = ScoreCalculator.calculateScore(rank = 2, kills = 5)
            assertEquals(11, score)
        }

        @Test
        @DisplayName("3위 + 0킬 = 5 + 0 = 5점")
        fun calculateScore_ThirdPlaceWithNoKills() {
            val score = ScoreCalculator.calculateScore(rank = 3, kills = 0)
            assertEquals(5, score)
        }

        @Test
        @DisplayName("8위 이하는 순위 점수 0점")
        fun calculateScore_LowRankGivesZeroPlacementPoints() {
            val score = ScoreCalculator.calculateScore(rank = 10, kills = 3)
            assertEquals(3, score) // 킬 점수만
        }

        @Test
        @DisplayName("킬 배수 적용")
        fun calculateScore_WithKillMultiplier() {
            val score = ScoreCalculator.calculateScore(rank = 1, kills = 5, killMultiplier = 2)
            assertEquals(20, score) // 10 + (5 * 2) = 20
        }

        @Test
        @DisplayName("음수 순위는 예외 발생")
        fun calculateScore_NegativeRankThrows() {
            assertThrows<IllegalArgumentException> {
                ScoreCalculator.calculateScore(rank = -1, kills = 0)
            }
        }

        @Test
        @DisplayName("음수 킬 수는 예외 발생")
        fun calculateScore_NegativeKillsThrows() {
            assertThrows<IllegalArgumentException> {
                ScoreCalculator.calculateScore(rank = 1, kills = -1)
            }
        }
    }

    @Nested
    @DisplayName("순위 점수")
    inner class PlacementPoints {

        @Test
        @DisplayName("순위별 점수 확인")
        fun getPlacementPoints_AllRanks() {
            assertEquals(10, ScoreCalculator.getPlacementPoints(1))
            assertEquals(6, ScoreCalculator.getPlacementPoints(2))
            assertEquals(5, ScoreCalculator.getPlacementPoints(3))
            assertEquals(4, ScoreCalculator.getPlacementPoints(4))
            assertEquals(3, ScoreCalculator.getPlacementPoints(5))
            assertEquals(2, ScoreCalculator.getPlacementPoints(6))
            assertEquals(1, ScoreCalculator.getPlacementPoints(7))
            assertEquals(1, ScoreCalculator.getPlacementPoints(8))
            assertEquals(0, ScoreCalculator.getPlacementPoints(9))
            assertEquals(0, ScoreCalculator.getPlacementPoints(20))
        }
    }

    @Nested
    @DisplayName("승리/상위권 판별")
    inner class WinAndTopChecks {

        @Test
        @DisplayName("1위만 승리")
        fun isWin_OnlyFirstPlace() {
            assertTrue(ScoreCalculator.isWin(1))
            assertFalse(ScoreCalculator.isWin(2))
        }

        @Test
        @DisplayName("상위 3위 확인")
        fun isTop3_FirstThreePlaces() {
            assertTrue(ScoreCalculator.isTop3(1))
            assertTrue(ScoreCalculator.isTop3(2))
            assertTrue(ScoreCalculator.isTop3(3))
            assertFalse(ScoreCalculator.isTop3(4))
        }

        @Test
        @DisplayName("상위 절반 확인")
        fun isTopHalf_ChecksCorrectly() {
            assertTrue(ScoreCalculator.isTopHalf(rank = 5, totalTeams = 16))
            assertTrue(ScoreCalculator.isTopHalf(rank = 8, totalTeams = 16))
            assertFalse(ScoreCalculator.isTopHalf(rank = 9, totalTeams = 16))
        }
    }

    @Nested
    @DisplayName("다중 매치 계산")
    inner class MultipleMatches {

        @Test
        @DisplayName("총점 계산")
        fun calculateTotalScore_MultipleMatches() {
            val results = listOf(
                MatchScoreInput(rank = 1, kills = 5),  // 10 + 5 = 15
                MatchScoreInput(rank = 3, kills = 3),  // 5 + 3 = 8
                MatchScoreInput(rank = 5, kills = 2)   // 3 + 2 = 5
            )
            val total = ScoreCalculator.calculateTotalScore(results)
            assertEquals(28, total)
        }

        @Test
        @DisplayName("평균 점수 계산")
        fun calculateAverageScore_MultipleMatches() {
            val results = listOf(
                MatchScoreInput(rank = 1, kills = 10),  // 20
                MatchScoreInput(rank = 2, kills = 5)    // 11
            )
            val avg = ScoreCalculator.calculateAverageScore(results)
            assertEquals(15.5, avg)
        }

        @Test
        @DisplayName("빈 결과는 평균 0")
        fun calculateAverageScore_EmptyList() {
            val avg = ScoreCalculator.calculateAverageScore(emptyList())
            assertEquals(0.0, avg)
        }
    }
}
