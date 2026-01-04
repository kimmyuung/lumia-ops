package com.lumiaops.lumiacore.domain

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "team_members",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_team_member_team_user",
            columnNames = ["team_id", "user_id"]
        )
    ]
)
class TeamMember(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    var role: TeamRole = TeamRole.MEMBER
) : BaseTimeEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}

enum class TeamRole { OWNER, LEADER, MEMBER }
