<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { itemApi } from '../../api/item'
import { aiApi } from '../../api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()

const form = reactive({
  title: '',
  categoryId: null,
  condition: 'LIKE_NEW',
  description: '',
  wantDescription: '',
})
const images = ref([])
const loading = ref(false)
const aiLoading = ref(false)

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

const handleFileChange = (file) => {
  images.value.push(file.raw)
}

const handleRemoveFile = (index) => {
  images.value.splice(index, 1)
}

const aiPolish = async () => {
  if (!form.description) {
    ElMessage.warning('请先输入描述内容')
    return
  }
  aiLoading.value = true
  try {
    const res = await aiApi.polish(form.description)
    form.description = res.data.polished
    ElMessage.success('AI 润色完成')
  } catch (e) {
    // handled by interceptor
  } finally {
    aiLoading.value = false
  }
}

const submit = async () => {
  if (!form.title || !form.categoryId || !form.condition || !form.description) {
    ElMessage.warning('请填写必填项')
    return
  }
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('title', form.title)
    formData.append('categoryId', form.categoryId)
    formData.append('condition', form.condition)
    formData.append('description', form.description)
    formData.append('wantDescription', form.wantDescription || '')
    images.value.forEach((img) => formData.append('images', img))

    await itemApi.create(formData)
    ElMessage.success('发布成功')
    router.push('/')
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
      <h1 class="form-title">发布物品</h1>

      <div class="form-body">
        <!-- 物品名称 -->
        <div class="field">
          <label class="field-label">物品名称 <span class="required">*</span></label>
          <el-input v-model="form.title" placeholder="给你的物品起个名字" size="large" />
        </div>

        <!-- 分类 + 成色 -->
        <div class="field-row">
          <div class="field field-half">
            <label class="field-label">分类 <span class="required">*</span></label>
            <el-select v-model="form.categoryId" placeholder="选择分类" size="large" style="width: 100%">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
            </el-select>
          </div>
          <div class="field field-half">
            <label class="field-label">成色 <span class="required">*</span></label>
            <el-select v-model="form.condition" size="large" style="width: 100%">
              <el-option v-for="c in conditionOptions" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
          </div>
        </div>

        <!-- 物品描述 -->
        <div class="field">
          <div class="field-label-row">
            <label class="field-label">物品描述 <span class="required">*</span></label>
            <button type="button" class="ai-btn" :disabled="aiLoading" @click="aiPolish">
              {{ aiLoading ? '润色中...' : 'AI 润色' }}
            </button>
          </div>
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="描述你的物品：品牌、型号、购买时间、使用情况等..."
          />
        </div>

        <!-- 想换什么 -->
        <div class="field">
          <label class="field-label">想换什么</label>
          <el-input
            v-model="form.wantDescription"
            type="textarea"
            :rows="2"
            placeholder="你想换到什么样的物品？（选填）"
          />
        </div>

        <!-- 图片上传 -->
        <div class="field">
          <label class="field-label">物品图片</label>
          <el-upload
            :auto-upload="false"
            :on-change="handleFileChange"
            list-type="picture-card"
            multiple
            accept="image/*"
          >
            <el-icon :size="32"><Plus /></el-icon>
          </el-upload>
          <p class="field-hint">支持多张图片，每张不超过10MB</p>
        </div>

        <!-- 提交 -->
        <button class="submit-btn" :disabled="loading" @click="submit">
          {{ loading ? '发布中...' : '发布' }}
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
.required { color: var(--color-primary); }

.field-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.ai-btn {
  background: var(--color-surface-soft);
  color: var(--color-primary);
  border: 1px solid var(--color-hairline);
  padding: 4px 12px;
  font-size: 13px;
  font-weight: 500;
  border-radius: var(--radius-tag);
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast);
}
.ai-btn:hover { background: var(--color-surface-strong); }
.ai-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-base);
}
.field-half { margin-bottom: var(--spacing-lg); }

.field-hint {
  font-size: 12px;
  color: var(--color-muted-soft);
  margin-top: var(--spacing-xs);
}

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
