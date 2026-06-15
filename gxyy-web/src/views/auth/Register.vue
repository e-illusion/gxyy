<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../../api/auth'

const router = useRouter()

const form = reactive({
  username: '',
  phone: '',
  email: '',
  address: '',
  password: '',
  confirmPassword: '',
})
const loading = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  if (!form.username || !form.phone || !form.password) {
    errorMsg.value = '请填写必填项'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次密码不一致'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    await authApi.register({
      username: form.username,
      phone: form.phone,
      email: form.email,
      address: form.address,
      password: form.password,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <template #header>
        <h2 style="text-align: center; color: #409eff">注册 GXYY</h2>
      </template>
      <el-form @submit.prevent="handleRegister" label-position="top">
        <el-form-item label="用户名 *">
          <el-input v-model="form.username" placeholder="2-20个字符" size="large" />
        </el-form-item>
        <el-form-item label="手机号 *">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="选填" size="large" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="选填，如紫菘13栋" size="large" />
        </el-form-item>
        <el-form-item label="密码 *">
          <el-input v-model="form.password" type="password" placeholder="6-20个字符" size="large" show-password />
        </el-form-item>
        <el-form-item label="确认密码 *">
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" size="large"
            show-password />
        </el-form-item>
        <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false"
          style="margin-bottom: 16px" />
        <el-button type="primary" size="large" :loading="loading" @click="handleRegister" style="width: 100%">
          注册
        </el-button>
      </el-form>
      <p style="text-align: center; margin-top: 16px; color: #999">
        已有账号？<el-link type="primary" @click="router.push('/login')">去登录</el-link>
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
  width: 420px;
}
</style>
