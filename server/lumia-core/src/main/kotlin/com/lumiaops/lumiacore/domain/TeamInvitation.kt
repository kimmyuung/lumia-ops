package com.lumiaops.lumiacore.domain

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * 팀 초대 엔티티
 * 팀장이 이메일을 통해 사용자를 팀에 초대할 때 생성됩니다.
 */
@Entity
@Table(
    name = "team_invitations",
    indexes = [
        Index(name = "idx_invitation_token", columnList = "token", unique = true),
        Index(name = "idx_invitation_email_status", columnList = "invitedEmail, status")
    ]
)
class TeamInvitation(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,

    @Column(nullable = false)
    val invitedEmail: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id", nullable = false)
    val invitedBy: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: TeamRole = TeamRole.MEMBER,

    @Column(length = 500)
    var message: String? = null,

    @Column(nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now().plusDays(7)
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true)
    val token: String = UUID.randomUUID().toString()

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: InvitationStatus = InvitationStatus.PENDING
        protected set

    var respondedAt: LocalDateTime? = null
        protected set

    /**
     * 초대가 만료되었는지 확인
     */
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    /**
     * 초대를 수락할 수 있는지 확인
     */
    fun canAccept(): Boolean = status == InvitationStatus.PENDING && !isExpired()

    /**
     * 초대 수락
     */
    fun accept() {
        require(canAccept()) { "초대를 수락할 수 없습니다. 상태: $status, 만료: ${isExpired()}" }
        status = InvitationStatus.ACCEPTED
        respondedAt = LocalDateTime.now()
    }

    /**
     * 초대 거절
     */
    fun decline() {
        require(status == InvitationStatus.PENDING) { "대기 중인 초대만 거절할 수 있습니다" }
        status = InvitationStatus.DECLINED
        respondedAt = LocalDateTime.now()
    }

    /**
     * 초대 취소 (팀장이 취소)
     */
    fun cancel() {
        require(status == InvitationStatus.PENDING) { "대기 중인 초대만 취소할 수 있습니다" }
        status = InvitationStatus.CANCELLED
    }

    /**
     * 만료 처리
     */
    fun markAsExpired() {
        if (status == InvitationStatus.PENDING && isExpired()) {
            status = InvitationStatus.EXPIRED
        }
    }
}
