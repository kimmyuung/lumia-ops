---
description: 시스템 아키텍처 및 주요 플로우 다이어그램
---

# 🏗️ 아키텍처 가이드

## 시스템 개요

Lumia Ops는 팀 기반 e스포츠 매니지먼트 플랫폼입니다.

---

## 📦 모듈 구조

```mermaid
graph TB
    subgraph Client["🖥️ Client (Vue 3)"]
        FE[Frontend<br/>localhost:5173]
    end

    subgraph Server["☕ Server (Spring Boot)"]
        API[lumia-api<br/>REST API<br/>:8080]
        SOCKET[lumia-socket<br/>WebSocket<br/>:8081]
        CORE[lumia-core<br/>비즈니스 로직]
    end

    subgraph Data["💾 Data"]
        H2[(H2<br/>개발)]
        PG[(PostgreSQL<br/>프로덕션)]
        REDIS[(Redis<br/>캐시)]
    end

    FE -->|HTTP| API
    FE -->|STOMP| SOCKET
    API --> CORE
    SOCKET --> CORE
    CORE --> H2
    CORE --> PG
    CORE --> REDIS
```

---

## 🔐 인증 플로우

### JWT 로그인 플로우

```mermaid
sequenceDiagram
    participant C as Client
    participant A as API Server
    participant DB as Database

    C->>A: POST /auth/login {email, password}
    A->>DB: 사용자 조회
    DB-->>A: User
    A->>A: 비밀번호 검증
    A->>A: Access/Refresh Token 생성
    A-->>C: {token, refreshToken, user}
    
    Note over C: localStorage에 토큰 저장
    
    C->>A: GET /users/me (Authorization: Bearer token)
    A->>A: 토큰 검증
    A->>DB: 사용자 조회
    A-->>C: UserResponse
```

### 토큰 갱신 플로우

```mermaid
sequenceDiagram
    participant C as Client
    participant A as API Server

    Note over C: Access Token 만료 5분 전

    C->>A: POST /auth/refresh {refreshToken}
    A->>A: Refresh Token 검증
    A->>A: 새 Access/Refresh Token 생성
    A-->>C: {token, refreshToken}
    
    Note over C: 새 토큰으로 교체
```

### OAuth2 로그인 플로우 (Kakao)

```mermaid
sequenceDiagram
    participant C as Client
    participant A as API Server
    participant K as Kakao OAuth

    C->>A: GET /oauth/kakao/login
    A-->>C: Redirect URL (Kakao 인증 페이지)
    C->>K: 사용자 로그인
    K-->>C: Redirect with code
    C->>A: GET /oauth/kakao/callback?code=xxx
    A->>K: POST /token (code 교환)
    K-->>A: Access Token
    A->>K: GET /user (사용자 정보)
    K-->>A: {kakaoId, nickname, email}
    
    alt 신규 사용자
        A->>A: 사용자 생성 (PENDING_NICKNAME)
        A-->>C: {needsNickname: true, tempToken}
        C->>A: POST /oauth/complete {nickname}
        A-->>C: {token, refreshToken, user}
    else 기존 사용자
        A->>A: Access/Refresh Token 생성
        A-->>C: {token, refreshToken, user}
    end
```

### OAuth2 로그인 플로우 (Steam)

```mermaid
sequenceDiagram
    participant C as Client
    participant A as API Server
    participant S as Steam OpenID

    C->>A: GET /oauth/steam/login
    A-->>C: Redirect URL (Steam 인증 페이지)
    C->>S: 사용자 로그인
    S-->>C: Redirect with OpenID params
    C->>A: GET /oauth/steam/callback?params
    A->>S: OpenID 검증 요청
    S-->>A: 검증 결과 + SteamID
    A->>A: SteamID로 사용자 조회/생성
    A-->>C: {token, refreshToken, user}
```

---

## 🚦 Rate Limiting 플로우

### Token Bucket 알고리즘

```mermaid
flowchart LR
    subgraph Request["요청 처리"]
        R[HTTP Request] --> IP[IP 추출]
        IP --> WL{화이트리스트?}
        WL -->|Yes| PASS[통과]
        WL -->|No| SE{민감 엔드포인트?}
        SE -->|Yes| SB[민감 Bucket 체크]
        SE -->|No| GB[일반 Bucket 체크]
        SB --> TC{토큰 있음?}
        GB --> TC
        TC -->|Yes| PASS
        TC -->|No| REJECT[429 응답]
    end
```

### 민감 엔드포인트 제한

