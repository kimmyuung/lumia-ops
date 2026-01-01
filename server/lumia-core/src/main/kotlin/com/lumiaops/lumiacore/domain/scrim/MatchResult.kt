package com.myeongho.lumia.core.domain.scrim

import com.myeongho.lumia.core.domain.team.Team
import jakarta.persistence.*

@Entity
@Table(name = "match_results")
class MatchResult(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    val match: ScrimMatch,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    val team: Team,

    var rank: Int,          // 순위
    var killCount: Int,     // 킬 수
    var totalScore: Int     // 합산 점수 (대회 룰에 따라 계산)
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}