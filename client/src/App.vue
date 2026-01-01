<template>
  <div id="app">
    <Navbar />
    <main class="main-content">
      <router-view />
    </main>
    <Footer />
    <ToastContainer />
  </div>
</template>

<script setup lang="ts">
import { Navbar, Footer } from '@/components/layout'
import { ToastContainer } from '@/components/common'
</script>

<style>
:root {
  /* Colors */
  --primary-color: #667eea;
  --secondary-color: #764ba2;
  --bg-color: #f5f7fa;
  --text-color: #333;
  --text-muted: #666;

  /* Success/Error/Warning/Info */
  --success-color: #10b981;
  --error-color: #ef4444;
  --warning-color: #f59e0b;
  --info-color: #3b82f6;

  /* Layout */
  --nav-height: 60px;
  --page-padding: 2rem;
  --section-gap: 4rem;

  /* Shadows */
  --shadow-sm: 0 2px 4px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 8px 24px rgba(0, 0, 0, 0.15);

  /* Border Radius */
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;

  /* Transitions */
  --transition-fast: 0.15s ease;
  --transition-normal: 0.2s ease;
  --transition-slow: 0.3s ease;
}

/* 반응형 CSS 변수 */
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
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
    sans-serif;
  background-color: var(--bg-color);
  color: var(--text-color);
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
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
</style>
