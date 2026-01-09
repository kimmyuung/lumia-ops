import { test, expect } from './fixtures/auth'

/**
 * 인증 Fixture 사용 예시
 * authenticatedPage는 자동으로 로그인된 상태
 */
test.describe('Fixture 사용 예시', () => {

    test('로그인 상태에서 팀 목록 조회', async ({ authenticatedPage }) => {
        // authenticatedPage는 이미 토큰이 설정된 상태
        await authenticatedPage.goto('/teams')

        // 로그인 페이지로 리다이렉트되지 않음
        await expect(authenticatedPage).not.toHaveURL(/\/login/)

        // 팀 페이지 컨텐츠 확인
        await expect(authenticatedPage.locator('h1, h2').filter({ hasText: /팀/ })).toBeVisible()
    })

    test('로그인 상태에서 프로필 접근', async ({ authenticatedPage }) => {
        await authenticatedPage.goto('/profile')

        // 인증된 사용자만 접근 가능
        await expect(authenticatedPage).not.toHaveURL(/\/login/)
    })
})
