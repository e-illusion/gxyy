<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { itemApi } from '../api/item'
import { favoriteApi } from '../api/favorite'
import { useUserStore } from '../stores/user'
import { useFavoriteStore } from '../stores/favorite'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const favoriteStore = useFavoriteStore()

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
    // 加载收藏状态
    if (userStore.isLoggedIn) {
      try {
        const favRes = await favoriteApi.list()
        favoriteStore.setFavorites(favRes.data)
      } catch (e) { /* ignore */ }
    }
  } catch (e) {
    // handled by interceptor
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

const toggleFavorite = async (item) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (favoriteStore.isFavorited(item.id)) {
      await favoriteApi.remove(item.id)
      favoriteStore.removeFavorite(item.id)
    } else {
      await favoriteApi.add(item.id)
      favoriteStore.addFavorite(item)
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  }
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
    <!-- Airbnb-style Pill Search Bar -->
    <div class="search-section">
      <div class="search-pill">
        <el-icon :size="18" class="search-icon"><Search /></el-icon>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索物品..."
          @keyup.enter="handleSearch"
        />
        <button v-if="keyword" class="search-clear" @click="keyword = ''; handleSearch()">
          <el-icon :size="14"><Close /></el-icon>
        </button>
        <div class="search-divider" />
        <button class="search-orb" @click="handleSearch" aria-label="搜索">
          <el-icon :size="18"><Search /></el-icon>
        </button>
      </div>
      <button v-if="userStore.isLoggedIn" class="create-btn" @click="goCreate">
        <el-icon :size="16"><Plus /></el-icon>
        <span>发布物品</span>
      </button>
    </div>

    <!-- Airbnb-style Category Scroll Strip -->
    <div class="category-strip">
      <button
        v-for="cat in categories"
        :key="cat.id"
        class="category-pill"
        :class="{ active: categoryId === cat.id }"
        @click="handleCategoryChange(cat.id)"
      >
        {{ cat.name }}
      </button>
    </div>

    <!-- Item Grid -->
    <div v-loading="loading" class="items-section">
      <el-empty v-if="!loading && items.length === 0" description="暂无物品" />

      <div v-else class="item-grid">
        <!-- Airbnb Property Card -->
        <article
          v-for="item in items"
          :key="item.id"
          class="property-card"
          @click="goDetail(item.id)"
        >
          <div class="property-photo">
            <button
              class="heart-btn"
              :class="{ favorited: favoriteStore.isFavorited(item.id) }"
              @click.stop="toggleFavorite(item)"
              :title="favoriteStore.isFavorited(item.id) ? '取消收藏' : '收藏'"
            >
              <el-icon :size="16">
                <StarFilled v-if="favoriteStore.isFavorited(item.id)" />
                <Star v-else />
              </el-icon>
            </button>
            <el-image
              v-if="getFirstImage(item)"
              :src="getFirstImage(item)"
              fit="cover"
              class="property-img"
            >
              <template #error>
                <div class="property-img-fallback">
                  <el-icon :size="40"><PictureFilled /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="property-img-fallback">
              <el-icon :size="40"><PictureFilled /></el-icon>
            </div>
          </div>
          <div class="property-meta">
            <h3 class="property-title">{{ item.title }}</h3>
            <div class="property-tags">
              <span class="property-category">{{ item.categoryName }}</span>
              <span class="property-condition">{{ conditionMap[item.condition] || item.condition }}</span>
            </div>
            <p class="property-want" v-if="item.wantDescription">
              想换：{{ item.wantDescription.substring(0, 30) }}{{ item.wantDescription.length > 30 ? '...' : '' }}
            </p>
            <div class="property-owner" @click.stop="router.push(`/user/${item.ownerId}`)">
              <el-avatar :size="20" :src="item.ownerAvatar" />
              <span>{{ item.ownerName }}</span>
            </div>
          </div>
        </article>
      </div>
    </div>

    <!-- Pagination -->
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
/* ══════════════════════════════════════
   Search Section
   ══════════════════════════════════════ */
.search-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-base);
  margin-bottom: var(--spacing-lg);
}

/* Airbnb pill search bar */
.search-pill {
  display: flex;
  align-items: center;
  height: 56px;
  max-width: 480px;
  width: 100%;
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-card);
  padding: 0 var(--spacing-sm);
  transition: box-shadow var(--transition-normal);
}
.search-pill:focus-within {
  box-shadow: var(--shadow-card-hover);
}

