package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.strategy.Strategy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StrategyRepository : JpaRepository<Strategy, Long> {
    fun findByTeam(team: Team): List<Strategy>
    fun findByCreatedBy(createdBy: Long): List<Strategy>
    fun findByTitleContainingIgnoreCase(title: String): List<Strategy>
}
