<template>
  <nav class="navbar">
    <div class="nav-brand">
      <router-link to="/">
        <Map :size="24" class="brand-icon" />
        <span>Lumia Ops</span>
      </router-link>
    </div>

    <div class="nav-links">
      <router-link to="/">
        <Home :size="18" />
        <span>홈</span>
      </router-link>
      <router-link to="/team">
        <Users :size="18" />
        <span>팀 관리</span>
      </router-link>
      <router-link to="/strategy">
        <Target :size="18" />
        <span>전략 보드</span>
      </router-link>
      <router-link to="/scrim">
        <Swords :size="18" />
        <span>스크림</span>
      </router-link>
    </div>

    <div class="nav-actions">
      <slot name="actions" />
    </div>

    <!-- 모바일 메뉴 버튼 -->
    <button class="nav-mobile-toggle" @click="isMenuOpen = !isMenuOpen" aria-label="메뉴">
      <Menu v-if="!isMenuOpen" :size="24" />
      <X v-else :size="24" />
    </button>

    <!-- 모바일 메뉴 -->
    <Transition name="mobile-menu">
      <div v-if="isMenuOpen" class="nav-mobile-menu">
        <router-link to="/" @click="isMenuOpen = false">
          <Home :size="20" />
          <span>홈</span>
        </router-link>
        <router-link to="/team" @click="isMenuOpen = false">
          <Users :size="20" />
          <span>팀 관리</span>
        </router-link>
        <router-link to="/strategy" @click="isMenuOpen = false">
          <Target :size="20" />
          <span>전략 보드</span>
        </router-link>
        <router-link to="/scrim" @click="isMenuOpen = false">
          <Swords :size="20" />
          <span>스크림</span>
        </router-link>
      </div>
    </Transition>
  </nav>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Map, Home, Users, Target, Swords, Menu, X } from 'lucide-vue-next'

const isMenuOpen = ref(false)
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--page-padding, 2rem);
  height: var(--nav-height, 60px);
  background: white;
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-brand a {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  text-decoration: none;
  color: var(--primary-color);
}

.nav-brand .brand-icon {
  color: var(--secondary-color);
}

.nav-links {
  display: flex;
  gap: 0.5rem;
}

.nav-links a {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: var(--text-muted);
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
}

.nav-links a:hover {
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary-color);
}

.nav-links a.router-link-active {
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--secondary-color) 100%
  );
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
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  color: var(--text-color);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.nav-mobile-toggle:hover {
  background: rgba(0, 0, 0, 0.05);
}

/* 모바일 메뉴 */
.nav-mobile-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  padding: 0.5rem;
}

.nav-mobile-menu a {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  text-decoration: none;
  color: var(--text-color);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.nav-mobile-menu a:hover,
.nav-mobile-menu a.router-link-active {
  background: var(--primary-color);
  color: white;
}

/* 모바일 메뉴 트랜지션 */
.mobile-menu-enter-active,
.mobile-menu-leave-active {
  transition: all 0.2s ease;
}

.mobile-menu-enter-from,
.mobile-menu-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 반응형 */
@media (max-width: 768px) {
  .nav-links,
  .nav-actions {
    display: none;
  }

  .nav-mobile-toggle {
    display: flex;
  }
}
</style>
