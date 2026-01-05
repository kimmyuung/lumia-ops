package com.lumiaops.lumiasocket.dto

import java.time.LocalDateTime

/**
 * 전략 맵 업데이트 메시지
 */
data class StrategyUpdateMessage(
    val strategyId: String,
    val sender: String,
    val senderId: Long,
    val action: StrategyAction,
    val data: String, // JSON 형태의 마커/경로/메모 데이터
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class StrategyAction {
    ADD_MARKER,
    REMOVE_MARKER,
    ADD_PATH,
    REMOVE_PATH,
    ADD_NOTE,
    REMOVE_NOTE,
    CLEAR_ALL,
    FULL_SYNC  // 전체 데이터 동기화
}
