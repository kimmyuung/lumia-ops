import { defineConfig, devices } from '@playwright/test'

/**
 * Playwright E2E 테스트 설정
 * @see https://playwright.dev/docs/test-configuration
 */
export default defineConfig({
  testDir: './tests/e2e',

  /* 병렬 실행 */
  fullyParallel: true,

  /* CI 환경에서만 test.only 금지 */
  forbidOnly: !!process.env.CI,

  /* CI에서는 재시도 2번, 로컬에서는 재시도 없음 */
  retries: process.env.CI ? 2 : 0,

  /* CI에서는 단일 worker, 로컬에서는 병렬 */
  workers: process.env.CI ? 1 : undefined,

  /* 리포터 설정 */
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['json', { outputFile: 'test-results/results.json' }],
    ['list'] // 콘솔 출력
  ],

  /* 공통 설정 */
  use: {
    /* 기본 URL */
    baseURL: process.env.BASE_URL || 'http://localhost:3000',

    /* 실패 시 trace 수집 */
    trace: 'on-first-retry',

    /* 실패 시 스크린샷 */
    screenshot: 'only-on-failure',

    /* 실패 시 비디오 저장 */
    video: 'retain-on-failure',

    /* 타임아웃 */
    actionTimeout: 10000,
    navigationTimeout: 30000
  },

  /* 테스트할 브라우저 */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    },

    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] }
    }

    // Safari는 선택사항
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] }
    // },

    /* 모바일 테스트 (선택사항) */
    // {
    //   name: 'Mobile Chrome',
    //   use: { ...devices['Pixel 5'] }
    // },
  ],

  /* 로컬 개발 서버 실행 */
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000 // 2분
  }
})
