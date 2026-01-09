package com.lumiaops.lumiacore.domain

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "teams",
    indexes = [
        Index(name = "idx_team_owner_id", columnList = "ownerId")
    ]
)
class Team(
    @Column(nullable = false)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var ownerId: Long, // 팀장(방장)의 User ID

    /** 디스코드 웹훅 URL */
    @Column(length = 500)
    var discordWebhookUrl: String? = null
) : BaseTimeEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Version
    val version: Long = 0

    /**
     * 팀장 변경
     */
    fun transferOwnership(newOwnerId: Long) {
        require(newOwnerId != ownerId) { "이미 해당 사용자가 팀장입니다" }
        this.ownerId = newOwnerId
    }
}
