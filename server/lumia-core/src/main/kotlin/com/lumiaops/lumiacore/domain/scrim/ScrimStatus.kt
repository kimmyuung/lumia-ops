package com.lumiaops.lumiacore.domain.scrim

/**
 * 스크림 상태
 */
enum class ScrimStatus {
    SCHEDULED,    // 예정됨
    IN_PROGRESS,  // 진행 중
    FINISHED,     // 종료됨
    CANCELLED     // 취소됨
}
