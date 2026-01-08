# 🎮 Lumia Ops

팀 기반 e스포츠 매니지먼트 플랫폼. 스크림 관리, 전략 공유, 실시간 채팅 기능을 제공합니다.

## 🏗️ 아키텍처

```
lumia-ops/
├── client/                 # Vue 3 + TypeScript + Vite
│   ├── src/
│   │   ├── api/           # API 클라이언트
│   │   ├── components/    # UI 컴포넌트
│   │   ├── composables/   # Vue Composables
│   │   ├── stores/        # Pinia 상태 관리
│   │   └── views/         # 페이지 컴포넌트
│   └── package.json
│
└── server/                 # Spring Boot + Kotlin
    ├── lumia-api/         # REST API 모듈 (포트: 8080)
    ├── lumia-core/        # 핵심 비즈니스 로직
    └── lumia-socket/      # WebSocket 모듈 (포트: 8081)
```

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| **인증** | JWT 기반 인증, 이메일 인증, 비밀번호 재설정 |
| **팀 관리** | 팀 생성, 멤버 초대, 역할 관리 |
| **스크림** | 스크림 일정, 결과 기록, 통계 |
| **전략** | 전략 문서 작성, 실시간 동기화 |
| **채팅** | STOMP WebSocket 실시간 채팅 |

## 🚀 시작하기

### 요구사항
- **Node.js** 20+
- **JDK** 21
- **Gradle** 8+

### Frontend

```bash
cd client
npm install
npm run dev        # http://localhost:5173
```

### Backend

```bash
cd server
./gradlew :lumia-api:bootRun      # REST API (8080)
./gradlew :lumia-socket:bootRun   # WebSocket (8081)
```

## 🔧 환경 변수

### Frontend (`client/.env`)
```env
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8081/ws
```

### Backend (`server/lumia-api/src/main/resources/application.properties`)
```properties
spring.profiles.active=dev
jwt.secret=your-secret-key
spring.mail.host=smtp.gmail.com
```

## 📚 문서

상세 문서는 `.agent/workflows/` 디렉토리를 참조하세요:

| 문서 | 설명 |
|------|------|
| [/api-guide](.agent/workflows/api-guide.md) | REST API 엔드포인트 |
| [/auth-flow](.agent/workflows/auth-flow.md) | 인증 시스템 |
| [/database-schema](.agent/workflows/database-schema.md) | DB 스키마 |
| [/websocket-guide](.agent/workflows/websocket-guide.md) | WebSocket 채팅 |
| [/ci-guide](.agent/workflows/ci-guide.md) | CI 파이프라인 |
| [/testing-guide](.agent/workflows/testing-guide.md) | 테스트 가이드 |

## 🧪 테스트

```bash
# Frontend
cd client
npm run test:run     # 단일 실행
npm run test         # watch 모드

# Backend
cd server
./gradlew test
```

## 📦 빌드

```bash
# Frontend
cd client
npm run build        # dist/ 폴더에 빌드

# Backend
cd server
./gradlew build -x test
```

## 🔄 CI/CD

GitHub Actions로 자동화:
- **트리거:** `main`, `dev` 브랜치 push/PR
- **파이프라인:** Lint → Type Check → Test → Build

## 📝 라이선스

MIT License
