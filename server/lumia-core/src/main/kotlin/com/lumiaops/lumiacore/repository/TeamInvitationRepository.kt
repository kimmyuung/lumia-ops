package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.InvitationStatus
import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.TeamInvitation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface TeamInvitationRepository : JpaRepository<TeamInvitation, Long> {

    /**
     * 토큰으로 초대 조회
     */
    fun findByToken(token: String): TeamInvitation?

    /**
     * 팀의 특정 상태 초대 목록 조회
     */
    fun findByTeamAndStatus(team: Team, status: InvitationStatus): List<TeamInvitation>

    /**
     * 이메일과 상태로 초대 목록 조회
     */
    fun findByInvitedEmailAndStatus(invitedEmail: String, status: InvitationStatus): List<TeamInvitation>

    /**
     * 팀에 해당 이메일로 대기 중인 초대가 있는지 확인
     */
    fun existsByTeamAndInvitedEmailAndStatus(
        team: Team,
        invitedEmail: String,
        status: InvitationStatus
    ): Boolean

    /**
     * 만료된 대기 중인 초대 조회 (스케줄러용)
     */
    @Query("SELECT i FROM TeamInvitation i WHERE i.status = :status AND i.expiresAt < :now")
    fun findExpiredInvitations(
        status: InvitationStatus = InvitationStatus.PENDING,
        now: LocalDateTime = LocalDateTime.now()
    ): List<TeamInvitation>
}
