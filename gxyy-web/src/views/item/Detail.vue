<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { itemApi } from '../../api/item'
import { exchangeApi } from '../../api/exchange'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const item = ref(null)
const loading = ref(true)

const showExchangeDialog = ref(false)
const myItems = ref([])
const selectedItemId = ref(null)
const exchangeMessage = ref('')
const exchangeLoading = ref(false)

const conditionMap = {
  'NEW': '全新',
  'LIKE_NEW': '几乎全新',
  'SLIGHTLY_USED': '轻微使用',
  'NORMAL_USE': '正常使用',
}

const statusMap = {
  'ACTIVE': '在架',
  'EXCHANGED': '已换出',
  'OFF_SHELF': '已下架',
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await itemApi.detail(route.params.id)
    item.value = res.data
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const fetchMyItems = async () => {
  try {
    const res = await itemApi.myItems()
    myItems.value = res.data.filter(i => i.status === 'ACTIVE' && i.id !== item.value.id)
  } catch (e) { /* ignore */ }
}

const openExchange = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  if (item.value.ownerId === userStore.user?.id) {
    ElMessage.warning('不能换取自己的物品')
    return
  }
  fetchMyItems()
  showExchangeDialog.value = true
}

const submitExchange = async () => {
  if (!selectedItemId.value) {
    ElMessage.warning('请选择你要交换的物品')
    return
  }
  exchangeLoading.value = true
  try {
    await exchangeApi.create({
      toItemId: item.value.id,
      fromItemId: selectedItemId.value,
      message: exchangeMessage.value,
    })
    ElMessage.success('交换请求已发送')
    showExchangeDialog.value = false
  } catch (e) {
    // handled by interceptor
  } finally {
    exchangeLoading.value = false
  }
}

const isOwner = () => item.value?.ownerId === userStore.user?.id

onMounted(fetchDetail)
</script>

<template>
  <div v-loading="loading">
    <el-empty v-if="!item" description="物品不存在" />

    <div v-else class="detail-container">
      <!-- 图片轮播 -->
      <div class="images-section">
        <el-image
          v-if="item.images && item.images.length > 0"
          :src="item.images[0]"
          fit="contain"
          class="main-image"
        >
          <template #error>
            <div class="image-placeholder">
              <el-icon :size="64"><PictureFilled /></el-icon>
            </div>
          </template>
        </el-image>
        <div v-else class="image-placeholder">
          <el-icon :size="64"><PictureFilled /></el-icon>
          <p>暂无图片</p>
        </div>
        <div class="image-list" v-if="(item.thumbImages || item.images) && (item.thumbImages || item.images).length > 1">
          <el-image
            v-for="(img, idx) in (item.thumbImages || item.images)"
            :key="idx"
            :src="img"
            fit="cover"
            class="thumb-image"
          />
        </div>
      </div>

      <!-- 物品信息 -->
      <div class="info-section">
        <div class="header-row">
          <h1>{{ item.title }}</h1>
          <el-tag :type="item.status === 'ACTIVE' ? 'success' : 'info'" size="large">
            {{ statusMap[item.status] || item.status }}
          </el-tag>
        </div>

        <div class="tags-row">
          <el-tag type="primary">{{ item.categoryName }}</el-tag>
          <el-tag>{{ conditionMap[item.condition] || item.condition }}</el-tag>
        </div>

        <el-divider />

        <div class="desc-section">
          <h3>物品描述</h3>
          <p>{{ item.description }}</p>
        </div>

        <div class="desc-section" v-if="item.wantDescription">
          <h3 style="color: #f56c6c">想换什么</h3>
          <p>{{ item.wantDescription }}</p>
        </div>

        <el-divider />

        <div class="owner-section" @click="router.push(`/user/${item.ownerId}`)">
          <el-avatar :size="48" :src="item.ownerAvatar" />
          <div class="owner-info">
            <p class="owner-name">{{ item.ownerName }}</p>
            <p class="owner-time">发布于 {{ item.createTime }}</p>
          </div>
        </div>

        <div class="action-row" v-if="item.status === 'ACTIVE'">
          <el-button
            v-if="!isOwner()"
            type="primary"
            size="large"
            @click="openExchange"
            style="width: 100%"
          >
            <el-icon><Switch /></el-icon> 想换！
          </el-button>
          <el-button
            v-else
            type="warning"
            size="large"
            @click="router.push(`/item/${item.id}/edit`)"
            style="width: 100%"
          >
            编辑物品
          </el-button>
        </div>
      </div>
    </div>

    <!-- 发起交换弹窗 -->
    <el-dialog v-model="showExchangeDialog" title="选择交换物品" width="500px">
      <el-form label-position="top">
        <el-form-item label="选择你要交换的物品">
          <el-select v-model="selectedItemId" placeholder="请选择你的物品" style="width: 100%">
            <el-option
              v-for="myItem in myItems"
              :key="myItem.id"
              :label="myItem.title"
              :value="myItem.id"
            >
              <span>{{ myItem.title }}</span>
              <span style="color: #999; margin-left: 8px; font-size: 12px">
                {{ myItem.categoryName }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="留言（选填）">
          <el-input
            v-model="exchangeMessage"
            type="textarea"
            :rows="3"
            placeholder="介绍一下你的物品..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showExchangeDialog = false">取消</el-button>
        <el-button type="primary" :loading="exchangeLoading" @click="submitExchange">
          发起交换
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

@media (max-width: 768px) {
  .detail-container {
    grid-template-columns: 1fr;
  }
}

.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
}

.image-placeholder {
  width: 100%;
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  color: #c0c4cc;
}

.image-list {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.thumb-image {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
}
.thumb-image:hover {
  border-color: #409eff;
}

.info-section {
  display: flex;
  flex-direction: column;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-row h1 {
  font-size: 24px;
  margin: 0;
}

.tags-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.desc-section {
  margin-bottom: 16px;
}

.desc-section h3 {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.desc-section p {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

.owner-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.owner-section:hover {
  background: #f5f7fa;
}

.owner-name {
  font-size: 16px;
  font-weight: 600;
}

.owner-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.action-row {
  margin-top: auto;
  padding-top: 16px;
}
</style>
