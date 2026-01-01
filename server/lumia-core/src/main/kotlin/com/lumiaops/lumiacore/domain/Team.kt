package com.lumiaops.lumiacore.domain

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "teams")
class Team(
    @Column(nullable = false)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    val ownerId: Long // 팀장(방장)의 User ID (연관관계 끊어내기 전략)
) : BaseTimeEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}