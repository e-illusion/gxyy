<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { itemApi } from '../../api/item'
import { exchangeApi } from '../../api/exchange'
import { favoriteApi } from '../../api/favorite'
import { aiApi } from '../../api/user'
import { useUserStore } from '../../stores/user'
import { useFavoriteStore } from '../../stores/favorite'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const favoriteStore = useFavoriteStore()

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

const isFav = ref(false)

// AI match state
const showAiDialog = ref(false)
const aiLoading = ref(false)
const aiMatches = ref([])

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await itemApi.detail(route.params.id)
    item.value = res.data
    // 检查收藏状态
    if (userStore.isLoggedIn) {
      try {
        const favRes = await favoriteApi.check(item.value.id)
        isFav.value = favRes.data.isFavorited
      } catch (e) { /* ignore */ }
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFav.value) {
      await favoriteApi.remove(item.value.id)
      isFav.value = false
    } else {
      await favoriteApi.add(item.value.id)
      isFav.value = true
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
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

const openAiMatch = async () => {
  if (!item.value) return
  showAiDialog.value = true
  aiLoading.value = true
  aiMatches.value = []
  try {
    const res = await aiApi.match(item.value.id)
    aiMatches.value = res.data
  } catch (e) {
    ElMessage.error(e?.message || 'AI推荐失败')
    showAiDialog.value = false
  } finally {
    aiLoading.value = false
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div v-loading="loading">
    <el-empty v-if="!item" description="物品不存在" />

    <!-- Airbnb listing detail: 2-column layout -->
    <div v-else class="listing-detail">
      <!-- Left: Photo + content -->
      <div class="listing-body">
        <!-- Photo -->
        <div class="listing-photo" v-if="item.images && item.images.length > 0">
          <el-image :src="item.images[0]" fit="cover" class="listing-img" :preview-src-list="item.images">
            <template #error>
              <div class="listing-img-fallback">
                <el-icon :size="56"><PictureFilled /></el-icon>
              </div>
            </template>
          </el-image>
        </div>
        <div v-else class="listing-img-fallback">
          <el-icon :size="56"><PictureFilled /></el-icon>
          <p>暂无图片</p>
        </div>

        <!-- Title + tags -->
        <h1 class="listing-title">{{ item.title }}</h1>
        <div class="listing-tags">
          <span class="listing-badge">{{ item.categoryName }}</span>
          <span class="listing-badge">{{ conditionMap[item.condition] || item.condition }}</span>
          <span class="listing-badge" :class="item.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'">
            {{ statusMap[item.status] || item.status }}
          </span>
        </div>

        <!-- Divider -->
        <div class="listing-divider" />

        <!-- Description -->
        <section class="listing-section">
          <h3 class="section-heading">物品描述</h3>
          <p class="section-text">{{ item.description }}</p>
        </section>

        <!-- Want description -->
        <section class="listing-section" v-if="item.wantDescription">
          <h3 class="section-heading want-heading">想换什么</h3>
          <p class="section-text">{{ item.wantDescription }}</p>
        </section>

        <div class="listing-divider" />

        <!-- Owner card (Airbnb host-card style) -->
        <div class="owner-card" @click="router.push(`/user/${item.ownerId}`)">
          <el-avatar :size="48" :src="item.ownerAvatar" />
          <div class="owner-info">
            <p class="owner-name">{{ item.ownerName }}</p>
            <p class="owner-time">发布于 {{ item.createTime }}</p>
          </div>
        </div>
      </div>

      <!-- Right: Sticky reservation/action card -->
      <aside class="listing-rail">
        <div class="reservation-card">
          <!-- Status display -->
          <div class="res-status" v-if="item.status !== 'ACTIVE'">
            <span class="res-status-badge" :class="item.status === 'EXCHANGED' ? 'badge-done' : 'badge-off'">
              {{ statusMap[item.status] || item.status }}
            </span>
          </div>

          <!-- Action buttons -->
          <template v-if="item.status === 'ACTIVE'">
            <button
              v-if="!isOwner()"
              class="reserve-btn"
              @click="openExchange"
            >
              <el-icon :size="18"><Switch /></el-icon>
              <span>我想换！</span>
            </button>
            <p v-if="!isOwner()" class="res-hint">选择你的物品发起交换请求</p>
            <button
              v-else
              class="reserve-btn secondary"
              @click="router.push(`/item/${item.id}/edit`)"
            >
              编辑物品
            </button>
          </template>

          <!-- AI Match button -->
          <button
            v-if="isOwner() && item.status === 'ACTIVE'"
            class="ai-match-btn"
            @click="openAiMatch"
            style="width: 100%; margin-top: var(--spacing-md)"
          >
            <el-icon :size="16"><MagicStick /></el-icon>
            <span>AI 推荐交换</span>
          </button>

          <!-- Favorite button -->
          <button
            class="fav-btn"
            :class="{ favorited: isFav }"
            @click="toggleFavorite"
            style="width: 100%; margin-top: var(--spacing-sm)"
          >
            <el-icon :size="16"><StarFilled v-if="isFav" /><Star v-else /></el-icon>
            <span>{{ isFav ? '已收藏' : '收藏物品' }}</span>
          </button>

          <p v-if="!isOwner() && item.status === 'ACTIVE'" class="res-note">
            发起交换不会立即成交，需双方同意
          </p>
        </div>
      </aside>
    </div>

    <!-- Exchange dialog -->
    <el-dialog v-model="showExchangeDialog" title="选择交换物品" width="480px">
      <div class="exchange-form">
        <label class="field-label">选择你的物品</label>
        <el-select v-model="selectedItemId" placeholder="请选择你要交换的物品" style="width: 100%">
          <el-option
            v-for="myItem in myItems"
            :key="myItem.id"
            :label="myItem.title"
            :value="myItem.id"
          >
            <span>{{ myItem.title }}</span>
            <span class="option-extra">{{ myItem.categoryName }}</span>
          </el-option>
        </el-select>

        <label class="field-label" style="margin-top: 16px">留言（选填）</label>
        <el-input
          v-model="exchangeMessage"
          type="textarea"
          :rows="3"
          placeholder="介绍一下你的物品..."
        />
      </div>
      <template #footer>
        <button class="dialog-btn cancel" @click="showExchangeDialog = false">取消</button>
        <button class="dialog-btn submit" :disabled="exchangeLoading" @click="submitExchange">
          {{ exchangeLoading ? '发送中...' : '发起交换' }}
        </button>
      </template>
    </el-dialog>

    <!-- AI Match Dialog -->
    <el-dialog v-model="showAiDialog" title="AI 推荐交换" width="520px">
      <div v-if="aiLoading" class="ai-loading">
        <el-icon :size="32" class="ai-spin"><Loading /></el-icon>
        <p>AI 正在为你匹配最佳交换对象...</p>
      </div>
      <div v-else class="ai-matches">
        <p class="ai-intro" v-if="item">
          为 <strong>{{ item.title }}</strong> 推荐以下交换对象：
        </p>
        <div v-for="(m, idx) in aiMatches" :key="idx" class="ai-match-card" @click="router.push(`/item/${m.item.id}`)">
          <div class="ai-match-thumb">
            <el-image
              v-if="(m.item.thumbImages && m.item.thumbImages.length) || (m.item.images && m.item.images.length)"
              :src="(m.item.thumbImages && m.item.thumbImages[0]) || (m.item.images && m.item.images[0])"
              fit="cover"
            >
              <template #error>
                <div class="thumb-fb-sm"><el-icon :size="18"><PictureFilled /></el-icon></div>
              </template>
            </el-image>
            <div v-else class="thumb-fb-sm"><el-icon :size="18"><PictureFilled /></el-icon></div>
          </div>
          <div class="ai-match-info">
            <span class="ai-match-title">{{ m.item.title }}</span>
            <span class="ai-match-cat">{{ m.item.categoryName }}</span>
            <span class="ai-match-reason">{{ m.reason }}</span>
          </div>
          <el-icon :size="14" class="ai-match-arrow"><ArrowRight /></el-icon>
        </div>
        <el-empty v-if="aiMatches.length === 0 && !aiLoading" description="暂无推荐结果" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* ══════════════════════════════════════
   Airbnb Listing Detail Layout
   ══════════════════════════════════════ */
.listing-detail {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 48px;
  align-items: start;
}

@media (max-width: 900px) {
  .listing-detail {
    grid-template-columns: 1fr;
    gap: var(--spacing-lg);
  }
}

/* ══════════════════════════════════════
   Left: Body
   ══════════════════════════════════════ */
.listing-body {
  min-width: 0;
}

/* Photo */
.listing-photo {
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: var(--spacing-lg);
}
.listing-img {
  width: 100%;
  aspect-ratio: 16 / 10;
  display: block;
}
.listing-img-fallback {
  width: 100%;
  aspect-ratio: 16 / 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--color-surface-soft);
  border-radius: var(--radius-md);
  color: var(--color-muted-soft);
  margin-bottom: var(--spacing-lg);
  gap: var(--spacing-sm);
}

/* Title (Airbnb: 22px/500) */
.listing-title {
  font-size: 22px;
  font-weight: 500;
  color: var(--color-ink);
  line-height: 1.18;
  letter-spacing: -0.44px;
  margin-bottom: var(--spacing-md);
}

/* Badges */
.listing-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.listing-badge {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-muted);
  background: var(--color-surface-soft);
  padding: 4px 12px;
  border-radius: var(--radius-tag);
}
.badge-active {
  background: #E8F5E9;
  color: #2DA844;
}
.badge-inactive {
  background: var(--color-surface-soft);
  color: var(--color-muted-soft);
}

/* Divider */
.listing-divider {
  height: 1px;
  background: var(--color-hairline);
  margin: var(--spacing-xl) 0;
}

/* Sections */
.listing-section {
  margin-bottom: var(--spacing-lg);
}
.section-heading {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: var(--spacing-sm);
}
.want-heading {
  color: var(--color-primary);
}
.section-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--color-body);
  white-space: pre-wrap;
}

