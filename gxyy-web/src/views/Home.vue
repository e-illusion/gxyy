<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { itemApi } from '../api/item'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const items = ref([])
const total = ref(0)
const loading = ref(false)

const keyword = ref('')
const categoryId = ref(null)
const page = ref(1)
const size = ref(12)

const categories = [
  { id: null, name: '全部' },
  { id: 1, name: '数码电子' },
  { id: 2, name: '书籍教材' },
  { id: 3, name: '生活用品' },
  { id: 4, name: '服饰鞋包' },
  { id: 5, name: '运动器材' },
  { id: 6, name: '美妆护肤' },
  { id: 7, name: '玩具乐器' },
  { id: 8, name: '其他' },
]

const conditionMap = {
  'NEW': '全新',
  'LIKE_NEW': '几乎全新',
  'SLIGHTLY_USED': '轻微使用',
  'NORMAL_USE': '正常使用',
}

const fetchItems = async () => {
  loading.value = true
  try {
    const res = await itemApi.list({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
    })
    items.value = res.data.records
    total.value = res.data.total
  } catch (e) {
    // 已由拦截器处理
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchItems()
}

const handleCategoryChange = (id) => {
  categoryId.value = id
  page.value = 1
  fetchItems()
}

const goDetail = (id) => router.push(`/item/${id}`)
const goCreate = () => router.push('/item/create')

const getFirstImage = (item) => {
  if (item.thumbImages && item.thumbImages.length > 0) return item.thumbImages[0]
  if (item.images && item.images.length > 0) return item.images[0]
  return ''
}

onMounted(fetchItems)
</script>

<template>
  <div>
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索物品..." size="large" clearable
        @keyup.enter="handleSearch" @clear="handleSearch" style="max-width: 400px">
        <template #append>
          <el-button @click="handleSearch" :icon="'Search'" />
        </template>
      </el-input>
      <el-button v-if="userStore.isLoggedIn" type="primary" size="large" @click="goCreate"
        style="margin-left: 16px">
        <el-icon><Plus /></el-icon> 发布物品
      </el-button>
    </div>

    <!-- 分类标签 -->
    <div class="category-tabs">
      <el-tag
        v-for="cat in categories"
        :key="cat.id"
        :effect="categoryId === cat.id ? 'dark' : 'plain'"
        :type="categoryId === cat.id ? 'primary' : ''"
        size="large"
        class="category-tag"
        @click="handleCategoryChange(cat.id)"
      >
        {{ cat.name }}
      </el-tag>
    </div>

    <!-- 物品列表 -->
    <div v-loading="loading">
      <el-empty v-if="!loading && items.length === 0" description="暂无物品" />

      <div v-else class="item-grid">
        <el-card
          v-for="item in items"
          :key="item.id"
          class="item-card"
          shadow="hover"
          @click="goDetail(item.id)"
        >
          <el-image
            :src="getFirstImage(item)"
            fit="cover"
            class="item-image"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon :size="48"><PictureFilled /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="item-info">
            <h3 class="item-title">{{ item.title }}</h3>
            <div class="item-meta">
              <el-tag size="small" type="info">{{ item.categoryName }}</el-tag>
              <el-tag size="small" style="margin-left: 4px">{{ conditionMap[item.condition] || item.condition }}</el-tag>
            </div>
            <p class="item-want" v-if="item.wantDescription">
              想换：{{ item.wantDescription.substring(0, 30) }}{{ item.wantDescription.length > 30 ? '...' : '' }}
            </p>
            <div class="item-owner" @click.stop="router.push(`/user/${item.ownerId}`)">
              <el-avatar :size="20" :src="item.ownerAvatar" />
              <span>{{ item.ownerName }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchItems"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

.category-tabs {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.category-tag {
  cursor: pointer;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 1000px) {
  .item-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 760px) {
  .item-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.item-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.item-card:hover {
  transform: translateY(-2px);
}
.item-card :deep(.el-card__body) {
  padding: 0;
}

.item-image {
  width: 100%;
  height: 200px;
}

.image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #c0c4cc;
}

.item-info {
  padding: 12px;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  margin-bottom: 6px;
}

.item-want {
  font-size: 12px;
  color: #f56c6c;
  margin-bottom: 8px;
  height: 20px;
}

.item-owner {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  padding: 4px 0;
  border-radius: 4px;
  transition: color 0.2s;
}
.item-owner:hover {
  color: #409eff;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
