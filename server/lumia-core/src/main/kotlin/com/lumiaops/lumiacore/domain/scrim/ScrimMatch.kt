package com.lumiaops.lumiacore.domain.scrim

import jakarta.persistence.*

@Entity
@Table(name = "scrim_matches")
class ScrimMatch(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scrim_id")
    val scrim: Scrim,

    @Column(nullable = false)
    val roundNumber: Int, // 1라운드, 2라운드...

    @Column(unique = true)
    var gameId: String? = null // 이터널 리턴 공식 게임 ID (API 연동용)
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    // ▼ [핵심 추가] 이 부분이 있어야 match.matchResults 로 접근 가능합니다!
    @OneToMany(mappedBy = "match", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var matchResults: MutableList<MatchResult> = mutableListOf()
}