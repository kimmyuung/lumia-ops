package com.lumiaops.lumiacore.domain.scrim

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 스크림 엔티티 (Aggregate Root)
 * 스크림 일정, 상태, 매치 관리를 담당합니다.
 */
@Entity
@Table(name = "scrims")
class Scrim(
    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var startTime: LocalDateTime
) : BaseTimeEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ScrimStatus = ScrimStatus.SCHEDULED
        protected set

    @OneToMany(mappedBy = "scrim", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private val _matches: MutableList<ScrimMatch> = mutableListOf()

    val matches: List<ScrimMatch>
        get() = _matches.toList()

    // ==================== 조회 메서드 ====================

    /**
     * 스크림이 종료되었는지 확인 (하위 호환성)
     */
    val isFinished: Boolean
        get() = status == ScrimStatus.FINISHED

    /**
     * 스크림이 활성 상태인지 확인
     */
    fun isActive(): Boolean = status == ScrimStatus.SCHEDULED || status == ScrimStatus.IN_PROGRESS

    /**
     * 스크림 수정 가능 여부
     */
    fun canEdit(): Boolean = status == ScrimStatus.SCHEDULED

    /**
     * 매치 추가 가능 여부
     */
    fun canAddMatch(): Boolean = status != ScrimStatus.FINISHED && status != ScrimStatus.CANCELLED

    /**
     * 매치 수
     */
    fun matchCount(): Int = _matches.size

    /**
     * 특정 라운드 조회
     */
    fun getMatch(roundNumber: Int): ScrimMatch? =
        _matches.find { it.roundNumber == roundNumber }

    // ==================== 상태 변경 메서드 ====================

    /**
     * 스크림 시작
     */
    fun start() {
        require(status == ScrimStatus.SCHEDULED) { "예정된 스크림만 시작할 수 있습니다. 현재 상태: $status" }
        status = ScrimStatus.IN_PROGRESS
    }

    /**
     * 스크림 종료
     */
    fun finish() {
        require(status == ScrimStatus.IN_PROGRESS || status == ScrimStatus.SCHEDULED) {
            "진행 중이거나 예정된 스크림만 종료할 수 있습니다. 현재 상태: $status"
        }
        status = ScrimStatus.FINISHED
    }

    /**
     * 스크림 취소
     */
    fun cancel() {
        require(status != ScrimStatus.FINISHED) { "이미 종료된 스크림은 취소할 수 없습니다" }
        status = ScrimStatus.CANCELLED
    }

    // ==================== 매치 관리 메서드 ====================

    /**
     * 라운드 추가 (Aggregate Root를 통해서만!)
     */
    fun addMatch(gameId: String? = null): ScrimMatch {
        require(canAddMatch()) { "종료되거나 취소된 스크림에는 매치를 추가할 수 없습니다" }
        val roundNumber = _matches.size + 1
        val match = ScrimMatch(scrim = this, roundNumber = roundNumber, gameId = gameId)
        _matches.add(match)
        return match
    }

    /**
     * 스크림 정보 수정
     */
    fun update(title: String?, startTime: LocalDateTime?) {
        require(canEdit()) { "예정된 스크림만 수정할 수 있습니다. 현재 상태: $status" }
        title?.let { this.title = it }
        startTime?.let { this.startTime = it }
    }
}