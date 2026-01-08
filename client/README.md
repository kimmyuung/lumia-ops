# 🎨 Lumia Ops Frontend

Vue 3 + TypeScript + Vite 기반 프론트엔드 애플리케이션.

## 🚀 시작하기

```bash
npm install
npm run dev
```

## 📁 프로젝트 구조

```
src/
├── api/           # API 클라이언트 (Axios)
├── components/    # UI 컴포넌트
│   ├── common/   # 공통 컴포넌트 (Button, Modal, Toast 등)
│   ├── layout/   # 레이아웃 (Navbar, Footer)
│   ├── chat/     # 채팅 컴포넌트
│   └── team/     # 팀 관련 컴포넌트
├── composables/   # Vue Composables
│   ├── useAuth.ts
│   ├── useChat.ts
│   ├── useStompClient.ts
│   └── useTheme.ts
├── stores/        # Pinia 상태 관리
├── views/         # 페이지 컴포넌트
└── utils/         # 유틸리티 함수
```

## 🛠️ 주요 명령어

| 명령어 | 설명 |
|--------|------|
| `npm run dev` | 개발 서버 (http://localhost:5173) |
| `npm run build` | 프로덕션 빌드 |
| `npm run test` | 테스트 watch 모드 |
| `npm run test:run` | 테스트 단일 실행 |
| `npm run lint` | ESLint 검사 |
| `npm run type-check` | TypeScript 타입 검사 |

## ✨ 기술 스택

- **Framework:** Vue 3 (Composition API)
- **Language:** TypeScript
- **Build:** Vite
- **State:** Pinia
- **Router:** Vue Router
- **HTTP:** Axios
- **WebSocket:** @stomp/stompjs
- **Test:** Vitest + Vue Test Utils
- **Icons:** Lucide Vue

## 🎨 디자인 시스템

- CSS Variables 기반 테마
- 다크 모드 지원 (`useTheme` composable)
- Glassmorphism 스타일
- 반응형 레이아웃
