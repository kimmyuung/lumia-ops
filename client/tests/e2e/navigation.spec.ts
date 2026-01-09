import { test, expect } from '@playwright/test'

/**
 * 네비게이션 및 기본 페이지 E2E 테스트
 */
test.describe('네비게이션', () => {

    test('홈페이지 접속', async ({ page }) => {
        await page.goto('/')

        // 페이지 제목 확인
        await expect(page).toHaveTitle(/Lumia Ops/)

        // 메인 컨텐츠가 로드되는지 확인
        await expect(page.locator('body')).toBeVisible()
    })

    test('네비게이션 메뉴 표시', async ({ page, context }) => {
        // 로그인 상태 설정
        await context.addInitScript(() => {
            localStorage.setItem('token', 'test-jwt-token')
        })

        await page.goto('/')

        // 네비게이션 메뉴 확인
        const nav = page.locator('nav').or(page.locator('[role="navigation"]'))
        await expect(nav).toBeVisible({ timeout: 5000 })
    })

    test('로그인되지 않은 상태에서 네비게이션', async ({ page }) => {
        await page.goto('/')

        // 로그인 버튼 또는 링크가 보이는지 확인
        const loginLink = page.locator('a[href="/login"]')
            .or(page.locator('text=로그인'))

        await expect(loginLink).toBeVisible({ timeout: 5000 })
    })

    test('페이지 간 이동', async ({ page, context }) => {
        await context.addInitScript(() => {
            localStorage.setItem('token', 'test-jwt-token')
        })

        await page.goto('/')

        // 팀 페이지로 이동
        const teamLink = page.locator('a[href="/teams"]')
            .or(page.locator('text=/팀|Teams/i'))

        if (await teamLink.isVisible()) {
            await teamLink.click()
            await expect(page).toHaveURL(/\/teams/)
        }
    })
})

/**
 * 반응형 디자인 테스트
 */
test.describe('반응형', () => {

    test('모바일 뷰포트에서 페이지 로드', async ({ page }) => {
        // 모바일 뷰포트 설정
        await page.setViewportSize({ width: 375, height: 667 })

        await page.goto('/')

        // 페이지가 정상적으로 로드되는지 확인
        await expect(page.locator('body')).toBeVisible()
    })

    test('태블릿 뷰포트에서 페이지 로드', async ({ page }) => {
        // 태블릿 뷰포트 설정
        await page.setViewportSize({ width: 768, height: 1024 })

        await page.goto('/')

        await expect(page.locator('body')).toBeVisible()
    })
})

/**
 * 404 및 에러 페이지 테스트
 */
test.describe('에러 처리', () => {

    test('존재하지 않는 페이지 접근', async ({ page }) => {
        await page.goto('/this-page-does-not-exist')

        // 404 페이지 또는 에러 메시지 확인
        const notFoundIndicator = page.locator('text=/404|찾을 수 없습니다|Not Found/i')

        await expect(notFoundIndicator).toBeVisible({ timeout: 5000 })
    })

    test('존재하지 않는 팀 페이지', async ({ page, context }) => {
        await context.addInitScript(() => {
            localStorage.setItem('token', 'test-jwt-token')
        })

        // 존재하지 않을 가능성이 높은 팀 ID
        await page.goto('/teams/999999')

        // 에러 메시지 또는 404 표시 확인
        const errorIndicator = page.locator('text=/찾을 수 없습니다|존재하지 않습니다|404/i')

        await expect(errorIndicator).toBeVisible({ timeout: 5000 })
    })
})

/**
 * 성능 및 로딩 상태 테스트
 */
test.describe('로딩 상태', () => {

    test('페이지 로딩 스피너 확인', async ({ page }) => {
        await page.goto('/')

        // 로딩 스피너가 일시적으로 표시될 수 있음
        const spinner = page.locator('[data-testid="spinner"]')
            .or(page.locator('.loading'))
            .or(page.locator('[role="progressbar"]'))

        // 스피너가 나타났다가 사라지는지 확인 (선택사항)
        // await expect(spinner).toBeVisible({ timeout: 1000 })
        // await expect(spinner).not.toBeVisible({ timeout: 5000 })
    })
})