/* ══════════════════════════════════════
   Owner Card (Airbnb host-card style)
   ══════════════════════════════════════ */
.owner-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: box-shadow var(--transition-normal);
}
.owner-card:hover {
  box-shadow: var(--shadow-card-hover);
}

.owner-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
}
.owner-time {
  font-size: 13px;
  color: var(--color-muted);
  margin-top: 4px;
}

/* ══════════════════════════════════════
   Right: Sticky Reservation Card
   ══════════════════════════════════════ */
.listing-rail {
  position: sticky;
  top: 100px;
}

.reservation-card {
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: var(--spacing-lg);
}

.res-status {
  text-align: center;
  margin-bottom: var(--spacing-base);
}
.res-status-badge {
  font-size: 14px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: var(--radius-tag);
}
.badge-done {
  background: #E8F5E9;
  color: #2DA844;
}
.badge-off {
  background: var(--color-surface-soft);
  color: var(--color-muted-soft);
}

/* Reserve CTA (Airbnb primary full-width) */
.reserve-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
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
  transition: background var(--transition-fast);
}
.reserve-btn:hover {
  background: var(--color-primary-dark);
}
.reserve-btn:active {
  background: var(--color-primary-active);
}
.reserve-btn.secondary {
  background: var(--color-canvas);
  color: var(--color-ink);
  border: 1px solid var(--color-ink);
}
.reserve-btn.secondary:hover {
  background: var(--color-surface-soft);
}

