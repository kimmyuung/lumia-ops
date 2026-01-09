package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.scrim.Scrim
import com.lumiaops.lumiacore.domain.scrim.ScrimStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ScrimRepository : JpaRepository<Scrim, Long> {
    fun findByStatus(status: ScrimStatus): List<Scrim>
    fun findByStatusIn(statuses: List<ScrimStatus>): List<Scrim>
    fun findByStartTimeBetween(start: LocalDateTime, end: LocalDateTime): List<Scrim>
    fun findByTitleContainingIgnoreCase(title: String): List<Scrim>
    
    /**
     * Scrim과 Matches를 함께 조회 (N+1 방지)
     */
    @Query("SELECT DISTINCT s FROM Scrim s LEFT JOIN FETCH s._matches WHERE s.id = :id")
    fun findByIdWithMatches(id: Long): Scrim?
    
    /**
     * Scrim, Matches, Results를 모두 함께 조회 (점수 재계산 등 전체 데이터 필요시)
     */
    @Query("""
        SELECT DISTINCT s FROM Scrim s 
        LEFT JOIN FETCH s._matches m 
        LEFT JOIN FETCH m._results 
        WHERE s.id = :id
    """)
    fun findByIdWithMatchesAndResults(id: Long): Scrim?
    
    /**
     * 상태별 Scrim 목록 조회 (Matches 포함)
     */
    @Query("SELECT DISTINCT s FROM Scrim s LEFT JOIN FETCH s._matches WHERE s.status = :status")
    fun findByStatusWithMatches(status: ScrimStatus): List<Scrim>
}
