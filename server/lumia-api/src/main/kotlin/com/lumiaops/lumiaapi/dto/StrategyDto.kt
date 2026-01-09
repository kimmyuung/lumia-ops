package com.lumiaops.lumiaapi.dto

import jakarta.validation.constraints.NotBlank

data class CreateStrategyRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    val title: String,

    val mapData: String = "{}"
)

data class UpdateStrategyRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    val title: String,
    
    val mapData: String
)

/**
 * 전략 응답 DTO
 */
data class StrategyResponse(
    val id: Long,
    val title: String,
    val mapData: String,
    val teamId: Long?,
    val createdBy: Long,
    val createdAt: java.time.LocalDateTime?,
    val updatedAt: java.time.LocalDateTime?
)
