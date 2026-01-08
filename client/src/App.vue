<template>
  <div id="app">
    <Navbar />
    <main class="main-content">
      <ErrorBoundary>
        <router-view />
      </ErrorBoundary>
    </main>
    <Footer />
    <ToastContainer />
    <ConfirmProvider />
  </div>
</template>

<script setup lang="ts">
import { Navbar, Footer } from '@/components/layout'
import { ToastContainer, ConfirmProvider, ErrorBoundary } from '@/components/common'
</script>

<style>
/* Pretendard 웹폰트 */
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css');

:root {
  /* Colors - Light Mode */
  --primary-color: #667eea;
  --secondary-color: #764ba2;
  --bg-color: #f5f7fa;
  --bg-color-alt: #eef1f5;
  --card-bg: rgba(255, 255, 255, 0.9);
  --card-bg-solid: #ffffff;
  --text-color: #1a1a2e;
  --text-muted: #64748b;
  --border-color: rgba(0, 0, 0, 0.08);

  /* Success/Error/Warning/Info */
  --success-color: #10b981;
  --error-color: #ef4444;
  --warning-color: #f59e0b;
  --info-color: #3b82f6;

  /* Layout */
  --nav-height: 64px;
  --page-padding: 2rem;
  --section-gap: 4rem;

  /* Shadows */
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.12);
  --shadow-glow: 0 0 20px rgba(102, 126, 234, 0.3);

  /* Border Radius */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --radius-xl: 24px;

  /* Transitions */
  --transition-fast: 0.15s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-slow: 0.35s cubic-bezier(0.4, 0, 0.2, 1);

  /* Glassmorphism */
  --glass-blur: blur(12px);
  --glass-bg: rgba(255, 255, 255, 0.7);
  --glass-border: rgba(255, 255, 255, 0.3);
}

/* 다크 모드 - data-theme 속성 기반 */
[data-theme='dark'] {
  --bg-color: #0f0f1a;
  --bg-color-alt: #1a1a2e;
  --card-bg: rgba(30, 30, 50, 0.8);
  --card-bg-solid: #1e1e32;
  --text-color: #f1f5f9;
  --text-muted: #94a3b8;
  --border-color: rgba(255, 255, 255, 0.08);
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.4);
  --shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.5);
  --glass-bg: rgba(30, 30, 50, 0.7);
  --glass-border: rgba(255, 255, 255, 0.1);
}

/* 시스템 테마 (data-theme 미설정 시 fallback) */
@media (prefers-color-scheme: dark) {
  :root:not([data-theme]) {
    --bg-color: #0f0f1a;
    --bg-color-alt: #1a1a2e;
    --card-bg: rgba(30, 30, 50, 0.8);
    --card-bg-solid: #1e1e32;
    --text-color: #f1f5f9;
    --text-muted: #94a3b8;
    --border-color: rgba(255, 255, 255, 0.08);
    --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.3);
    --shadow-md: 0 4px 16px rgba(0, 0, 0, 0.4);
    --shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.5);
    --glass-bg: rgba(30, 30, 50, 0.7);
    --glass-border: rgba(255, 255, 255, 0.1);
  }
}

/* 태블릿 브레이크포인트 */
@media (max-width: 1024px) {
  :root {
    --page-padding: 1.5rem;
    --section-gap: 3rem;
  }
}

/* 모바일 브레이크포인트 */
@media (max-width: 768px) {
  :root {
    --page-padding: 1rem;
    --section-gap: 2rem;
    --nav-height: 56px;
  }
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family:
    'Pretendard',
    'Inter',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    sans-serif;
  background-color: var(--bg-color);
  color: var(--text-color);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  transition:
    background-color var(--transition-slow),
    color var(--transition-slow);
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  min-height: calc(100vh - var(--nav-height));
}

/* 페이지 전환 애니메이션 */
.page-enter-active,
.page-leave-active {
  transition:
    opacity var(--transition-normal),
    transform var(--transition-normal);
}

.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 유틸리티 클래스 */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--page-padding);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

/* 포커스 스타일 (접근성) */
:focus-visible {
  outline: 2px solid var(--primary-color);
  outline-offset: 2px;
}
</style>
