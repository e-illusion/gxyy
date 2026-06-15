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
  <div class="create-container">
    <el-card>
      <template #header>
        <h2>发布物品</h2>
      </template>
      <el-form label-position="top" style="max-width: 640px; margin: 0 auto">
        <el-form-item label="物品名称 *">
          <el-input v-model="form.title" placeholder="给你的物品起个名字" size="large" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类 *">
              <el-select v-model="form.categoryId" placeholder="选择分类" size="large" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成色 *">
              <el-select v-model="form.condition" size="large" style="width: 100%">
                <el-option v-for="c in conditionOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <template #label>
            <span>物品描述 *</span>
            <el-button size="small" type="warning" :loading="aiLoading" @click="aiPolish"
              style="margin-left: 12px">
              ✨ AI 润色
            </el-button>
          </template>
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="描述你的物品：品牌、型号、购买时间、使用情况等..."
          />
        </el-form-item>

        <el-form-item label="想换什么">
          <el-input
            v-model="form.wantDescription"
            type="textarea"
            :rows="2"
            placeholder="你想换到什么样的物品？（选填）"
          />
        </el-form-item>

        <el-form-item label="物品图片">
          <el-upload
            :auto-upload="false"
            :on-change="handleFileChange"
            list-type="picture-card"
            multiple
            accept="image/*"
          >
            <el-icon :size="32"><Plus /></el-icon>
          </el-upload>
          <template #extra>
            <span style="font-size: 12px; color: #999">支持多张图片，每张不超过10MB</span>
          </template>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="submit" style="width: 100%">
            发布
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.create-container {
  max-width: 800px;
  margin: 0 auto;
}
</style>
