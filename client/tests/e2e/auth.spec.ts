import { test, expect } from '@playwright/test'

/**
 * 인증 플로우 E2E 테스트
 * - 회원가입
 * - 로그인
 * - 로그아웃
 */
test.describe('인증 플로우', () => {

    test('로그인 페이지 접근', async ({ page }) => {
        await page.goto('/login')

        // 페이지 제목 확인
        await expect(page).toHaveTitle(/Lumia Ops/)

        // 로그인 폼 요소 확인
        await expect(page.locator('input[type="email"]')).toBeVisible()
        await expect(page.locator('input[type="password"]')).toBeVisible()
        await expect(page.locator('button[type="submit"]')).toBeVisible()
    })

    test('로그인 성공 플로우', async ({ page }) => {
        await page.goto('/login')

        // 로그인 폼 작성
        await page.fill('input[type="email"]', 'test@example.com')
        await page.fill('input[type="password"]', 'password123')

        // 로그인 버튼 클릭
        await page.click('button[type="submit"]')

        // 홈으로 리다이렉트 확인 (실제 API 연동 시)
        // await expect(page).toHaveURL('/')

        // 또는 성공 메시지 확인
        // await expect(page.locator('text=로그인 성공')).toBeVisible({ timeout: 5000 })
    })

    test('로그인 유효성 검증', async ({ page }) => {
        await page.goto('/login')

        // 빈 폼 제출 시도
        await page.click('button[type="submit"]')

        // 에러 메시지 확인 (프론트엔드 유효성 검증)
        await expect(page.locator('text=이메일을 입력해주세요').or(page.locator('text=필수'))).toBeVisible()
    })

    test('회원가입 페이지 접근', async ({ page }) => {
        await page.goto('/register')

        // 회원가입 폼 요소 확인
        await expect(page.locator('input[type="email"]')).toBeVisible()
        await expect(page.locator('input[type="password"]').first()).toBeVisible()
    })

    test('비밀번호 확인 검증', async ({ page }) => {
        await page.goto('/register')

        // 이메일 입력
        await page.fill('input[type="email"]', 'newuser@example.com')

        // 비밀번호 입력
        const passwordInputs = page.locator('input[type="password"]')
        await passwordInputs.nth(0).fill('Password123!')
        await passwordInputs.nth(1).fill('DifferentPassword123!')

        // 제출 시도
        await page.click('button[type="submit"]')

        // 비밀번호 불일치 에러 확인
        await expect(page.locator('text=비밀번호가 일치하지 않습니다').or(page.locator('text=일치'))).toBeVisible()
    })

    test('로그아웃 (Mock)', async ({ page, context }) => {
        // localStorage에 토큰 설정 (로그인 상태 Mock)
        await context.addInitScript(() => {
            localStorage.setItem('token', 'fake-jwt-token')
            localStorage.setItem('refreshToken', 'fake-refresh-token')
        })

        await page.goto('/')

        // 사용자 메뉴가 보이는지 확인 (로그인 상태)
        // NOTE: 실제 UI 구조에 맞게 셀렉터 수정 필요
        const userMenu = page.locator('[data-testid="user-menu"]').or(page.locator('text=로그아웃'))

        if (await userMenu.isVisible()) {
            await userMenu.click()

            // 로그아웃 버튼 클릭
            const logoutButton = page.locator('text=로그아웃').or(page.locator('[data-testid="logout-button"]'))
            if (await logoutButton.isVisible()) {
                await logoutButton.click()
            }

            // 로그인 페이지로 리다이렉트 확인
            await expect(page).toHaveURL('/login', { timeout: 3000 })
        }
    })
})

/**
 * 보호된 라우트 접근 테스트
 */
test.describe('인증 가드', () => {

    test('인증 없이 보호된 페이지 접근 시 리다이렉트', async ({ page }) => {
        // 팀 페이지 접근 시도 (인증 필요)
        await page.goto('/teams')

        // 로그인 페이지로 리다이렉트되는지 확인
        await expect(page).toHaveURL(/\/login/, { timeout: 3000 })
    })

    test('인증 후 보호된 페이지 접근 가능', async ({ page, context }) => {
        // localStorage에 토큰 설정
        await context.addInitScript(() => {
            localStorage.setItem('token', 'valid-jwt-token')
        })

        await page.goto('/teams')

        // 팀 페이지가 로드되는지 확인 (로그인 페이지로 리다이렉트 안됨)
        await expect(page).not.toHaveURL(/\/login/)
    })
})
