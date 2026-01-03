---
description: 에러 코드 및 예외 처리 규칙
---

# ⚠️ 에러 처리 가이드

## 개요

Lumia Ops는 일관된 에러 응답 형식과 커스텀 예외 계층 구조를 사용합니다.

---

## 📋 에러 응답 형식

모든 에러는 다음 JSON 형식으로 반환됩니다:

```json
{
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "입력값이 올바르지 않습니다",
  "path": "/auth/register",
  "field": "email",
  "details": [
    {
      "field": "email",
      "message": "유효한 이메일 형식이 아닙니다",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

---

## 🏗️ 예외 계층 구조

```
DomainException (추상 클래스)
├── NotFoundException (404)
├── DuplicateException (409)
├── InvalidStateException (400)
├── ValidationException (422)
├── AuthenticationException (401)
└── ForbiddenException (403)
```

---

## 📝 에러 코드 목록

### 인증 (Authentication)

| 코드 | HTTP | 설명 |
|------|------|------|
| `AUTHENTICATION_REQUIRED` | 401 | 인증이 필요합니다 |
| `INVALID_CREDENTIALS` | 401 | 이메일 또는 비밀번호가 올바르지 않습니다 |
| `INVALID_TOKEN` | 401 | 유효하지 않은 토큰입니다 |
| `TOKEN_EXPIRED` | 401 | 토큰이 만료되었습니다 |
| `ACCOUNT_LOCKED` | 401 | 계정이 잠겼습니다 |
| `EMAIL_NOT_VERIFIED` | 403 | 이메일 인증이 필요합니다 |
| `ACCOUNT_INACTIVE` | 403 | 계정이 비활성화되었습니다 |

### 사용자 (User)

| 코드 | HTTP | 설명 |
|------|------|------|
| `USER_NOT_FOUND` | 404 | 사용자를 찾을 수 없습니다 |
| `EMAIL_ALREADY_EXISTS` | 409 | 이미 사용 중인 이메일입니다 |
| `NICKNAME_ALREADY_EXISTS` | 409 | 이미 사용 중인 닉네임입니다 |
| `NICKNAME_CHANGE_LIMIT` | 400 | 닉네임 변경은 한 달에 1회만 가능합니다 |
| `NICKNAME_ALREADY_SET` | 400 | 이미 닉네임이 설정되어 있습니다 |

### 팀 (Team)

| 코드 | HTTP | 설명 |
|------|------|------|
| `TEAM_NOT_FOUND` | 404 | 팀을 찾을 수 없습니다 |
| `TEAM_NAME_ALREADY_EXISTS` | 409 | 이미 사용 중인 팀 이름입니다 |
| `ALREADY_HAS_TEAM` | 400 | 이미 팀에 소속되어 있습니다 |
| `NOT_TEAM_MEMBER` | 403 | 팀 멤버가 아닙니다 |
| `INSUFFICIENT_PERMISSION` | 403 | 권한이 부족합니다 |
| `OWNER_CANNOT_LEAVE` | 400 | 팀장은 탈퇴할 수 없습니다 |
| `CANNOT_REMOVE_OWNER` | 400 | 팀장은 제거할 수 없습니다 |

### 초대 (Invitation)

| 코드 | HTTP | 설명 |
|------|------|------|
| `INVITATION_NOT_FOUND` | 404 | 초대를 찾을 수 없습니다 |
| `INVITATION_EXPIRED` | 400 | 초대가 만료되었습니다 |
| `INVITATION_ALREADY_ACCEPTED` | 400 | 이미 수락된 초대입니다 |
| `ALREADY_TEAM_MEMBER` | 400 | 이미 팀 멤버입니다 |
| `PENDING_INVITATION_EXISTS` | 409 | 이미 대기 중인 초대가 있습니다 |

### 스크림 (Scrim)

| 코드 | HTTP | 설명 |
|------|------|------|
| `SCRIM_NOT_FOUND` | 404 | 스크림을 찾을 수 없습니다 |
| `INVALID_SCRIM_STATUS` | 400 | 유효하지 않은 스크림 상태입니다 |

### 전략 (Strategy)

| 코드 | HTTP | 설명 |
|------|------|------|
| `STRATEGY_NOT_FOUND` | 404 | 전략을 찾을 수 없습니다 |

### 일반 (General)

| 코드 | HTTP | 설명 |
|------|------|------|
| `VALIDATION_ERROR` | 400 | 입력값 검증 실패 |
| `MALFORMED_REQUEST` | 400 | 요청 본문을 읽을 수 없습니다 |
| `MISSING_PARAMETER` | 400 | 필수 파라미터가 누락되었습니다 |
| `TYPE_MISMATCH` | 400 | 파라미터 타입이 올바르지 않습니다 |
| `METHOD_NOT_ALLOWED` | 405 | 지원하지 않는 HTTP 메서드입니다 |
| `INTERNAL_ERROR` | 500 | 서버 오류가 발생했습니다 |

---

## 🛠️ 예외 사용 방법

### Backend에서 예외 던지기

```kotlin
// NotFoundException
throw NotFoundException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다")

// DuplicateException
throw DuplicateException("EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다")

// ValidationException (필드 포함)
throw ValidationException("INVALID_EMAIL", "유효한 이메일 형식이 아닙니다", "email")

// AuthenticationException
throw AuthenticationException("ACCOUNT_LOCKED", "계정이 잠겼습니다")

// ForbiddenException
throw ForbiddenException("INSUFFICIENT_PERMISSION", "권한이 부족합니다")
```

### Frontend에서 에러 처리

```typescript
try {
  await authApi.login(email, password)
} catch (error) {
  const apiError = error as ApiError
  
  switch (apiError.code) {
    case 'INVALID_CREDENTIALS':
      toast.error('이메일 또는 비밀번호가 올바르지 않습니다')
      break
    case 'ACCOUNT_LOCKED':
      toast.error('계정이 잠겼습니다. 비밀번호를 재설정해주세요.')
      break
    default:
      toast.error(apiError.message || '로그인에 실패했습니다')
  }
}
```

---

## 📁 관련 파일

### Backend
- `GlobalExceptionHandler.kt` - 전역 예외 처리
- `DomainException.kt` - 커스텀 예외 기본 클래스
- `NotFoundException.kt`, `DuplicateException.kt`, ...
- `ErrorResponse.kt` - 에러 응답 DTO

### Frontend
- `api/client.ts` - API 클라이언트 에러 인터셉터
- `types/error.ts` - 에러 타입 정의
