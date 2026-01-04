package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.domain.strategy.Strategy
import com.lumiaops.lumiacore.repository.StrategyRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class StrategyService(
    private val strategyRepository: StrategyRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun findById(id: Long): Strategy? = strategyRepository.findById(id).orElse(null)

    fun findAll(): List<Strategy> = strategyRepository.findAll()

    fun findByTeam(team: Team): List<Strategy> = strategyRepository.findByTeam(team)

    fun findByCreatedBy(createdBy: Long): List<Strategy> = strategyRepository.findByCreatedBy(createdBy)

    fun searchByTitle(title: String): List<Strategy> = strategyRepository.findByTitleContainingIgnoreCase(title)

    @Transactional
    fun createStrategy(title: String, mapData: String, team: Team?, createdBy: Long): Strategy {
        val strategy = strategyRepository.save(
            Strategy(
                title = title,
                mapData = mapData,
                team = team,
                createdBy = createdBy
            )
        )
        log.info("전략 생성: strategyId=${strategy.id}, title=$title, teamId=${team?.id}, createdBy=$createdBy")
        return strategy
    }

    @Transactional
    fun updateStrategy(strategyId: Long, title: String, mapData: String): Strategy {
        val strategy = findById(strategyId)
            ?: throw IllegalArgumentException("전략을 찾을 수 없습니다: $strategyId")
        val oldTitle = strategy.title
        strategy.updateData(title, mapData)
        log.info("전략 수정: strategyId=$strategyId, title: $oldTitle→$title")
        return strategy
    }

    @Transactional
    fun deleteStrategy(strategyId: Long) {
        log.warn("전략 삭제: strategyId=$strategyId")
        strategyRepository.deleteById(strategyId)
    }
}
