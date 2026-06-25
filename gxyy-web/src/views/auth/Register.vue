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
    await router.replace('/login')
  } catch (e) {
    errorMsg.value = e?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">注册 GXYY</h2>

      <div class="auth-form">
        <div class="field">
          <label class="field-label">用户名 <span class="required">*</span></label>
          <el-input v-model="form.username" placeholder="2-20个字符" size="large" />
        </div>

        <div class="field">
          <label class="field-label">手机号 <span class="required">*</span></label>
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
        </div>

        <div class="field">
          <label class="field-label">邮箱</label>
          <el-input v-model="form.email" placeholder="选填" size="large" />
        </div>

        <div class="field">
          <label class="field-label">地址</label>
          <el-input v-model="form.address" placeholder="选填，如紫菘13栋" size="large" />
        </div>

        <div class="field">
          <label class="field-label">密码 <span class="required">*</span></label>
          <el-input v-model="form.password" type="password" placeholder="6-20个字符" size="large" show-password />
        </div>

        <div class="field">
          <label class="field-label">确认密码 <span class="required">*</span></label>
          <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" size="large" show-password />
        </div>

        <div v-if="errorMsg" class="auth-error">{{ errorMsg }}</div>

        <button class="auth-submit" :disabled="loading" @click="handleRegister">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </div>

      <p class="auth-switch">
        已有账号？<a @click="router.push('/login')">去登录</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 160px);
  padding: var(--spacing-lg);
}
.auth-card {
  width: 420px;
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
}
.auth-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-ink);
  text-align: center;
  margin-bottom: var(--spacing-xl);
}
.auth-form {
  display: flex;
  flex-direction: column;
}
.field {
  margin-bottom: var(--spacing-base);
}
.field-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-muted);
  margin-bottom: 6px;
}
.required { color: var(--color-primary); }
.auth-error {
  padding: var(--spacing-sm) var(--spacing-md);
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: var(--radius-input);
  font-size: 13px;
  color: var(--color-error-text);
  margin-bottom: var(--spacing-base);
}
.auth-submit {
  width: 100%;
  height: 48px;
  background: var(--color-primary);
  color: var(--color-on-primary);
  border: none;
  border-radius: var(--radius-button);
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  margin-top: var(--spacing-sm);
  transition: background var(--transition-fast);
}
.auth-submit:hover { background: var(--color-primary-dark); }
.auth-submit:active { background: var(--color-primary-active); }
.auth-submit:disabled { background: var(--color-primary-disabled); cursor: not-allowed; }
.auth-switch {
  text-align: center;
  margin-top: var(--spacing-lg);
  font-size: 14px;
  color: var(--color-muted);
}
.auth-switch a {
  color: var(--color-primary);
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
}
.auth-switch a:hover { text-decoration: underline; }
</style>
