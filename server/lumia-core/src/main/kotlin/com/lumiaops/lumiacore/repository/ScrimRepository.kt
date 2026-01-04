package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.scrim.Scrim
import com.lumiaops.lumiacore.domain.scrim.ScrimStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ScrimRepository : JpaRepository<Scrim, Long> {
    fun findByStatus(status: ScrimStatus): List<Scrim>
    fun findByStatusIn(statuses: List<ScrimStatus>): List<Scrim>
    fun findByStartTimeBetween(start: LocalDateTime, end: LocalDateTime): List<Scrim>
    fun findByTitleContainingIgnoreCase(title: String): List<Scrim>
}
