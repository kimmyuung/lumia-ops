---
description: 환경 변수 및 프로필 설정 가이드
---

# ⚙️ 환경 설정 가이드

## 개요

Lumia Ops는 3개의 환경 프로필을 지원합니다.

| 프로필 | 용도 | 데이터베이스 |
|--------|------|-------------|
| `local` | 로컬 개발 | H2 (인메모리) |
| `dev` | 개발 서버 | PostgreSQL (개발) |
| `prod` | 프로덕션 | PostgreSQL (운영) |

---

## 🚀 프로필별 실행

```bash
# 로컬 개발 (기본값)
./gradlew :lumia-api:bootRun

# 개발 서버
./gradlew :lumia-api:bootRun --args='--spring.profiles.active=dev'

# 프로덕션
java -jar lumia-api.jar --spring.profiles.active=prod
```

환경 변수로 설정:
```bash
# PowerShell
$env:SPRING_PROFILES_ACTIVE="local"; ./gradlew :lumia-api:bootRun

# Bash
SPRING_PROFILES_ACTIVE=local ./gradlew :lumia-api:bootRun
```

---

## 📋 환경 변수 목록

### 필수 환경 변수 (프로덕션)

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `JWT_SECRET` | JWT 서명 키 (Base64) | `bHVtaWEtb3BzLXNlY3VyZS1qd3QtLi4u` |
| `DATABASE_URL` | PostgreSQL 연결 URL | `jdbc:postgresql://localhost:5432/lumiadb` |
| `DATABASE_USERNAME` | DB 사용자명 | `lumia` |
| `DATABASE_PASSWORD` | DB 비밀번호 | `password123` |
| `MAIL_USERNAME` | SMTP 사용자명 | `your-email@gmail.com` |
| `MAIL_PASSWORD` | SMTP 앱 비밀번호 | `xxxx xxxx xxxx xxxx` |

### 선택 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로필 | `local` |
| `JWT_EXPIRATION_MS` | Access 토큰 만료 (ms) | `3600000` (1시간) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh 토큰 만료 (ms) | `604800000` (7일) |
| `CORS_ALLOWED_ORIGINS` | CORS 허용 Origin | `http://localhost:5173` |
| `APP_BASE_URL` | 프론트엔드 URL | `http://localhost:5173` |

### OAuth2 환경 변수

| 변수명 | 설명 |
|--------|------|
| `KAKAO_CLIENT_ID` | Kakao OAuth 클라이언트 ID |
| `KAKAO_CLIENT_SECRET` | Kakao OAuth 시크릿 |
| `KAKAO_REDIRECT_URI` | Kakao 콜백 URL |
| `STEAM_API_KEY` | Steam Web API 키 |

### 외부 API

| 변수명 | 설명 |
|--------|------|
| `ER_API_KEY` | Eternal Return Open API 키 |

---

## 📁 설정 파일 구조

```
server/lumia-api/src/main/resources/
├── application.properties          # 공통 설정
├── application-local.properties    # 로컬 개발
├── application-dev.properties      # 개발 서버
└── application-prod.properties     # 프로덕션
```

### 프로필별 주요 차이점

| 설정 | Local | Dev | Prod |
|------|-------|-----|------|
| 데이터베이스 | H2 인메모리 | PostgreSQL | PostgreSQL |
| DDL 자동화 | `create-drop` | `update` | `validate` |
| SQL 로깅 | `true` | `false` | `false` |
| 로그 레벨 | `DEBUG` | `INFO` | `WARN` |
| Rate Limiting | 1000 req/min | 200 req/min | 100 req/min |
| H2 Console | ✅ 활성화 | ❌ | ❌ |

---

## 🖥️ 프론트엔드 환경 변수

프론트엔드(Vite)는 `.env.*` 파일을 사용합니다.

```bash
# .env.local (로컬 개발)
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8081/ws

# .env.development (개발 서버)
VITE_API_URL=https://dev-api.lumia-ops.com/api
VITE_WS_URL=https://dev-ws.lumia-ops.com/ws

# .env.production (프로덕션)
VITE_API_URL=https://api.lumia-ops.com/api
VITE_WS_URL=https://ws.lumia-ops.com/ws
```

---

## 🔐 보안 권장사항

> [!CAUTION]
> - 프로덕션에서 `JWT_SECRET`은 반드시 환경 변수로 설정
> - `.env.local` 파일은 Git에 커밋하지 않음 (`.gitignore`에 포함)
> - CORS에 와일드카드(`*`) 사용 금지

---

## 📁 관련 파일

| 파일 | 설명 |
|------|------|
| `application.properties` | [공통 설정](file:///c:/workspace/lumia-ops/server/lumia-api/src/main/resources/application.properties) |
| `application-local.properties` | [로컬 설정](file:///c:/workspace/lumia-ops/server/lumia-api/src/main/resources/application-local.properties) |
| `application-prod.properties` | [프로덕션 설정](file:///c:/workspace/lumia-ops/server/lumia-api/src/main/resources/application-prod.properties) |
| `.env.example` | [프론트엔드 환경 변수 예시](file:///c:/workspace/lumia-ops/client/.env.example) |
