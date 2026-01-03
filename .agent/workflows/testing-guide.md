---
description: 테스트 작성 및 실행 방법
---

# 🧪 테스트 가이드

## 개요

Lumia Ops는 Frontend와 Backend 모두 자동화된 테스트를 갖추고 있습니다.

---

## 🎨 Frontend 테스트

### 테스트 프레임워크
- **Vitest** - 테스트 러너
- **Vue Test Utils** - Vue 컴포넌트 테스트
- **Happy DOM** - DOM 시뮬레이션

### 테스트 실행

```bash
cd client

# 모든 테스트 실행
npm run test

# 단일 실행 (watch 모드 없이)
npm run test:run

# 특정 파일만 테스트
npm run test -- Button.spec.ts

# 커버리지 리포트
npm run test:coverage
```

### 테스트 파일 위치

```
client/src/
├── components/common/__tests__/
│   ├── Button.spec.ts
│   ├── Card.spec.ts
│   ├── Input.spec.ts
│   └── Modal.spec.ts
├── stores/__tests__/
│   ├── user.spec.ts
│   ├── team.spec.ts
│   └── scrim.spec.ts
├── views/__tests__/
│   ├── HomeView.spec.ts
│   ├── TeamView.spec.ts
│   ├── ScrimView.spec.ts
│   └── StrategyView.spec.ts
├── composables/__tests__/
│   └── useToast.spec.ts
└── utils/__tests__/
    └── formatters.spec.ts
```

### 테스트 작성 예시

```typescript
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import Button from '../Button.vue'

describe('Button', () => {
    it('should render slot content', () => {
        const wrapper = mount(Button, {
            slots: { default: 'Click me' }
        })
        expect(wrapper.text()).toContain('Click me')
    })

    it('should emit click event', async () => {
        const wrapper = mount(Button)
        await wrapper.trigger('click')
        expect(wrapper.emitted('click')).toBeTruthy()
    })
})
```

---

## ☕ Backend 테스트

### 테스트 프레임워크
- **JUnit 5** - 테스트 프레임워크
- **MockK** - Kotlin Mocking 라이브러리
- **Spring Boot Test** - 통합 테스트

### 테스트 실행

```bash
cd server

# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "*.UserServiceTest"

# 특정 모듈만 테스트
./gradlew :lumia-core:test
./gradlew :lumia-api:test

# 테스트 리포트 보기
# build/reports/tests/test/index.html
```

### 테스트 파일 위치

```
server/
├── lumia-core/src/test/kotlin/
│   ├── domain/
│   │   ├── UserTest.kt
│   │   ├── TeamTest.kt
│   │   └── ...
│   ├── service/
│   │   ├── UserServiceTest.kt
│   │   ├── TeamServiceTest.kt
│   │   ├── ScrimServiceTest.kt
│   │   └── StrategyServiceTest.kt
│   └── util/
│       └── ScoreCalculatorTest.kt
└── lumia-api/src/test/kotlin/
    └── controller/
        └── AuthControllerTest.kt
```

### 단위 테스트 예시 (Service)

```kotlin
@ExtendWith(MockKExtension::class)
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userService: UserService

    @Test
    fun `이메일로 사용자 조회 성공`() {
        // given
        val user = User(email = "test@example.com", password = "encoded")
        every { userRepository.findByEmail("test@example.com") } returns user

        // when
        val result = userService.findByEmail("test@example.com")

        // then
        assertNotNull(result)
        assertEquals("test@example.com", result?.email)
    }
}
```

### 통합 테스트 예시 (Controller)

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `로그인 성공 시 JWT 토큰 반환`() {
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"test@test.com","password":"pass123"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").exists())
    }
}
```

---

## 📝 테스트 작성 가이드

### 명명 규칙

**Frontend:**
```typescript
describe('ComponentName', () => {
    describe('rendering', () => { ... })
    describe('events', () => { ... })
    describe('states', () => { ... })
})
```

**Backend:**
```kotlin
@Test
fun `동작 설명_조건_예상결과`() { ... }

// 예시
fun `로그인 시_비밀번호 오류_실패 횟수 증가`() { ... }
```

### AAA 패턴

```kotlin
@Test
fun `테스트 설명`() {
    // Arrange (준비)
    val user = createTestUser()
    
    // Act (실행)
    val result = userService.findById(user.id)
    
    // Assert (검증)
    assertNotNull(result)
}
```

---

## 🔧 CI에서의 테스트

GitHub Actions에서 자동 실행됩니다.

```yaml
# Frontend
- name: Test
  run: npm run test:run

# Backend
- name: Run tests
  run: ./gradlew test
```
