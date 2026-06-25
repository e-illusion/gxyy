<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { favoriteApi } from '../../api/favorite'
import { useFavoriteStore } from '../../stores/favorite'
import { ElMessage } from 'element-plus'

const router = useRouter()
const favoriteStore = useFavoriteStore()

const items = ref([])
const loading = ref(true)
const filterStatus = ref('')
const filterCondition = ref('')

const filteredItems = computed(() => {
  let list = items.value
  if (filterStatus.value) list = list.filter(i => i.status === filterStatus.value)
  if (filterCondition.value) list = list.filter(i => i.condition === filterCondition.value)
  return list
})

const conditionMap = {
  'NEW': '全新', 'LIKE_NEW': '几乎全新', 'SLIGHTLY_USED': '轻微使用', 'NORMAL_USE': '正常使用',
}

const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await favoriteApi.list()
    items.value = res.data
    favoriteStore.setFavorites(res.data)
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

const goDetail = (id) => router.push(`/item/${id}`)

const handleRemove = async (item) => {
  try {
    await favoriteApi.remove(item.id)
    items.value = items.value.filter(i => i.id !== item.id)
    favoriteStore.removeFavorite(item.id)
    ElMessage.success('已取消收藏')
  } catch (e) { /* ignore */ }
}

const getFirstImage = (item) => {
  if (item.thumbImages && item.thumbImages.length > 0) return item.thumbImages[0]
  if (item.images && item.images.length > 0) return item.images[0]
  return ''
}

onMounted(fetchFavorites)
</script>

<template>
  <div>
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">我的收藏</h1>
        <span class="page-count" v-if="items.length">{{ items.length }} 件物品</span>
      </div>
      <div class="header-right">
        <el-select v-model="filterStatus" placeholder="状态" clearable size="small" style="width:110px">
          <el-option label="在架" value="ACTIVE" />
          <el-option label="已换出" value="EXCHANGED" />
          <el-option label="已下架" value="OFF_SHELF" />
        </el-select>
        <el-select v-model="filterCondition" placeholder="成色" clearable size="small" style="width:110px">
          <el-option label="全新" value="NEW" />
          <el-option label="几乎全新" value="LIKE_NEW" />
          <el-option label="轻微使用" value="SLIGHTLY_USED" />
          <el-option label="正常使用" value="NORMAL_USE" />
        </el-select>
      </div>
    </div>

    <div v-loading="loading">
      <div v-if="!loading && items.length === 0" class="empty-state">
        <el-icon :size="48" color="var(--color-muted-soft)"><Star /></el-icon>
        <p>还没有收藏任何物品</p>
        <button class="browse-btn" @click="router.push('/')">去首页看看</button>
      </div>
      <div v-else-if="!loading && filteredItems.length === 0" class="empty-state">
        <p>没有符合筛选条件的物品</p>
      </div>

      <div v-else class="item-grid">
        <article v-for="item in filteredItems" :key="item.id" class="property-card" @click="goDetail(item.id)">
          <div class="property-photo">
            <el-image v-if="getFirstImage(item)" :src="getFirstImage(item)" fit="cover" class="property-img">
              <template #error><div class="property-img-fallback"><el-icon :size="32"><PictureFilled /></el-icon></div></template>
            </el-image>
            <div v-else class="property-img-fallback"><el-icon :size="32"><PictureFilled /></el-icon></div>
            <button class="heart-btn favorited" @click.stop="handleRemove(item)" title="取消收藏">
              <el-icon :size="18"><StarFilled /></el-icon>
            </button>
          </div>
          <div class="property-meta">
            <h3 class="property-title">{{ item.title }}</h3>
            <div class="property-tags">
              <span class="property-category">{{ item.categoryName }}</span>
              <span class="property-condition">{{ conditionMap[item.condition] || item.condition }}</span>
              <span v-if="item.status !== 'ACTIVE'" class="property-status">{{ item.status === 'EXCHANGED' ? '已换出' : '已下架' }}</span>
            </div>
            <div class="property-owner" @click.stop="router.push('/user/' + item.ownerId)">
              <el-avatar :size="18" :src="item.ownerAvatar" />
              <span>{{ item.ownerName }}</span>
            </div>
          </div>
        </article>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--spacing-sm); margin-bottom: var(--spacing-xl); }
.header-left { display: flex; align-items: baseline; gap: var(--spacing-md); }
.header-right { display: flex; gap: var(--spacing-sm); }
.page-title { font-size: 22px; font-weight: 600; color: var(--color-ink); }
.page-count { font-size: 14px; color: var(--color-muted); }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: var(--spacing-base); padding: var(--spacing-section) 0; color: var(--color-muted-soft); }
.browse-btn { padding: 10px 24px; font-size: 14px; font-weight: 500; color: var(--color-on-primary); background: var(--color-primary); border: none; border-radius: var(--radius-button); cursor: pointer; font-family: inherit; }

.item-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: var(--spacing-base); }
@media (max-width: 1280px) { .item-grid { grid-template-columns: repeat(5, 1fr); } }
@media (max-width: 1000px) { .item-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 744px) { .item-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 480px) { .item-grid { grid-template-columns: repeat(2, 1fr); } }

.property-card { cursor: pointer; border-radius: var(--radius-md); background: var(--color-canvas); border: 1px solid var(--color-hairline); overflow: hidden; transition: box-shadow var(--transition-normal), transform var(--transition-normal); }
.property-card:hover { box-shadow: var(--shadow-card-hover); transform: translateY(-2px); }
.property-photo { position: relative; }
.property-img { width: 100%; aspect-ratio: 1 / 1; display: block; }
.property-img-fallback { width: 100%; aspect-ratio: 1 / 1; display: flex; align-items: center; justify-content: center; background: var(--color-surface-soft); color: var(--color-muted-soft); }

.heart-btn { position: absolute; top: 8px; right: 8px; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.9); border: none; border-radius: 50%; cursor: pointer; color: var(--color-muted-soft); transition: color var(--transition-fast), transform var(--transition-fast); }
.heart-btn:hover { transform: scale(1.15); }
.heart-btn.favorited { color: var(--color-primary); }

.property-meta { padding: var(--spacing-sm) var(--spacing-md) var(--spacing-md); }
.property-title { font-size: 14px; font-weight: 600; color: var(--color-ink); margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.property-tags { display: flex; gap: 4px; margin-bottom: 6px; }
.property-category, .property-condition { font-size: 11px; font-weight: 500; color: var(--color-muted); background: var(--color-surface-soft); padding: 1px 6px; border-radius: var(--radius-tag); }
.property-status { font-size: 11px; font-weight: 500; color: var(--color-muted-soft); background: var(--color-surface-soft); padding: 1px 6px; border-radius: var(--radius-tag); }
.property-owner { display: flex; align-items: center; gap: 4px; font-size: 11px; color: var(--color-muted); cursor: pointer; transition: color var(--transition-fast); }
.property-owner:hover { color: var(--color-ink); }
</style>
