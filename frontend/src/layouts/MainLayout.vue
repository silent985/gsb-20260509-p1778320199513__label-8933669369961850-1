<template>
  <div class="main-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <span class="logo-icon">💰</span>
          <span class="logo-text" v-if="!sidebarCollapsed">记账服务</span>
        </div>
        <button class="sidebar-toggle" @click="toggleSidebar">
          {{ sidebarCollapsed ? '→' : '←' }}
        </button>
      </div>
      
      <nav class="sidebar-nav">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: $route.path === item.path }"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-text" v-if="!sidebarCollapsed">{{ item.name }}</span>
        </router-link>
      </nav>
      
      <div class="sidebar-footer">
        <div class="user-info" v-if="!sidebarCollapsed">
          <div class="user-avatar">{{ user?.nickname?.[0] || user?.username?.[0] || 'U' }}</div>
          <div class="user-details">
            <div class="user-name">{{ user?.nickname || user?.username }}</div>
            <div class="user-email">{{ user?.email }}</div>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout" :title="sidebarCollapsed ? '退出登录' : ''">
          <span>🚪</span>
          <span v-if="!sidebarCollapsed">退出登录</span>
        </button>
      </div>
    </aside>
    
    <!-- Main Content -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const sidebarCollapsed = ref(false)
const user = computed(() => authStore.user)

const menuItems = [
  { path: '/', name: '仪表盘', icon: '📊' },
  { path: '/transactions', name: '交易记录', icon: '💳' },
  { path: '/accounts', name: '账户管理', icon: '🏦' },
  { path: '/categories', name: '分类管理', icon: '📁' },
  { path: '/budgets', name: '预算管理', icon: '🎯' },
  { path: '/statistics', name: '统计报表', icon: '📈' }
]

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 260px;
  background: var(--color-bg-sidebar);
  color: white;
  display: flex;
  flex-direction: column;
  transition: width var(--transition-normal);
  position: fixed;
  height: 100vh;
  z-index: 100;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.1);
}

.sidebar-collapsed {
  width: 80px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  height: 80px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.sidebar-collapsed .sidebar-header {
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem 0.5rem;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  overflow: hidden;
}

.logo-icon {
  font-size: 2rem;
  min-width: 2rem;
  text-align: center;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.logo-text {
  font-size: 1.25rem;
  font-weight: 700;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.sidebar-toggle {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: var(--radius-sm);
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.sidebar-collapsed .sidebar-toggle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  margin-top: 0.25rem;
}

.sidebar-nav {
  flex: 1;
  padding: 1.5rem 1rem;
  overflow-y: auto;
  overflow-x: hidden;
}

.sidebar-collapsed .sidebar-nav {
  padding: 1.5rem 0.75rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  margin-bottom: 0.5rem;
  border-radius: var(--radius-lg);
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.sidebar-collapsed .nav-item {
  justify-content: center;
  padding: 1rem 0;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  transform: translateX(4px);
}

.sidebar-collapsed .nav-item:hover {
  transform: none;
  background: rgba(255, 255, 255, 0.15);
}

.nav-item.active {
  background: var(--gradient-primary);
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

.nav-icon {
  font-size: 1.5rem;
  min-width: 1.5rem;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-text {
  font-size: 0.95rem;
  font-weight: 500;
}

.sidebar-footer {
  padding: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.user-avatar {
  width: 40px;
  height: 40px;
  min-width: 40px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1.125rem;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 600;
  font-size: 0.9375rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: white;
}

.user-email {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.875rem;
  background: rgba(239, 68, 68, 0.1);
  border: none;
  border-radius: var(--radius-lg);
  color: #fca5a5;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-collapsed .logout-btn {
  padding: 0.875rem 0;
  background: transparent;
  width: 40px;
  height: 40px;
  margin: 0 auto;
  border-radius: 50%;
}

.logout-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  color: #fecaca;
  transform: translateY(-1px);
}

.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 2rem;
  background: var(--color-bg);
  min-height: 100vh;
  transition: margin-left var(--transition-normal);
  width: calc(100% - 260px);
}

.sidebar-collapsed ~ .main-content {
  margin-left: 80px;
  width: calc(100% - 80px);
}

@media (max-width: 768px) {
  .sidebar {
    width: 80px;
  }
  
  .logo-text,
  .nav-text,
  .user-info {
    display: none !important;
  }
  
  .main-content {
    margin-left: 80px;
    width: calc(100% - 80px);
    padding: 1rem;
  }
  
  .sidebar-toggle {
    display: none;
  }
  
  .nav-item {
    justify-content: center;
    padding: 1rem 0;
  }
  
  .logout-btn {
    padding: 0.875rem 0;
    justify-content: center;
  }
}
</style>
