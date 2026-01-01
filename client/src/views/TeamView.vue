<template>
  <div class="team-view">
    <div class="container">
      <header class="page-header">
        <div class="header-content">
          <h1>
            <Users :size="32" class="header-icon" />
            <span>팀 관리</span>
          </h1>
          <p class="page-description">팀원 초대, 역할 배정, 일정 관리</p>
        </div>
        <Button variant="primary">
          <Plus :size="20" />
          <span>팀 생성</span>
        </Button>
      </header>

      <section class="empty-state">
        <div class="empty-icon">
          <Users :size="64" />
        </div>
        <h2>아직 팀이 없습니다</h2>
        <p>새로운 팀을 생성하거나 초대 링크로 팀에 참여하세요.</p>
        <div class="empty-actions">
          <Button variant="primary">
            <Plus :size="20" />
            <span>팀 생성하기</span>
          </Button>
          <Button variant="secondary">
            <UserPlus :size="20" />
            <span>초대 코드 입력</span>
          </Button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Users, Plus, UserPlus } from 'lucide-vue-next'
import { Button } from '@/components/common'
</script>

<style scoped>
.team-view {
  min-height: 100%;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--page-padding);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.header-content h1 {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.header-icon {
  color: var(--primary-color);
}

.page-description {
  color: var(--text-muted);
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    135deg,
    rgba(102, 126, 234, 0.1) 0%,
    rgba(118, 75, 162, 0.1) 100%
  );
  border-radius: 50%;
  color: var(--primary-color);
}

.empty-state h2 {
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: 1.5rem;
}

.empty-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* 반응형 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
  }

  .empty-actions {
    flex-direction: column;
  }
}
</style>
