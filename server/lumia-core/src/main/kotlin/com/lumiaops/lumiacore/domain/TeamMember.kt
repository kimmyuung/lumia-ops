package com.lumiaops.lumiacore.domain

import jakarta.persistence.*

@Entity
@Table(name = "team_members")
class TeamMember(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Enumerated(EnumType.STRING)
    var role: TeamRole = TeamRole.MEMBER
) {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}

enum class TeamRole { OWNER, LEADER, MEMBER }