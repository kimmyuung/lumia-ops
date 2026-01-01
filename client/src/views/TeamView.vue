<template>
  <div class="team-view">
    <div class="container">
      <PageHeader
        title="팀 관리"
        description="팀원 초대, 역할 배정, 일정 관리"
        :icon="Users"
      >
        <template #actions>
          <Button v-if="!hasTeam" variant="primary" @click="showCreateModal = true">
            <Plus :size="20" />
            <span>팀 생성</span>
          </Button>
          <Button v-else-if="isOwner" variant="secondary" @click="editTeam">
            <Edit :size="20" />
            <span>팀 수정</span>
          </Button>
        </template>
      </PageHeader>

      <!-- 로딩 상태 -->
      <template v-if="isLoading">
        <div class="skeleton-grid">
          <SkeletonCard v-for="i in 3" :key="i" />
        </div>
      </template>

      <!-- 팀이 있는 경우 -->
      <template v-else-if="currentTeam">
        <section class="team-info">
          <Card>
            <div class="team-header">
              <div class="team-avatar">
                <Users :size="32" />
              </div>
              <div class="team-details">
                <h2>{{ currentTeam.name }}</h2>
                <p v-if="currentTeam.description">{{ currentTeam.description }}</p>
              </div>
              <div class="team-actions" v-if="isOwner">
                <Button variant="ghost" size="sm" @click="editTeam">
                  <Edit :size="18" />
                </Button>
                <Button variant="ghost" size="sm" @click="confirmDelete">
                  <Trash2 :size="18" />
                </Button>
              </div>
            </div>
          </Card>
        </section>

        <section class="members-section">
          <div class="section-header">
            <h3>
              <Users :size="20" />
              <span>팀원 ({{ memberCount }}명)</span>
            </h3>
            <Button v-if="isAdmin" variant="secondary" size="sm" @click="showInviteModal = true">
              <UserPlus :size="18" />
              <span>초대</span>
            </Button>
          </div>
          
          <div class="members-grid">
            <Card v-for="member in currentTeam.members" :key="member.id" class="member-card">
              <div class="member-info">
                <div class="member-avatar">
                  <User :size="24" />
                </div>
                <div class="member-details">
                  <span class="member-name">{{ member.nickname }}</span>
                  <span class="member-role" :class="member.role.toLowerCase()">
                    {{ getRoleLabel(member.role) }}
                  </span>
                </div>
              </div>
            </Card>
          </div>
        </section>

        <!-- 대기 중인 초대 (관리자만) -->
        <section v-if="isAdmin && currentTeam" class="invitations-section">
          <PendingInvitations ref="pendingInvitationsRef" :team-id="currentTeam.id" />
        </section>

        <div class="danger-zone" v-if="!isOwner">
          <Button variant="danger" @click="confirmLeave">
            <LogOut :size="18" />
            <span>팀 탈퇴</span>
          </Button>
        </div>
      </template>

      <!-- 팀이 없는 경우 -->
      <template v-else>
        <section class="empty-state">
          <div class="empty-icon">
            <Users :size="64" />
          </div>
          <h2>아직 팀이 없습니다</h2>
          <p>새로운 팀을 생성하거나 초대 링크로 팀에 참여하세요.</p>
          <div class="empty-actions">
            <Button variant="primary" @click="showCreateModal = true">
              <Plus :size="20" />
              <span>팀 생성하기</span>
            </Button>
            <Button variant="secondary" @click="showJoinModal = true">
              <UserPlus :size="20" />
              <span>초대 코드 입력</span>
            </Button>
          </div>
        </section>
      </template>
    </div>

    <!-- 팀 생성/수정 모달 -->
    <TeamFormModal
      v-if="showCreateModal || showEditModal"
      :team="showEditModal ? currentTeam : null"
      @close="closeModals"
      @saved="handleTeamSaved"
    />

    <!-- 초대 모달 -->
    <TeamInviteModal
      v-if="showInviteModal && currentTeam"
      :team-id="currentTeam.id"
      @close="showInviteModal = false"
      @invited="handleInvited"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Users, Plus, UserPlus, Edit, Trash2, User, LogOut } from 'lucide-vue-next'
import { Button, Card, PageHeader, SkeletonCard } from '@/components/common'
import TeamFormModal from '@/components/team/TeamFormModal.vue'
import TeamInviteModal from '@/components/team/TeamInviteModal.vue'
import PendingInvitations from '@/components/team/PendingInvitations.vue'
import { useTeamStore } from '@/stores/team'
import { storeToRefs } from 'pinia'
import type { Team, TeamRole } from '@/types/team'

const teamStore = useTeamStore()

const pendingInvitationsRef = ref<InstanceType<typeof PendingInvitations> | null>(null)

const { currentTeam, isLoading, hasTeam, isOwner, isAdmin, memberCount } = storeToRefs(teamStore)

const showCreateModal = ref(false)
const showEditModal = ref(false)
const showInviteModal = ref(false)
const showJoinModal = ref(false)

onMounted(() => {
  teamStore.fetchMyTeam()
})

function getRoleLabel(role: TeamRole): string {
  switch (role) {
    case 'OWNER': return '팀장'
    case 'ADMIN': return '관리자'
    case 'MEMBER': return '멤버'
    default: return role
  }
}

function editTeam() {
  showEditModal.value = true
}

function closeModals() {
  showCreateModal.value = false
  showEditModal.value = false
  showInviteModal.value = false
  showJoinModal.value = false
}

function handleTeamSaved(_team: Team) {
  closeModals()
}

function handleInvited() {
  showInviteModal.value = false
  pendingInvitationsRef.value?.refresh()
}

async function confirmDelete() {
  if (!currentTeam.value) return
  if (confirm('정말 팀을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    await teamStore.deleteTeam(currentTeam.value.id)
  }
}

async function confirmLeave() {
  if (confirm('정말 팀에서 탈퇴하시겠습니까?')) {
    await teamStore.leaveTeam()
  }
}
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

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.team-info {
  margin-bottom: 2rem;
}

.team-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.team-avatar {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  border-radius: var(--radius-lg);
  color: white;
}

.team-details {
  flex: 1;
}

.team-details h2 {
  margin-bottom: 0.25rem;
  color: var(--text-color);
}

.team-details p {
  color: var(--text-muted);
}

.team-actions {
  display: flex;
  gap: 0.5rem;
}

.members-section {
  margin-bottom: 2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-header h3 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-color);
}

.members-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
}

.member-card {
  padding: 1rem;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.member-avatar {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-color-alt);
  border-radius: 50%;
  color: var(--text-muted);
}

.member-details {
  display: flex;
  flex-direction: column;
}

.member-name {
  font-weight: 500;
  color: var(--text-color);
}

.member-role {
  font-size: 0.75rem;
  padding: 0.125rem 0.5rem;
  border-radius: var(--radius-sm);
  display: inline-block;
  width: fit-content;
}

.member-role.owner {
  background: rgba(102, 126, 234, 0.1);
  color: var(--primary-color);
}

.member-role.admin {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
}

.member-role.member {
  background: var(--bg-color-alt);
  color: var(--text-muted);
}

.danger-zone {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: var(--card-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
}

.empty-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  border-radius: 50%;
  color: var(--primary-color);
}

.empty-state h2 {
  margin-bottom: 0.5rem;
  color: var(--text-color);
}

.empty-state p {
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.empty-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .team-header {
    flex-wrap: wrap;
  }
  
  .empty-actions {
    flex-direction: column;
  }
}
</style>
