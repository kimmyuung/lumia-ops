package com.lumiaops.lumiacore.repository

import com.lumiaops.lumiacore.domain.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamRepository : JpaRepository<Team, Long> {
    fun findByName(name: String): Team?
    fun findByOwnerId(ownerId: Long): List<Team>
}
