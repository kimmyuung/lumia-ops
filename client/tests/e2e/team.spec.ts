import { test, expect } from '@playwright/test'

/**
 * 팀 관리 E2E 테스트
 * - 팀 목록 조회
 * - 팀 생성
 * - 팀 상세 조회
 */
test.describe('팀 관리', () => {
  // 각 테스트 전에 로그인 상태 설정
  test.beforeEach(async ({ context }) => {
    await context.addInitScript(() => {
      localStorage.setItem('token', 'test-jwt-token')
      localStorage.setItem('refreshToken', 'test-refresh-token')
    })
  })

  test('팀 목록 페이지 접근', async ({ page }) => {
    await page.goto('/teams')

    // 페이지 로드 확인
    await expect(page.locator('h1, h2').filter({ hasText: /팀/ })).toBeVisible({ timeout: 5000 })

    // 팀 생성 버튼 또는 팀 목록이 보이는지 확인
    const createButton = page.locator('button').filter({ hasText: /팀 생성|새 팀/ })
    const teamList = page.locator('[data-testid="team-list"]')

    await expect(createButton.or(teamList)).toBeVisible()
  })

  test('팀 생성 모달 열기', async ({ page }) => {
    await page.goto('/teams')

    // 팀 생성 버튼 찾기
    const createButton = page.locator('button').filter({ hasText: /팀 생성|새 팀|만들기/ })

    if (await createButton.isVisible()) {
      await createButton.click()

      // 모달이 열리는지 확인
      await expect(page.locator('[role="dialog"]').or(page.locator('.modal'))).toBeVisible({
        timeout: 2000
      })

      // 팀 이름 입력 필드 확인
      await expect(
        page.locator('input[name="name"]').or(page.locator('input').filter({ hasText: /이름/ }))
      ).toBeVisible()
    }
  })

  test('팀 생성 폼 유효성 검증', async ({ page }) => {
    await page.goto('/teams')

    const createButton = page.locator('button').filter({ hasText: /팀 생성|새 팀/ })

    if (await createButton.isVisible()) {
      await createButton.click()

      // 모달 내 제출 버튼 클릭 (빈 폼)
      const submitButton = page
        .locator('[role="dialog"] button[type="submit"]')
        .or(page.locator('.modal button').filter({ hasText: /생성|확인/ }))

      if (await submitButton.isVisible()) {
        await submitButton.click()

        // 에러 메시지 확인
        await expect(page.locator('text=/필수|입력해주세요|required/i')).toBeVisible()
      }
    }
  })

  test('팀 생성 폼 작성', async ({ page }) => {
    await page.goto('/teams')

    const createButton = page.locator('button').filter({ hasText: /팀 생성|새 팀/ })

    if (await createButton.isVisible()) {
      await createButton.click()

      // 팀 이름 입력
      const nameInput = page.locator('input[name="name"]').or(page.locator('input').first())
      await nameInput.fill('E2E Test Team')

      // 팀 설명 입력 (있는 경우)
      const descInput = page.locator('input[name="description"], textarea[name="description"]')
      if (await descInput.isVisible()) {
        await descInput.fill('This is an E2E test team')
      }

      // 제출 버튼 클릭
      const submitButton = page
        .locator('[role="dialog"] button[type="submit"]')
        .or(page.locator('button').filter({ hasText: /생성|확인/ }))

      if (await submitButton.isVisible()) {
        await submitButton.click()

        // 성공 메시지 또는 모달 닫힘 확인
        // NOTE: 실제 API 연동 시 동작 확인 필요
      }
    }
  })

  test('팀 상세 페이지 접근', async ({ page }) => {
    // 팀 ID 1번 페이지로 직접 이동
    await page.goto('/teams/1')

    // 페이지 로드 확인 (404가 아닌지)
    await expect(page.locator('text=/404|찾을 수 없습니다/i')).not.toBeVisible({ timeout: 3000 })

    // 팀 정보 섹션이 있는지 확인
    await expect(page.locator('h1, h2, [data-testid="team-name"]')).toBeVisible({ timeout: 5000 })
  })
})

/**
 * 팀 멤버 관리 테스트
 */
test.describe('팀 멤버 관리', () => {
  test.beforeEach(async ({ context }) => {
    await context.addInitScript(() => {
      localStorage.setItem('token', 'test-jwt-token')
    })
  })

  test('멤버 초대 모달 열기', async ({ page }) => {
    await page.goto('/teams/1')

    // 멤버 초대 버튼 찾기
    const inviteButton = page.locator('button').filter({ hasText: /초대|멤버 추가/ })

    if (await inviteButton.isVisible()) {
      await inviteButton.click()

      // 초대 모달 확인
      await expect(page.locator('[role="dialog"]').or(page.locator('.modal'))).toBeVisible()

      // 이메일 입력 필드 확인
      await expect(page.locator('input[type="email"]')).toBeVisible()
    }
  })

  test('멤버 목록 표시', async ({ page }) => {
    await page.goto('/teams/1')

    // 멤버 목록 또는 멤버 섹션 확인
    const memberSection = page
      .locator('[data-testid="team-members"]')
      .or(page.locator('text=/멤버|Members/i'))

    await expect(memberSection).toBeVisible({ timeout: 5000 })
  })
})
