package com.lumiaops.lumiacore.service


import com.lumiaops.lumiacore.common.annotation.LogExecution
import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.TeamMember
import com.lumiaops.lumiacore.domain.TeamRole
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.TeamMemberRepository
import com.lumiaops.lumiacore.repository.TeamRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TeamService(
    private val teamRepository: TeamRepository,
    private val teamMemberRepository: TeamMemberRepository
) {
    fun findById(id: Long): Team? = teamRepository.findById(id).orElse(null)

    fun findByName(name: String): Team? = teamRepository.findByName(name)

    fun findByOwnerId(ownerId: Long): List<Team> = teamRepository.findByOwnerId(ownerId)

    fun findAll(): List<Team> = teamRepository.findAll()

    @LogExecution
    @Transactional
    fun createTeam(name: String, description: String?, ownerId: Long): Team {
        return teamRepository.save(Team(name = name, description = description, ownerId = ownerId))
    }


    @Transactional
    fun updateTeam(teamId: Long, name: String, description: String?): Team {
        val team = findById(teamId) ?: throw IllegalArgumentException("팀을 찾을 수 없습니다: $teamId")
        team.name = name
        team.description = description
        return team
    }


    @Transactional
    fun deleteTeam(teamId: Long) {
        teamRepository.deleteById(teamId)
    }

    // TeamMember 관련 메서드
    fun findMembersByTeam(team: Team): List<TeamMember> = teamMemberRepository.findByTeam(team)

    fun findTeamsByUser(user: User): List<TeamMember> = teamMemberRepository.findByUser(user)

    fun isMember(team: Team, user: User): Boolean = teamMemberRepository.existsByTeamAndUser(team, user)


    @Transactional
    fun addMember(team: Team, user: User, role: TeamRole = TeamRole.MEMBER): TeamMember {
        if (isMember(team, user)) {
            throw IllegalArgumentException("이미 팀의 멤버입니다")
        }
        return teamMemberRepository.save(TeamMember(team = team, user = user, role = role))
    }


    @Transactional
    fun removeMember(team: Team, user: User) {
        val member = teamMemberRepository.findByTeamAndUser(team, user)
            ?: throw IllegalArgumentException("팀 멤버를 찾을 수 없습니다")
        teamMemberRepository.delete(member)
    }


    @Transactional
    fun updateMemberRole(team: Team, user: User, newRole: TeamRole): TeamMember {
        val member = teamMemberRepository.findByTeamAndUser(team, user)
            ?: throw IllegalArgumentException("팀 멤버를 찾을 수 없습니다")
        member.role = newRole
        return member
    }
}
