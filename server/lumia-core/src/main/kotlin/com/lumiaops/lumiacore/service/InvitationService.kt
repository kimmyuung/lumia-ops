package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.*
import com.lumiaops.lumiacore.repository.TeamInvitationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 팀 초대 관련 비즈니스 로직 서비스
 */
@Service
@Transactional(readOnly = true)
class InvitationService(
    private val invitationRepository: TeamInvitationRepository,
    private val teamService: TeamService,
    private val userRepository: UserRepository,
    private val emailService: EmailService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 팀 초대 생성
     * @return Pair<TeamInvitation, Boolean> - 초대 객체와 이메일 발송 성공 여부
     */
    @Transactional
    fun createInvitation(
        team: Team,
        invitedEmail: String,
        inviter: User,
        role: TeamRole = TeamRole.MEMBER,
        message: String? = null
    ): Pair<TeamInvitation, Boolean> {
        // 이미 대기 중인 초대가 있는지 확인
        if (invitationRepository.existsByTeamAndInvitedEmailAndStatus(
                team, invitedEmail, InvitationStatus.PENDING
            )
        ) {
            throw IllegalArgumentException("이미 해당 이메일로 대기 중인 초대가 있습니다")
        }

        // 이미 팀의 멤버인지 확인
        val existingUser = userRepository.findByEmail(invitedEmail)
        if (existingUser != null && teamService.isMember(team, existingUser)) {
            throw IllegalArgumentException("이미 팀의 멤버입니다")
        }

        val invitation = TeamInvitation(
            team = team,
            invitedEmail = invitedEmail,
            invitedBy = inviter,
            role = role,
            message = message
        )

        val savedInvitation = invitationRepository.save(invitation)
        log.info("초대 생성됨: teamId=${team.id}, email=$invitedEmail, token=${savedInvitation.token}")

        // 이메일 발송
        val emailSent = emailService.sendInvitationEmail(savedInvitation)

        return Pair(savedInvitation, emailSent)
    }

    /**
     * 토큰으로 초대 조회
     */
    fun getInvitationByToken(token: String): TeamInvitation {
        val invitation = invitationRepository.findByToken(token)
            ?: throw IllegalArgumentException("유효하지 않은 초대입니다")

        // 만료 체크 및 상태 업데이트
        if (invitation.isExpired() && invitation.status == InvitationStatus.PENDING) {
            invitation.markAsExpired()
        }

        return invitation
    }

    /**
     * 팀의 대기 중인 초대 목록 조회
     */
    fun getTeamInvitations(team: Team): List<TeamInvitation> {
        return invitationRepository.findByTeamAndStatus(team, InvitationStatus.PENDING)
    }

    /**
     * 사용자의 대기 중인 초대 목록 조회
     */
    fun getMyPendingInvitations(email: String): List<TeamInvitation> {
        return invitationRepository.findByInvitedEmailAndStatus(email, InvitationStatus.PENDING)
            .filter { !it.isExpired() }
    }

    /**
     * 초대 수락
     * @return 생성된 TeamMember
     */
    @Transactional
    fun acceptInvitation(token: String, acceptingUser: User): TeamMember {
        val invitation = getInvitationByToken(token)

        // 이메일 일치 확인
        if (!invitation.invitedEmail.equals(acceptingUser.email, ignoreCase = true)) {
            throw IllegalArgumentException("초대받은 이메일과 일치하지 않습니다")
        }

        if (!invitation.canAccept()) {
            if (invitation.isExpired()) {
                throw IllegalArgumentException("만료된 초대입니다")
            }
            throw IllegalArgumentException("수락할 수 없는 초대입니다")
        }

        invitation.accept()

        // 팀 멤버로 추가
        val member = teamService.addMember(
            team = invitation.team,
            user = acceptingUser,
            role = invitation.role
        )

        log.info("초대 수락됨: invitationId=${invitation.id}, userId=${acceptingUser.id}, teamId=${invitation.team.id}")
        return member
    }

    /**
     * 초대 거절
     */
    @Transactional
    fun declineInvitation(token: String) {
        val invitation = getInvitationByToken(token)
        invitation.decline()
        log.info("초대 거절됨: invitationId=${invitation.id}")
    }

    /**
     * 초대 취소 (팀장이 취소)
     */
    @Transactional
    fun cancelInvitation(invitationId: Long) {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { IllegalArgumentException("초대를 찾을 수 없습니다") }
        invitation.cancel()
        log.info("초대 취소됨: invitationId=${invitation.id}")
    }

    /**
     * 초대 재발송
     * @return 이메일 발송 성공 여부
     */
    @Transactional
    fun resendInvitation(invitationId: Long): Boolean {
        val invitation = invitationRepository.findById(invitationId)
            .orElseThrow { IllegalArgumentException("초대를 찾을 수 없습니다") }

        if (invitation.status != InvitationStatus.PENDING) {
            throw IllegalArgumentException("대기 중인 초대만 재발송할 수 있습니다")
        }

        if (invitation.isExpired()) {
            throw IllegalArgumentException("만료된 초대는 재발송할 수 없습니다")
        }

        val emailSent = emailService.sendInvitationEmail(invitation)
        log.info("초대 재발송: invitationId=${invitation.id}, emailSent=$emailSent")
        return emailSent
    }

    /**
     * 만료된 초대 처리 (스케줄러)
     * 매일 자정에 실행
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    fun expireInvitations() {
        val expiredInvitations = invitationRepository.findExpiredInvitations()
        expiredInvitations.forEach { it.markAsExpired() }
        if (expiredInvitations.isNotEmpty()) {
            log.info("만료 처리된 초대 수: ${expiredInvitations.size}")
        }
    }
}
