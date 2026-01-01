<template>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-brand">
        <span class="footer-logo">🗺️ Lumia Ops</span>
        <p class="footer-description">
          이터널 리턴 팀 전략 & 스크림 관리 플랫폼
        </p>
      </div>

      <div class="footer-links">
        <div class="footer-section">
          <h4>서비스</h4>
          <router-link to="/team">팀 관리</router-link>
          <router-link to="/strategy">전략 보드</router-link>
          <router-link to="/scrim">스크림</router-link>
        </div>

        <div class="footer-section">
          <h4>링크</h4>
          <a href="https://github.com/kimmyuung/lumia-ops" target="_blank">GitHub</a>
        </div>
      </div>
    </div>

    <div class="footer-bottom">
      <p>&copy; {{ currentYear }} Lumia Ops. All rights reserved.</p>
    </div>
  </footer>
</template>

<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<style scoped>
.footer {
  background: #1a1a2e;
  color: white;
  padding: 3rem 2rem 1rem;
  margin-top: auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  gap: 3rem;
}

.footer-brand {
  flex: 1;
}

.footer-logo {
  font-size: 1.5rem;
  font-weight: 700;
}

.footer-description {
  margin-top: 0.5rem;
  color: #888;
  font-size: 0.875rem;
}

.footer-links {
  display: flex;
  gap: 4rem;
}

.footer-section h4 {
  margin-bottom: 1rem;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #888;
}

.footer-section a {
  display: block;
  color: #ccc;
  text-decoration: none;
  padding: 0.25rem 0;
  transition: color 0.2s;
}

.footer-section a:hover {
  color: white;
}

.footer-bottom {
  max-width: 1200px;
  margin: 2rem auto 0;
  padding-top: 1rem;
  border-top: 1px solid #333;
  text-align: center;
}

.footer-bottom p {
  color: #666;
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
  }

  .footer-links {
    flex-direction: column;
    gap: 2rem;
  }
}
</style>
