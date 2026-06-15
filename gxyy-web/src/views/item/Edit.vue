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
  <div class="create-container">
    <el-card>
      <template #header>
        <h2>编辑物品</h2>
      </template>
      <el-form label-position="top" style="max-width: 640px; margin: 0 auto">
        <el-form-item label="物品名称">
          <el-input v-model="form.title" size="large" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" size="large" style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="成色">
              <el-select v-model="form.condition" size="large" style="width: 100%">
                <el-option v-for="c in conditionOptions" :key="c.value" :label="c.label" :value="c.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="物品描述">
          <el-input v-model="form.description" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="想换什么">
          <el-input v-model="form.wantDescription" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="submit" style="width: 100%">
            保存修改
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
