import { test as base, expect, Page } from '@playwright/test'

/**
 * 인증된 사용자 Fixture
 * 로그인 상태를 자동으로 설정
 */
type AuthFixtures = {
  authenticatedPage: Page
}

export const test = base.extend<AuthFixtures>({
  authenticatedPage: async ({ page, context }, use) => {
    // localStorage에 토큰 설정
    await context.addInitScript(() => {
      localStorage.setItem('token', 'test-jwt-token')
      localStorage.setItem('refreshToken', 'test-refresh-token')
      localStorage.setItem('userId', '1')
    })

    // 인증된 페이지 제공
    await use(page)
  }
})

export { expect }

/**
 * 실제 API 로그인 Fixture (선택사항)
 * 실제 백엔드 API를 호출하여 토큰 발급
 */
export const authenticatedTest = base.extend<AuthFixtures>({
  authenticatedPage: async ({ page, context }, use) => {
    // 실제 로그인 API 호출
    const response = await page.request.post('http://localhost:8080/api/auth/login', {
      data: {
        email: 'test@example.com',
        password: 'password'
      }
    })

    if (response.ok()) {
      const { token, refreshToken } = await response.json()

      // 토큰을 localStorage에 설정
      await context.addInitScript(
        ({ token, refreshToken }) => {
          localStorage.setItem('token', token)
          localStorage.setItem('refreshToken', refreshToken)
        },
        { token, refreshToken }
      )
    } else {
      console.warn('로그인 실패, Mock 토큰 사용')
      await context.addInitScript(() => {
        localStorage.setItem('token', 'mock-jwt-token')
      })
    }

    await use(page)
  }
})