.search-icon {
  color: var(--color-muted);
  margin: 0 var(--spacing-xs) 0 var(--spacing-sm);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  font-weight: 400;
  color: var(--color-ink);
  background: transparent;
  font-family: inherit;
  min-width: 0;
}
.search-input::placeholder {
  color: var(--color-muted-soft);
}

.search-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-surface-soft);
  border: none;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  cursor: pointer;
  color: var(--color-muted);
  flex-shrink: 0;
  transition: background var(--transition-fast);
}
.search-clear:hover {
  background: var(--color-surface-strong);
}

.search-divider {
  width: 1px;
  height: 24px;
  background: var(--color-hairline);
  margin: 0 var(--spacing-sm);
}

/* Airbnb search orb (Rausch circle) */
.search-orb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--color-primary);
  border: none;
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  flex-shrink: 0;
  transition: background var(--transition-fast);
}
.search-orb:hover {
  background: var(--color-primary-dark);
}
.search-orb:active {
  background: var(--color-primary-active);
}

/* Create button */
.create-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 48px;
  padding: 0 var(--spacing-lg);
  background: var(--color-primary);
  color: var(--color-on-primary);
  border: none;
  border-radius: var(--radius-button);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
  transition: background var(--transition-fast);
}
.create-btn:hover {
  background: var(--color-primary-dark);
}
.create-btn:active {
  background: var(--color-primary-active);
}

/* ══════════════════════════════════════
   Category Scroll Strip (Airbnb style)
   ══════════════════════════════════════ */
.category-strip {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xl);
}

.category-pill {
  padding: var(--spacing-sm) var(--spacing-base);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-muted);
  background: none;
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  font-family: inherit;
  transition: color var(--transition-fast), background var(--transition-fast);
}
.category-pill:hover {
  color: var(--color-ink);
  background: var(--color-surface-soft);
}
.category-pill.active {
  color: var(--color-ink);
  background: var(--color-surface-strong);
}

/* ══════════════════════════════════════
   Item Grid — Airbnb Property Cards
   ══════════════════════════════════════ */
.items-section {
  min-height: 200px;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--spacing-base);
}

@media (max-width: 1100px) {
  .item-grid { grid-template-columns: repeat(5, 1fr); }
}
@media (max-width: 1000px) {
  .item-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 744px) {
  .item-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 480px) {
  .item-grid { grid-template-columns: repeat(2, 1fr); }
}

/* Airbnb property card */
.property-card {
  cursor: pointer;
  border-radius: var(--radius-md);
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  overflow: hidden;
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
}
.property-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

/* Photo plate */
.property-photo {
  position: relative;
}
/* Heart button */
.heart-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.85);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  color: var(--color-muted-soft);
  z-index: 2;
  transition: color var(--transition-fast), transform var(--transition-fast);
}
.heart-btn:hover { transform: scale(1.15); }
.heart-btn.favorited { color: var(--color-primary); }
.property-img {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: block;
}
.property-img-fallback {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-surface-soft);
  color: var(--color-muted-soft);
}

/* Card meta */
.property-meta {
  padding: var(--spacing-md);
}

.property-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.property-tags {
  display: flex;
  gap: 4px;
  margin-bottom: 6px;
}
.property-category {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-muted);
  background: var(--color-surface-soft);
  padding: 2px 8px;
  border-radius: var(--radius-tag);
}
.property-condition {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-muted);
  background: var(--color-surface-soft);
  padding: 2px 8px;
  border-radius: var(--radius-tag);
}

.property-want {
  font-size: 12px;
  color: var(--color-primary);
  margin-bottom: var(--spacing-sm);
  height: 18px;
  overflow: hidden;
}

.property-owner {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--color-muted);
  cursor: pointer;
  padding: 2px 0;
  transition: color var(--transition-fast);
}
.property-owner:hover {
  color: var(--color-ink);
}

/* ══════════════════════════════════════
   Pagination
   ══════════════════════════════════════ */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-xl);
}

@media (max-width: 600px) {
  .search-section {
    flex-direction: column;
    gap: var(--spacing-sm);
  }
  .search-pill {
    max-width: 100%;
    height: 48px;
  }
  .create-btn {
    width: 100%;
    justify-content: center;
    height: 40px;
    font-size: 13px;
  }
}
</style>
