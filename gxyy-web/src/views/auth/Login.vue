<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../../api/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: '',
})
const loading = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  if (!form.username || !form.password) {
    errorMsg.value = '请填写用户名和密码'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await authApi.login(form)
    userStore.setAuth(res.data.token, res.data.user)
    router.push('/')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <template #header>
        <h2 style="text-align: center; color: #409eff">登录 GXYY</h2>
      </template>
      <el-form @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large"
            show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false"
          style="margin-bottom: 16px" />
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" style="width: 100%">
          登录
        </el-button>
      </el-form>
      <p style="text-align: center; margin-top: 16px; color: #999">
        还没有账号？<el-link type="primary" @click="router.push('/register')">立即注册</el-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 160px);
}
.auth-card {
  width: 400px;
}
</style>
