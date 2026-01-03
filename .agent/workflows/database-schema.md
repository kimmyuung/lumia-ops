---
description: DB 스키마, 엔티티 관계도
---

# 🗄️ 데이터베이스 스키마 가이드

## 개요

Lumia Ops는 JPA/Hibernate를 사용하며, 개발 시 H2, 프로덕션 시 PostgreSQL을 사용합니다.

---

## 📊 엔티티 관계도

```mermaid
erDiagram
    User ||--o{ TeamMember : "belongs to"
    User ||--o{ EmailVerification : "has"
    Team ||--|{ TeamMember : "contains"
    Team ||--o{ TeamInvitation : "has"
    Team ||--o{ Scrim : "has"
    Team ||--o{ Strategy : "has"
    Scrim ||--o{ ScrimMatch : "contains"
    ScrimMatch ||--o{ MatchResult : "has"

    User {
        Long id PK
        String email UK
        String password
        String nickname
        String profileImageUrl
        UserRole role
        AccountStatus status
        Int failedLoginAttempts
        LocalDateTime lockedAt
        LocalDateTime lastLoginAt
        LocalDateTime nicknameChangedAt
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    Team {
        Long id PK
        String name
        String description
        String logoUrl
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    TeamMember {
        Long id PK
        Long teamId FK
        Long userId FK
        TeamRole role
        String position
        LocalDateTime joinedAt
    }

    TeamInvitation {
        Long id PK
        Long teamId FK
        Long inviterId FK
        String inviteeEmail
        String token UK
        InvitationStatus status
        LocalDateTime expiresAt
        LocalDateTime createdAt
    }

    Scrim {
        Long id PK
        Long teamId FK
        String title
        LocalDateTime scheduledAt
        ScrimStatus status
        String opponentTeamName
        String mapName
        String notes
        LocalDateTime createdAt
    }

    ScrimMatch {
        Long id PK
        Long scrimId FK
        Int round
        String mapName
        LocalDateTime playedAt
    }

    MatchResult {
        Long id PK
        Long scrimMatchId FK
        Int placement
        Int kills
        Int score
        String notes
    }

    Strategy {
        Long id PK
        Long teamId FK
        Long creatorId FK
        String title
        String description
        String mapName
        String content
        StrategyVisibility visibility
        LocalDateTime createdAt
        LocalDateTime updatedAt
    }

    EmailVerification {
        Long id PK
        Long userId FK
        String token UK
        VerificationType type
        Boolean used
        LocalDateTime expiresAt
        LocalDateTime createdAt
    }
```

---

## 📋 Enum 타입

### UserRole
| 값 | 설명 |
|----|------|
| `USER` | 일반 사용자 |
| `ADMIN` | 시스템 관리자 |

### AccountStatus
| 값 | 설명 |
|----|------|
| `PENDING_VERIFICATION` | 이메일 인증 대기 |
| `PENDING_NICKNAME` | 닉네임 설정 대기 |
| `ACTIVE` | 활성 |
| `LOCKED` | 잠금 |
| `INACTIVE` | 비활성 (6개월 미접속) |

### TeamRole
| 값 | 설명 |
|----|------|
| `OWNER` | 팀장 |
| `ADMIN` | 관리자 |
| `MEMBER` | 멤버 |

### InvitationStatus
| 값 | 설명 |
|----|------|
| `PENDING` | 대기 |
| `ACCEPTED` | 수락됨 |
| `DECLINED` | 거절됨 |
| `EXPIRED` | 만료됨 |
| `CANCELLED` | 취소됨 |

### ScrimStatus
| 값 | 설명 |
|----|------|
| `SCHEDULED` | 예정됨 |
| `IN_PROGRESS` | 진행 중 |
| `COMPLETED` | 완료됨 |
| `CANCELLED` | 취소됨 |

### StrategyVisibility
| 값 | 설명 |
|----|------|
| `PUBLIC` | 공개 |
| `TEAM` | 팀 내 공개 |
| `PRIVATE` | 비공개 |

---

## 🔧 인덱스

### 자주 조회되는 컬럼
- `User.email` - 로그인 시 조회
- `TeamMember.userId` - 사용자의 팀 조회
- `TeamMember.teamId` - 팀의 멤버 목록
- `TeamInvitation.token` - 초대 수락/거절
- `Scrim.teamId` - 팀의 스크림 목록
- `Strategy.teamId` - 팀의 전략 목록

---

## 📁 관련 파일

### 엔티티
```
lumia-core/src/main/kotlin/com/lumiaops/lumiacore/domain/
├── User.kt
├── Team.kt
├── TeamMember.kt
├── TeamInvitation.kt
├── Scrim.kt
├── ScrimMatch.kt
├── MatchResult.kt
├── Strategy.kt
└── EmailVerification.kt
```

### 리포지토리
```
lumia-core/src/main/kotlin/com/lumiaops/lumiacore/repository/
├── UserRepository.kt
├── TeamRepository.kt
├── TeamMemberRepository.kt
├── TeamInvitationRepository.kt
├── ScrimRepository.kt
├── ScrimMatchRepository.kt
├── MatchResultRepository.kt
├── StrategyRepository.kt
└── EmailVerificationRepository.kt
```
