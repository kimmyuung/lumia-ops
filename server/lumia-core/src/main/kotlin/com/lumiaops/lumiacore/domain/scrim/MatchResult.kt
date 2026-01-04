package com.lumiaops.lumiacore.domain.scrim

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.scrim.ScrimMatch
import jakarta.persistence.*

@Entity
@Table(
    name = "match_results",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_match_result_match_team",
            columnNames = ["match_id", "team_id"]
        )
    ]
)
class MatchResult(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    val match: ScrimMatch,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    var rank: Int,          // 순위
    var killCount: Int,     // 킬 수
    var totalScore: Int     // 합산 점수 (대회 룰에 따라 계산)
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
