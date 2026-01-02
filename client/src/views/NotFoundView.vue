<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="error-code">
        <span class="digit">4</span>
        <div class="icon-wrapper">
          <SearchX :size="80" />
        </div>
        <span class="digit">4</span>
      </div>
      
      <h1>페이지를 찾을 수 없습니다</h1>
      <p>요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다.</p>
      
      <div class="actions">
        <router-link to="/" class="btn btn-primary">
          <Home :size="20" />
          <span>홈으로 돌아가기</span>
        </router-link>
        <button class="btn btn-secondary" @click="goBack">
          <ArrowLeft :size="20" />
          <span>이전 페이지</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Home, ArrowLeft, SearchX } from 'lucide-vue-next'

const router = useRouter()

function goBack() {
  if (window.history.length > 2) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.not-found {
  min-height: calc(100vh - var(--nav-height));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--page-padding);
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.05) 0%,
    rgba(118, 75, 162, 0.05) 100%
  );
}

.not-found-content {
  text-align: center;
  max-width: 500px;
}

.error-code {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 2rem;
}

.digit {
  font-size: clamp(4rem, 15vw, 8rem);
  font-weight: 800;
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--secondary-color) 100%
  );
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.15) 0%,
    rgba(118, 75, 162, 0.15) 100%
  );
  border-radius: 50%;
  color: var(--primary-color);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.not-found h1 {
  font-size: clamp(1.5rem, 4vw, 2rem);
  margin-bottom: 0.75rem;
  color: var(--text-color);
}

.not-found p {
  color: var(--text-muted);
  margin-bottom: 2.5rem;
  font-size: 1rem;
}

.actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1.5rem;
  border-radius: var(--radius-lg);
  font-weight: 600;
  transition: all var(--transition-normal);
}

.btn:hover {
  transform: translateY(-2px);
}

.btn-primary {
  background: linear-gradient(
    135deg,
    var(--primary-color) 0%,
    var(--secondary-color) 100%
  );
  color: white;
  box-shadow: var(--shadow-md), 0 0 20px rgba(102, 126, 234, 0.3);
}

.btn-primary:hover {
  box-shadow: var(--shadow-lg), 0 0 30px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: var(--card-bg);
  backdrop-filter: var(--glass-blur);
  color: var(--text-color);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.btn-secondary:hover {
  background: var(--card-bg-solid);
  box-shadow: var(--shadow-md);
}

@media (max-width: 480px) {
  .actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }

  .icon-wrapper {
    width: 70px;
    height: 70px;
  }

  .icon-wrapper svg {
    width: 50px;
    height: 50px;
  }
}
</style>
