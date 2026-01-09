package com.lumiaops.lumiacore.domain.strategy

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import com.lumiaops.lumiacore.domain.Team
import jakarta.persistence.*

@Entity
@Table(
    name = "strategies",
    indexes = [
        Index(name = "idx_strategy_team", columnList = "team_id"),
        Index(name = "idx_strategy_created_by", columnList = "createdBy")
    ]
)
class Strategy(
    @Column(nullable = false)
    var title: String,

    // 지도 위에 그린 데이터 (용량이 클 수 있으므로 TEXT나 JSONB 타입 권장)
    // PostgreSQL 사용 시 @Column(columnDefinition = "jsonb") 고려
    @Column(columnDefinition = "TEXT", nullable = false)
    var mapData: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    val team: Team? = null, // 팀 전략일 수도, 개인 전략일 수도 있음 (Nullable)

    @Column(nullable = false)
    val createdBy: Long // 작성자 ID
) : BaseTimeEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun updateData(title: String, mapData: String) {
        this.title = title
        this.mapData = mapData
    }
}