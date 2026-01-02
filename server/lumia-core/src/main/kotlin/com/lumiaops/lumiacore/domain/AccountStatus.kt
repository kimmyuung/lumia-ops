package com.lumiaops.lumiacore.domain

/**
 * 계정 상태
 */
enum class AccountStatus {
    PENDING_EMAIL,     // 이메일 인증 대기
    PENDING_NICKNAME,  // 닉네임 설정 대기 (이메일 인증 완료)
    ACTIVE,            // 정상 활성
    LOCKED,            // 로그인 5회 실패로 잠김
    DORMANT            // 6개월 이상 미로그인 휴면
}
