<template>
  <nav class="navbar">
    <div class="nav-brand">
      <router-link to="/">🗺️ Lumia Ops</router-link>
    </div>
    
    <div class="nav-links">
      <router-link to="/">홈</router-link>
      <router-link to="/team">팀 관리</router-link>
      <router-link to="/strategy">전략 보드</router-link>
      <router-link to="/scrim">스크림</router-link>
    </div>

    <div class="nav-actions">
      <slot name="actions" />
    </div>

    <!-- 모바일 메뉴 버튼 -->
    <button class="nav-mobile-toggle" @click="isMenuOpen = !isMenuOpen" aria-label="메뉴">
      <span></span>
      <span></span>
      <span></span>
    </button>

    <!-- 모바일 메뉴 -->
    <div :class="['nav-mobile-menu', { 'is-open': isMenuOpen }]">
      <router-link to="/" @click="isMenuOpen = false">홈</router-link>
      <router-link to="/team" @click="isMenuOpen = false">팀 관리</router-link>
      <router-link to="/strategy" @click="isMenuOpen = false">전략 보드</router-link>
      <router-link to="/scrim" @click="isMenuOpen = false">스크림</router-link>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const isMenuOpen = ref(false)
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  height: var(--nav-height, 60px);
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-brand a {
  font-size: 1.5rem;
  font-weight: 700;
  text-decoration: none;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-links {
  display: flex;
  gap: 2rem;
}

.nav-links a {
  text-decoration: none;
  color: #666;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  transition: all 0.2s;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
  color: white;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

/* 모바일 토글 버튼 */
.nav-mobile-toggle {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.nav-mobile-toggle span {
  width: 24px;
  height: 2px;
  background: #333;
  transition: all 0.3s;
}

/* 모바일 메뉴 */
.nav-mobile-menu {
  display: none;
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  flex-direction: column;
  padding: 1rem;
}

.nav-mobile-menu.is-open {
  display: flex;
}

.nav-mobile-menu a {
  padding: 0.75rem 1rem;
  text-decoration: none;
  color: #333;
  border-radius: 6px;
}

.nav-mobile-menu a:hover,
.nav-mobile-menu a.router-link-active {
  background: var(--primary-color);
  color: white;
}

/* 반응형 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .nav-actions {
    display: none;
  }

  .nav-mobile-toggle {
    display: flex;
  }
}
</style>
