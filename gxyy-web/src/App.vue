<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'

const router = useRouter()
const userStore = useUserStore()
const activeMenu = ref('/')

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
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    goProfile()
  } else if (command === 'my-items') {
    goMyItems()
  }
}
</script>

<template>
  <div class="app-container">
    <!-- 顶部导航 -->
    <el-menu
      :default-active="activeMenu"
      mode="horizontal"
      :ellipsis="false"
      class="main-header"
    >
      <el-menu-item index="/" @click="goHome" class="logo-item">
        <img src="/logo.png" alt="GXYY" class="logo-img" />
        <span class="logo-text">GXYY</span>
      </el-menu-item>
      <div class="flex-grow" />

      <!-- Android 下载 -->
      <el-menu-item index="/app-debug.apk">
        <a href="/app-debug.apk" download class="android-dl">
          <el-icon><Download /></el-icon>
          <span>Android 客户端</span>
        </a>
      </el-menu-item>

      <!-- 未登录 -->
      <template v-if="!isLoggedIn()">
        <el-menu-item index="/login" @click="goLogin">登录</el-menu-item>
        <el-menu-item index="/register" @click="goRegister">注册</el-menu-item>
      </template>

      <!-- 已登录 -->
      <template v-else>
        <el-menu-item index="/exchange" @click="goExchange">
          <el-icon><Switch /></el-icon>
          交换请求
        </el-menu-item>
        <el-menu-item index="/notifications" @click="goNotifications">
          <el-icon><Bell /></el-icon>
          通知
        </el-menu-item>
        <el-sub-menu index="user">
          <template #title>
            <el-avatar :size="28" :src="userStore.user?.avatar" />
            <span style="margin-left: 8px">{{ userStore.user?.username }}</span>
          </template>
          <el-menu-item index="profile" @click="goProfile">个人中心</el-menu-item>
          <el-menu-item index="my-items" @click="goMyItems">我的物品</el-menu-item>
          <el-menu-item index="logout" @click="handleLogout">退出登录</el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>

    <!-- 内容区域 -->
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background-color: #f5f7fa;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.app-container {
  min-height: 100vh;
}

.main-header {
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #e4e7ed;
}

.logo-img {
  width: 28px;
  height: 28px;
  border-radius: 4px;
}
.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  margin-left: 8px;
}

.flex-grow {
  flex-grow: 1;
}

.android-dl {
  display: flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  color: inherit;
}
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
</style>
