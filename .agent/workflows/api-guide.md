---
description: REST API 엔드포인트 목록 및 사용법
---

# 📡 API 가이드

## 개요

Lumia Ops Backend는 REST API를 제공합니다.
기본 URL: `http://localhost:8080`

---

## 🔐 인증

모든 보호된 API는 JWT 토큰이 필요합니다.

```bash
Authorization: Bearer <access_token>
```

---

## 📋 API 엔드포인트

### 🔑 인증 (Auth)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/auth/register` | 회원가입 | ❌ |
| POST | `/auth/login` | 로그인 | ❌ |
| GET | `/auth/verify` | 이메일 인증 | ❌ |
| POST | `/auth/resend-verification` | 인증 메일 재발송 | ❌ |
| POST | `/auth/set-nickname` | 닉네임 설정 | ✅ |

### 🔒 비밀번호 (Password)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/password/forgot` | 비밀번호 찾기 | ❌ |
| POST | `/password/reset` | 비밀번호 재설정 | ❌ |
| GET | `/password/validate-token` | 토큰 유효성 확인 | ❌ |

### 👤 사용자 (User)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/users/me` | 내 정보 조회 | ✅ |
| PATCH | `/users/me/nickname` | 닉네임 변경 | ✅ |
| GET | `/users/{id}` | 사용자 조회 | ✅ |

### 👥 팀 (Team)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/teams` | 팀 목록 조회 | ✅ |
| POST | `/teams` | 팀 생성 | ✅ |
| GET | `/teams/my` | 내 팀 조회 | ✅ |
| GET | `/teams/{id}` | 팀 상세 조회 | ✅ |
| PATCH | `/teams/{id}` | 팀 수정 | ✅ |
| DELETE | `/teams/{id}` | 팀 삭제 | ✅ |
| DELETE | `/teams/{id}/members/me` | 팀 탈퇴 | ✅ |
| DELETE | `/teams/{id}/members/{memberId}` | 멤버 제거 | ✅ |
| PATCH | `/teams/{id}/members/{memberId}/role` | 역할 변경 | ✅ |

### 📨 초대 (Invitation)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/teams/{id}/invitations` | 초대 생성 | ✅ |
| GET | `/teams/{id}/invitations` | 팀 초대 목록 | ✅ |
| DELETE | `/teams/{id}/invitations/{invitationId}` | 초대 취소 | ✅ |
| POST | `/teams/{id}/invitations/{invitationId}/resend` | 초대 재발송 | ✅ |
| GET | `/invitations/pending` | 내게 온 초대 | ✅ |
| GET | `/invitations/{token}` | 초대 상세 | ❌ |
| POST | `/invitations/{token}/accept` | 초대 수락 | ✅ |
| POST | `/invitations/{token}/decline` | 초대 거절 | ✅ |

### ⚔️ 스크림 (Scrim)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/scrims` | 스크림 목록 | ✅ |
| POST | `/scrims` | 스크림 생성 | ✅ |
| GET | `/scrims/{id}` | 스크림 상세 | ✅ |
| PATCH | `/scrims/{id}` | 스크림 수정 | ✅ |
| DELETE | `/scrims/{id}` | 스크림 삭제 | ✅ |
| PATCH | `/scrims/{id}/status` | 상태 변경 | ✅ |
| POST | `/scrims/{id}/results` | 결과 추가 | ✅ |
| DELETE | `/scrims/{id}/results/{resultId}` | 결과 삭제 | ✅ |

### 📊 전략 (Strategy)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/strategies` | 전략 목록 | ✅ |
| POST | `/strategies` | 전략 생성 | ✅ |
| GET | `/strategies/{id}` | 전략 상세 | ✅ |
| PATCH | `/strategies/{id}` | 전략 수정 | ✅ |
| DELETE | `/strategies/{id}` | 전략 삭제 | ✅ |

### 📈 통계 (Statistics)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/statistics/teams/{id}` | 팀 통계 | ✅ |
| GET | `/statistics/teams/{id}/recent` | 최근 성적 | ✅ |
| GET | `/statistics/leaderboard` | 순위표 | ✅ |
| POST | `/statistics/calculate-score` | 점수 계산 | ✅ |
| GET | `/statistics/placement-points` | 순위별 점수 | ✅ |

---

## 📦 요청/응답 예시

### 로그인
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**응답:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "userId": 1,
  "email": "user@example.com",
  "nickname": "Player1",
  "status": "ACTIVE"
}
```

### 에러 응답
```json
{
  "status": 401,
  "error": "Unauthorized",
  "code": "INVALID_CREDENTIALS",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다",
  "path": "/auth/login"
}
```

---

## 🔧 HTTP 상태 코드

| 코드 | 설명 |
|------|------|
| 200 | 성공 |
| 201 | 생성 성공 |
| 400 | 잘못된 요청 |
| 401 | 인증 필요 |
| 403 | 권한 없음 |
| 404 | 리소스 없음 |
| 409 | 중복 (Conflict) |
| 422 | 유효성 검사 실패 |
| 500 | 서버 오류 |
