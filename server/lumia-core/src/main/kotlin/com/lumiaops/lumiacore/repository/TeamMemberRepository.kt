package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.TeamMember
import com.lumiaops.lumiacore.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamMemberRepository : JpaRepository<TeamMember, Long> {
    fun findByTeam(team: Team): List<TeamMember>
    fun findByUser(user: User): List<TeamMember>
    fun findByTeamAndUser(team: Team, user: User): TeamMember?
    fun existsByTeamAndUser(team: Team, user: User): Boolean
}