```mermaid
graph TB
    subgraph Limits["엔드포인트별 제한"]
        L1["/auth/login<br/>10 req/min"]
        L2["/auth/register<br/>5 req/min"]
        L3["/password/forgot<br/>3 req/min"]
        L4["/password/reset<br/>3 req/min"]
    end
    
    subgraph General["일반 API"]
        G1["일반 엔드포인트<br/>local: 1000/min<br/>dev: 200/min<br/>prod: 100/min"]
    end
```

### 응답 헤더

| 헤더 | 설명 |
|------|------|
| `X-Rate-Limit-Limit` | 허용된 최대 요청 수 |
| `X-Rate-Limit-Remaining` | 남은 요청 수 |
| `X-Rate-Limit-Retry-After-Seconds` | 재시도까지 대기 시간 |

---

## 📡 WebSocket 아키텍처

### STOMP 연결 플로우

```mermaid
sequenceDiagram
    participant C as Client
    participant WS as WebSocket Server
    participant A as API Server

    C->>WS: CONNECT (Authorization: Bearer token)
    WS->>A: 토큰 검증
    A-->>WS: User 정보
    WS-->>C: CONNECTED

    C->>WS: SUBSCRIBE /user/{userId}/notification
    C->>WS: SUBSCRIBE /topic/team/{teamId}/chat

    WS-->>C: MESSAGE (새 알림)
    WS-->>C: MESSAGE (채팅)
```

### WebSocket 토픽

| 목적지 | 설명 |
|--------|------|
| `/user/{userId}/notification` | 개인 알림 |
| `/topic/team/{teamId}/chat` | 팀 채팅 |
| `/topic/strategy/{strategyId}` | 전략 실시간 동기화 |

---

## 🗃️ 데이터베이스 ERD

```mermaid
erDiagram
    USER {
        bigint id PK
        string email UK
        string password
        string nickname
        string status
        string steam_id
        bigint kakao_id
        string game_nickname
    }

    TEAM {
        bigint id PK
        string name
        string description
        bigint owner_id FK
    }

    TEAM_MEMBER {
        bigint id PK
        bigint team_id FK
        bigint user_id FK
        string role
    }

    SCRIM {
        bigint id PK
        string title
        datetime start_time
        string status
    }

    SCRIM_MATCH {
        bigint id PK
        bigint scrim_id FK
        int round_number
        string game_id
    }

    MATCH_RESULT {
        bigint id PK
        bigint match_id FK
        bigint team_id FK
        int rank
        int kill_count
        int total_score
    }

    STRATEGY {
        bigint id PK
        bigint team_id FK
        bigint author_id FK
        string title
        text map_data
    }

    USER ||--o{ TEAM_MEMBER : "belongs to"
    TEAM ||--o{ TEAM_MEMBER : "has"
    TEAM ||--o{ MATCH_RESULT : "participates"
    SCRIM ||--o{ SCRIM_MATCH : "contains"
    SCRIM_MATCH ||--o{ MATCH_RESULT : "has"
    TEAM ||--o{ STRATEGY : "owns"
    USER ||--o{ STRATEGY : "creates"
```

---

## 📁 디렉토리 구조

```
lumia-ops/
├── client/                      # Vue 3 Frontend
│   ├── src/
│   │   ├── api/                 # API 클라이언트
│   │   ├── components/          # Vue 컴포넌트
│   │   ├── composables/         # Vue Composables
│   │   ├── stores/              # Pinia 상태 관리
│   │   ├── views/               # 페이지 컴포넌트
│   │   └── utils/               # 유틸리티
│   └── package.json
│
├── server/                      # Spring Boot Backend
│   ├── lumia-api/               # REST API 모듈
│   │   ├── controller/          # 컨트롤러
│   │   ├── dto/                 # 요청/응답 DTO
│   │   └── security/            # 보안 설정
│   │
│   ├── lumia-core/              # 핵심 비즈니스 모듈
│   │   ├── domain/              # 엔티티
│   │   ├── repository/          # JPA Repository
│   │   └── service/             # 비즈니스 로직
│   │
│   └── lumia-socket/            # WebSocket 모듈
│       ├── controller/          # STOMP 컨트롤러
│       └── config/              # WebSocket 설정
│
└── .github/workflows/           # CI/CD
    └── ci.yml
```

---

## 🔧 기술 스택

| 영역 | 기술 |
|------|------|
| **Frontend** | Vue 3, TypeScript, Vite, Pinia |
| **Backend** | Spring Boot 3, Kotlin, JPA |
| **Database** | H2 (개발), PostgreSQL (프로덕션) |
| **Cache** | Redis |
| **WebSocket** | STOMP over SockJS |
| **Auth** | JWT (Access + Refresh Token) |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **CI/CD** | GitHub Actions |
