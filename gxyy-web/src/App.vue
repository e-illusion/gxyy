<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from './stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const isLoggedIn = () => !!userStore.token

const goHome = () => router.push('/')
const goLogin = () => router.push('/login')
const goRegister = () => router.push('/register')
const goProfile = () => router.push('/profile')
const goMyItems = () => router.push('/my-items')
const goExchange = () => router.push('/exchange-requests')
const goNotifications = () => router.push('/notifications')

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const handleCommand = (command) => {
  if (command === 'logout') handleLogout()
  else if (command === 'profile') goProfile()
  else if (command === 'my-items') goMyItems()
  else if (command === 'favorites') router.push('/favorites')
  else if (command === 'exchange') goExchange()
  else if (command === 'notifications') goNotifications()
}

const navItems = computed(() => {
  if (!isLoggedIn()) return []
  return [
    { key: 'favorites', label: '收藏', action: () => router.push('/favorites'), icon: 'Star' },
    { key: 'exchange', label: '交换', action: goExchange, icon: 'Switch' },
    { key: 'notifications', label: '通知', action: goNotifications, icon: 'Bell' },
  ]
})
</script>

<template>
  <div class="app-container">
    <!-- Airbnb-style Top Navigation -->
    <nav class="airbnb-nav">
      <!-- Left: Logo -->
      <div class="nav-left" @click="goHome">
        <img src="/logo.png" alt="GXYY" class="nav-logo-img" />
        <span class="nav-logo-text">GXYY</span>
      </div>

      <!-- Center: Nav items -->
      <div class="nav-center">
        <button class="nav-link" :class="{ active: activeMenu === '/' }" @click="goHome">
          <el-icon :size="16"><Goods /></el-icon>
          <span>物品</span>
        </button>
        <button
          v-for="item in navItems"
          :key="item.key"
          class="nav-link"
          :class="{ active: activeMenu.startsWith('/' + item.key.split('-')[0]) }"
          @click="item.action"
        >
          <el-icon :size="16"><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </button>
      </div>

      <!-- Right: Actions -->
      <div class="nav-right">
        <!-- Android download -->
        <a href="/app-debug.apk" download class="nav-android-btn">
          <el-icon :size="16"><Download /></el-icon>
          <span>Android</span>
        </a>

        <!-- Not logged in -->
        <template v-if="!isLoggedIn()">
          <button class="nav-text-btn" @click="goLogin">登录</button>
          <button class="nav-primary-btn" @click="goRegister">注册</button>
        </template>

        <!-- Logged in -->
        <template v-else>
          <el-dropdown trigger="click" @command="handleCommand" popper-class="airbnb-dropdown">
            <div class="nav-user-area">
              <el-avatar :size="32" :src="userStore.user?.avatar" />
              <span class="nav-username">{{ userStore.user?.username }}</span>
              <el-icon :size="12" class="nav-chevron"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="my-items">我的物品</el-dropdown-item>
                <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
                <el-dropdown-item command="exchange">交换请求</el-dropdown-item>
                <el-dropdown-item command="notifications">通知</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </nav>

    <!-- Content -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<style>
/* ══════════════════════════════════════
   Global Reset
   ══════════════════════════════════════ */
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background-color: var(--bg-page);
  color: var(--text-primary);
  font-family: var(--font-family);
  -webkit-font-smoothing: antialiased;
}

.app-container {
  min-height: 100vh;
}

/* ══════════════════════════════════════
   Airbnb Navigation (80px, hairline bottom)
   ══════════════════════════════════════ */
.airbnb-nav {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  height: 68px;
  padding: 0 var(--spacing-xl);
  background: var(--color-canvas);
  border-bottom: 1px solid var(--color-hairline);
}

/* ── Left: Logo ── */
.nav-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  cursor: pointer;
  flex-shrink: 0;
  user-select: none;
}
.nav-logo-img {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
}
.nav-logo-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: -0.5px;
}

/* ── Center: Nav links ── */
.nav-center {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  flex: 1;
}
.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  padding: var(--spacing-sm) var(--spacing-base);
  font-size: 16px;
  font-weight: 500;
  color: var(--color-muted);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: color var(--transition-fast), background var(--transition-fast);
  font-family: inherit;
}
.nav-link:hover {
  color: var(--color-ink);
  background: var(--color-surface-soft);
}
.nav-link.active {
  color: var(--color-ink);
}

/* ── Right: Actions ── */
.nav-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-shrink: 0;
}

/* Android download link */
.nav-android-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
  text-decoration: none;
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
}
.nav-android-btn:hover {
  background: var(--color-surface-soft);
}

/* Text button (登录) */
.nav-text-btn {
  background: none;
  border: none;
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
  font-family: inherit;
}
.nav-text-btn:hover {
  background: var(--color-surface-soft);
}

/* Primary button (注册) */
.nav-primary-btn {
  background: var(--color-primary);
  color: var(--color-on-primary);
  border: none;
  padding: var(--spacing-sm) var(--spacing-base);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-radius: var(--radius-button);
  transition: background var(--transition-fast);
  font-family: inherit;
}
.nav-primary-btn:hover {
  background: var(--color-primary-dark);
}
.nav-primary-btn:active {
  background: var(--color-primary-active);
}

/* User area (avatar + name) */
.nav-user-area {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xs) var(--spacing-xs) var(--spacing-xs) var(--spacing-md);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: box-shadow var(--transition-fast);
}
.nav-user-area:hover {
  box-shadow: var(--shadow-card);
}
.nav-username {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
}
.nav-chevron {
  color: var(--color-muted);
}

/* ══════════════════════════════════════
   Content
   ══════════════════════════════════════ */
.main-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: var(--spacing-xl) var(--spacing-lg);
}

/* ══════════════════════════════════════
   Dropdown popper (non-scoped, needs global)
   ══════════════════════════════════════ */
.airbnb-dropdown {
  margin-top: 8px !important;
}
.airbnb-dropdown .el-dropdown-menu__item {
  font-size: 14px;
  font-weight: 500;
  padding: 10px 16px;
}
.airbnb-dropdown .el-dropdown-menu__item:hover {
  background: var(--color-surface-soft);
  color: var(--color-ink);
}

/* ══════════════════════════════════════
   Responsive
   ══════════════════════════════════════ */
@media (max-width: 744px) {
  .airbnb-nav {
    height: 56px;
    padding: 0 var(--spacing-base);
  }
  .nav-center {
    display: none;
  }
  .nav-logo-text {
    font-size: 18px;
  }
  .nav-username {
    display: none;
  }
  .nav-android-btn span {
    display: none;
  }
}
</style>
