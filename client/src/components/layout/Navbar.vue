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
      <template v-if="isLoggedIn">
        <div class="user-info">
          <User :size="18" />
          <span>{{ user?.nickname }}</span>
        </div>
        <button class="logout-btn" @click="handleLogout" aria-label="로그아웃">
          <LogOut :size="18" />
        </button>
      </template>
      <template v-else>
        <router-link to="/login" class="login-btn">
          <LogIn :size="18" />
          <span>로그인</span>
        </router-link>
      </template>
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
        <div class="mobile-auth">
          <template v-if="isLoggedIn">
            <div class="user-info-mobile">
              <User :size="20" />
              <span>{{ user?.nickname }}</span>
            </div>
            <button class="mobile-logout" @click="handleLogout">
              <LogOut :size="20" />
              <span>로그아웃</span>
            </button>
          </template>
          <router-link v-else to="/login" class="mobile-login" @click="isMenuOpen = false">
            <LogIn :size="20" />
            <span>로그인</span>
          </router-link>
        </div>
      </div>
    </Transition>
  </nav>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Map, Home, Users, Target, Swords, Menu, X, LogIn, LogOut, User } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { useAuth } from '@/composables/useAuth'

const userStore = useUserStore()
const { logout } = useAuth()

const isMenuOpen = ref(false)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const user = computed(() => userStore.user)

function handleLogout() {
  isMenuOpen.value = false
  logout()
}
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 var(--page-padding);
  height: var(--nav-height);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: all var(--transition-normal);
}

.nav-brand a {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.5rem;
  font-weight: 700;
  text-decoration: none;
  color: var(--primary-color);
  transition: transform var(--transition-fast);
}

.nav-brand a:hover {
  transform: scale(1.02);
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
  transform: translateY(-1px);
}

.nav-links a.router-link-active {
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--secondary-color) 100%
  );
  color: white;
  box-shadow: var(--shadow-sm);
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
  transition: all var(--transition-fast);
}

.nav-mobile-toggle:hover {
  background: var(--bg-color-alt);
}

.nav-mobile-toggle:active {
  transform: scale(0.95);
}

/* 모바일 메뉴 */
.nav-mobile-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border-bottom: 1px solid var(--border-color);
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  padding: 0.5rem;
}

.nav-mobile-menu a {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
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
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
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

/* 로그인/사용자 정보 */
.login-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  text-decoration: none;
  border-radius: var(--radius-md);
  font-weight: 600;
  transition: all var(--transition-normal);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: var(--bg-color-alt);
  border-radius: var(--radius-md);
  color: var(--text-color);
  font-weight: 500;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.5rem;
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.logout-btn:hover {
  color: var(--error-color);
  background: rgba(239, 68, 68, 0.1);
}

/* 모바일 인증 */
.mobile-auth {
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid var(--border-color);
}

.user-info-mobile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  color: var(--text-color);
}

.mobile-login,
.mobile-logout {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1rem;
  text-decoration: none;
  color: var(--text-color);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  width: 100%;
  background: none;
  border: none;
  cursor: pointer;
  font-size: inherit;
}

.mobile-login:hover {
  background: var(--primary-color);
  color: white;
}

.mobile-logout:hover {
  background: rgba(239, 68, 68, 0.1);
  color: var(--error-color);
}
</style>
