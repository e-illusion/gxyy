<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { itemApi } from '../../api/item'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const form = reactive({
  title: '',
  categoryId: null,
  condition: 'LIKE_NEW',
  description: '',
  wantDescription: '',
})
const loading = ref(false)

const categories = [
  { id: 1, name: '数码电子' },
  { id: 2, name: '书籍教材' },
  { id: 3, name: '生活用品' },
  { id: 4, name: '服饰鞋包' },
  { id: 5, name: '运动器材' },
  { id: 6, name: '美妆护肤' },
  { id: 7, name: '玩具乐器' },
  { id: 8, name: '其他' },
]

const conditionOptions = [
  { value: 'NEW', label: '全新' },
  { value: 'LIKE_NEW', label: '几乎全新' },
  { value: 'SLIGHTLY_USED', label: '轻微使用' },
  { value: 'NORMAL_USE', label: '正常使用' },
]

onMounted(async () => {
  try {
    const res = await itemApi.detail(route.params.id)
    const item = res.data
    form.title = item.title
    form.categoryId = item.categoryId
    form.condition = item.condition
    form.description = item.description
    form.wantDescription = item.wantDescription
  } catch (e) {
    ElMessage.error('加载物品信息失败')
    router.push('/')
  }
})

const submit = async () => {
  loading.value = true
  try {
    await itemApi.update(route.params.id, form)
    ElMessage.success('更新成功')
    router.push(`/item/${route.params.id}`)
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="form-page">
    <div class="form-card">
      <h1 class="form-title">编辑物品</h1>

      <div class="form-body">
        <div class="field">
          <label class="field-label">物品名称</label>
          <el-input v-model="form.title" size="large" />
        </div>

        <div class="field-row">
          <div class="field field-half">
            <label class="field-label">分类</label>
            <el-select v-model="form.categoryId" size="large" style="width: 100%">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </div>
          <div class="field field-half">
            <label class="field-label">成色</label>
            <el-select v-model="form.condition" size="large" style="width: 100%">
              <el-option v-for="c in conditionOptions" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </div>
        </div>

        <div class="field">
          <label class="field-label">物品描述</label>
          <el-input v-model="form.description" type="textarea" :rows="5" />
        </div>

        <div class="field">
          <label class="field-label">想换什么</label>
          <el-input v-model="form.wantDescription" type="textarea" :rows="2" />
        </div>

        <button class="submit-btn" :disabled="loading" @click="submit">
          {{ loading ? '保存中...' : '保存修改' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form-page {
  max-width: 720px;
  margin: 0 auto;
}

.form-card {
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
}

.form-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: var(--spacing-xl);
}

.form-body {
  display: flex;
  flex-direction: column;
}

.field {
  margin-bottom: var(--spacing-lg);
}

.field-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-muted);
  margin-bottom: 6px;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-base);
}
.field-half { margin-bottom: var(--spacing-lg); }

.submit-btn {
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
.submit-btn:hover { background: var(--color-primary-dark); }
.submit-btn:active { background: var(--color-primary-active); }
.submit-btn:disabled { background: var(--color-primary-disabled); cursor: not-allowed; }
</style>
