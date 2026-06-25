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
    await router.replace('/')
  } catch (e) {
    errorMsg.value = e?.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">登录 GXYY</h2>

      <div class="auth-form">
        <div class="field">
          <label class="field-label">用户名</label>
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" @keyup.enter="handleLogin" />
        </div>

        <div class="field">
          <label class="field-label">密码</label>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </div>

        <div v-if="errorMsg" class="auth-error">{{ errorMsg }}</div>

        <button class="auth-submit" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>

      <p class="auth-switch">
        还没有账号？<a @click="router.push('/register')">立即注册</a>
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
  width: 400px;
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
