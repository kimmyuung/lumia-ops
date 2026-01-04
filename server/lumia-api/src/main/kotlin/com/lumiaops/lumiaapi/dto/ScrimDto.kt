package com.lumiaops.lumiaapi.dto

import com.lumiaops.lumiacore.domain.scrim.ScrimStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CreateScrimRequest(
    @field:NotBlank(message = "스크림 제목은 필수입니다")
    val title: String,

    @field:NotNull(message = "시작 시간은 필수입니다")
    val startTime: LocalDateTime
)

data class UpdateScrimRequest(
    val title: String?,
    val startTime: LocalDateTime?,
    val opponentTeamName: String?,
    val mapName: String?,
    val notes: String?,
    val status: ScrimStatus?
)

data class UpdateScrimStatusRequest(
    @field:NotNull(message = "상태는 필수입니다")
    val status: ScrimStatus
)

data class AddMatchResultRequest(
    @field:NotNull(message = "라운드 번호는 필수입니다")
    val round: Int,

    @field:NotNull(message = "등수는 필수입니다")
    val placement: Int,

    @field:NotNull(message = "킬 수는 필수입니다")
    val kills: Int,

    val notes: String?
)

data class ScrimResponse(
    val id: Long,
    val title: String,
    val startTime: LocalDateTime,
    val status: ScrimStatus,
    val matchCount: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