.res-hint {
  text-align: center;
  font-size: 13px;
  color: var(--color-muted);
  margin-top: var(--spacing-sm);
}

.res-note {
  text-align: center;
  font-size: 12px;
  color: var(--color-muted-soft);
  margin-top: var(--spacing-base);
}

/* Favorite button */
.fav-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 40px;
  background: var(--color-canvas);
  color: var(--color-muted);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-button);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition-fast);
}
.fav-btn:hover { border-color: var(--color-primary); color: var(--color-primary); }
.fav-btn.favorited { color: var(--color-primary); border-color: var(--color-primary); }

/* ══════════════════════════════════════
   Exchange Dialog
   ══════════════════════════════════════ */
.exchange-form .field-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-muted);
  margin-bottom: 6px;
}
.option-extra {
  color: var(--color-muted);
  margin-left: 8px;
  font-size: 12px;
}

/* Dialog footer buttons */
.dialog-btn {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: var(--radius-button);
  border: none;
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast);
}
.dialog-btn.cancel {
  background: var(--color-surface-soft);
  color: var(--color-ink);
}
.dialog-btn.cancel:hover {
  background: var(--color-surface-strong);
}
.dialog-btn.submit {
  background: var(--color-primary);
  color: var(--color-on-primary);
  margin-left: var(--spacing-sm);
}
.dialog-btn.submit:hover {
  background: var(--color-primary-dark);
}
.dialog-btn.submit:disabled {
  background: var(--color-primary-disabled);
  cursor: not-allowed;
}

/* AI match button */
.ai-match-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 40px;
  background: var(--color-luxe);
  color: #fff;
  border: none;
  border-radius: var(--radius-button);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: opacity var(--transition-fast);
}
.ai-match-btn:hover { opacity: 0.9; }

/* AI dialog */
.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-base);
  padding: var(--spacing-xxl) 0;
  color: var(--color-muted);
}
.ai-spin { animation: spin 1s linear infinite; color: var(--color-primary); }
@keyframes spin { to { transform: rotate(360deg); } }

.ai-intro {
  font-size: 14px;
  color: var(--color-muted);
  margin-bottom: var(--spacing-base);
}
.ai-intro strong { color: var(--color-ink); }

.ai-match-card {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-sm);
  cursor: pointer;
  transition: box-shadow var(--transition-fast);
}
.ai-match-card:hover { box-shadow: var(--shadow-card); }

.ai-match-thumb {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}
.ai-match-thumb .el-image { width: 100%; height: 100%; }
.thumb-fb-sm {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-muted-soft);
}

.ai-match-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.ai-match-title { font-size: 14px; font-weight: 600; color: var(--color-ink); }
.ai-match-cat { font-size: 12px; color: var(--color-muted); }
.ai-match-reason { font-size: 12px; color: var(--color-luxe); font-style: italic; }
.ai-match-arrow { color: var(--color-muted-soft); flex-shrink: 0; }
</style>
