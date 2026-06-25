<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { itemApi } from '../../api/item'
import { aiApi } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const items = ref([])
const loading = ref(false)
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
const statusMap = {
  'ACTIVE': '在架', 'EXCHANGED': '已换出', 'OFF_SHELF': '已下架',
}

const fetchItems = async () => {
  loading.value = true
  try {
    const res = await itemApi.myItems()
    items.value = res.data
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

const goDetail = (id) => router.push(`/item/${id}`)
const goCreate = () => router.push('/item/create')

const handleDelete = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这个物品吗？', '提示', { type: 'warning' })
    await itemApi.delete(item.id)
    ElMessage.success('删除成功')
    fetchItems()
  } catch (e) { /* cancelled or error */ }
}

const handleOffShelf = async (item) => {
  try {
    await ElMessageBox.confirm('确定下架该物品吗？', '提示', { type: 'warning' })
    await itemApi.offShelf(item.id)
    ElMessage.success('已下架')
    fetchItems()
  } catch (e) { /* cancelled or error */ }
}

const getFirstImage = (item) => {
  if (item.thumbImages && item.thumbImages.length > 0) return item.thumbImages[0]
  if (item.images && item.images.length > 0) return item.images[0]
  return ''
}

// AI match state
const showAiDialog = ref(false)
const aiLoading = ref(false)
const aiMatches = ref([])
const aiTargetItem = ref(null)

const openAiMatch = async (item) => {
  aiTargetItem.value = item
  showAiDialog.value = true
  aiLoading.value = true
  aiMatches.value = []
  try {
    const res = await aiApi.match(item.id)
    aiMatches.value = res.data
  } catch (e) {
    ElMessage.error(e?.message || 'AI推荐失败')
    showAiDialog.value = false
  } finally {
    aiLoading.value = false
  }
}

onMounted(fetchItems)
</script>

<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">我的物品</h1>
      <div class="header-right">
        <el-select v-model="filterStatus" placeholder="状态" clearable size="small" style="width:100px">
          <el-option label="在架" value="ACTIVE" />
          <el-option label="已换出" value="EXCHANGED" />
          <el-option label="已下架" value="OFF_SHELF" />
        </el-select>
        <el-select v-model="filterCondition" placeholder="成色" clearable size="small" style="width:100px">
          <el-option label="全新" value="NEW" />
          <el-option label="几乎全新" value="LIKE_NEW" />
          <el-option label="轻微使用" value="SLIGHTLY_USED" />
          <el-option label="正常使用" value="NORMAL_USE" />
        </el-select>
        <button class="create-btn" @click="goCreate">
          <el-icon :size="16"><Plus /></el-icon>
          <span>发布物品</span>
        </button>
      </div>
    </div>

    <div v-loading="loading">
      <div v-if="!loading && items.length === 0" class="empty-state">
        <el-icon :size="48" color="var(--color-muted-soft)"><FolderOpened /></el-icon>
        <p>还没有发布物品</p>
        <button class="create-btn" @click="goCreate">发布第一个物品</button>
      </div>
      <div v-else-if="!loading && filteredItems.length === 0" class="empty-state">
        <p>没有符合筛选条件的物品</p>
      </div>

      <div v-else class="my-items-list">
        <article v-for="item in filteredItems" :key="item.id" class="my-item-card" @click="goDetail(item.id)">
          <div class="item-thumb">
            <el-image v-if="getFirstImage(item)" :src="getFirstImage(item)" fit="cover" class="thumb-img">
              <template #error>
                <div class="thumb-fallback"><el-icon :size="24"><PictureFilled /></el-icon></div>
              </template>
            </el-image>
            <div v-else class="thumb-fallback"><el-icon :size="24"><PictureFilled /></el-icon></div>
          </div>
          <div class="item-body">
            <h3 class="item-title">{{ item.title }}</h3>
            <div class="item-meta">
              <span class="meta-badge">{{ item.categoryName }}</span>
              <span class="meta-badge">{{ conditionMap[item.condition] }}</span>
              <span class="meta-badge" :class="item.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'">{{ statusMap[item.status] }}</span>
            </div>
            <span class="item-time">{{ item.createTime }}</span>
          </div>
          <div class="item-actions" @click.stop>
            <button class="action-btn ai" :disabled="item.status !== 'ACTIVE'" @click="openAiMatch(item)">AI推荐</button>
            <button class="action-btn" @click="goDetail(item.id)">查看</button>
            <button class="action-btn" :disabled="item.status !== 'ACTIVE'" @click="router.push('/item/' + item.id + '/edit')">编辑</button>
            <button class="action-btn warn" :disabled="item.status !== 'ACTIVE'" @click="handleOffShelf(item)">下架</button>
            <button class="action-btn danger" @click="handleDelete(item)">删除</button>
          </div>
        </article>
      </div>
    </div>

    <el-dialog v-model="showAiDialog" title="AI 推荐交换" width="520px">
      <div v-if="aiLoading" class="ai-loading">
        <el-icon :size="32" class="ai-spin"><Loading /></el-icon>
        <p>AI 正在为你匹配最佳交换对象...</p>
      </div>
      <div v-else class="ai-matches">
        <p class="ai-intro" v-if="aiTargetItem">为 <strong>{{ aiTargetItem.title }}</strong> 推荐以下交换对象：</p>
        <div v-for="(m, idx) in aiMatches" :key="idx" class="ai-match-card" @click="goDetail(m.item.id)">
          <div class="ai-match-thumb">
            <el-image v-if="(m.item.thumbImages && m.item.thumbImages.length) || (m.item.images && m.item.images.length)" :src="(m.item.thumbImages && m.item.thumbImages[0]) || (m.item.images && m.item.images[0])" fit="cover">
              <template #error><div class="thumb-fb-sm"><el-icon :size="18"><PictureFilled /></el-icon></div></template>
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
        <el-empty v-if="aiMatches.length === 0" description="暂无推荐结果" :image-size="60" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: var(--spacing-sm); margin-bottom: var(--spacing-xl); }
.page-title { font-size: 22px; font-weight: 600; color: var(--color-ink); }
.header-right { display: flex; align-items: center; gap: var(--spacing-sm); }

.create-btn { display: flex; align-items: center; gap: 6px; height: 40px; padding: 0 var(--spacing-lg); background: var(--color-primary); color: var(--color-on-primary); border: none; border-radius: var(--radius-button); font-size: 14px; font-weight: 500; cursor: pointer; font-family: inherit; transition: background var(--transition-fast); }
.create-btn:hover { background: var(--color-primary-dark); }

.empty-state { display: flex; flex-direction: column; align-items: center; gap: var(--spacing-base); padding: var(--spacing-section) 0; color: var(--color-muted-soft); }

.my-items-list { display: flex; flex-direction: column; gap: 1px; border: 1px solid var(--color-hairline); border-radius: var(--radius-md); overflow: hidden; background: var(--color-hairline); }
.my-item-card { display: flex; align-items: center; gap: var(--spacing-base); padding: var(--spacing-base); background: var(--color-canvas); cursor: pointer; transition: background var(--transition-fast); }
.my-item-card:hover { background: var(--color-surface-soft); }

.item-thumb { flex-shrink: 0; }
.thumb-img { width: 72px; height: 72px; border-radius: var(--radius-sm); display: block; }
.thumb-fallback { width: 72px; height: 72px; border-radius: var(--radius-sm); background: var(--color-surface-soft); display: flex; align-items: center; justify-content: center; color: var(--color-muted-soft); }

.item-body { flex: 1; min-width: 0; }
.item-title { font-size: 15px; font-weight: 600; color: var(--color-ink); margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-meta { display: flex; gap: 4px; margin-bottom: 4px; }
.meta-badge { font-size: 12px; font-weight: 500; color: var(--color-muted); background: var(--color-surface-soft); padding: 1px 8px; border-radius: var(--radius-tag); }
.badge-active { background: #E8F5E9; color: #2DA844; }
.badge-inactive { background: var(--color-surface-soft); color: var(--color-muted-soft); }
.item-time { font-size: 12px; color: var(--color-muted-soft); }

.item-actions { display: flex; gap: 6px; flex-shrink: 0; }
.action-btn { padding: 6px 12px; font-size: 13px; font-weight: 500; color: var(--color-ink); background: var(--color-surface-soft); border: 1px solid var(--color-hairline); border-radius: var(--radius-sm); cursor: pointer; font-family: inherit; transition: background var(--transition-fast); }
.action-btn:hover { background: var(--color-surface-strong); }
.action-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.action-btn.ai { color: var(--color-luxe); }
.action-btn.ai:hover:not(:disabled) { background: #F5EDFF; }
.action-btn.warn { color: #B85F0A; }
.action-btn.warn:hover:not(:disabled) { background: #FDF6EC; }
.action-btn.danger { color: var(--color-error-text); }
.action-btn.danger:hover:not(:disabled) { background: #FEF2F2; }

.ai-loading { display: flex; flex-direction: column; align-items: center; gap: var(--spacing-base); padding: var(--spacing-xxl) 0; color: var(--color-muted); }
.ai-spin { animation: spin 1s linear infinite; color: var(--color-primary); }
@keyframes spin { to { transform: rotate(360deg); } }
.ai-intro { font-size: 14px; color: var(--color-muted); margin-bottom: var(--spacing-base); }
.ai-intro strong { color: var(--color-ink); }
.ai-match-card { display: flex; align-items: center; gap: var(--spacing-md); padding: var(--spacing-md); border: 1px solid var(--color-hairline); border-radius: var(--radius-md); margin-bottom: var(--spacing-sm); cursor: pointer; transition: box-shadow var(--transition-fast); }
.ai-match-card:hover { box-shadow: var(--shadow-card); }
.ai-match-thumb { width: 56px; height: 56px; border-radius: var(--radius-sm); overflow: hidden; flex-shrink: 0; }
.ai-match-thumb .el-image { width: 100%; height: 100%; }
.thumb-fb-sm { width: 56px; height: 56px; border-radius: var(--radius-sm); background: var(--color-surface-soft); display: flex; align-items: center; justify-content: center; color: var(--color-muted-soft); }
.ai-match-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.ai-match-title { font-size: 14px; font-weight: 600; color: var(--color-ink); }
.ai-match-cat { font-size: 12px; color: var(--color-muted); }
.ai-match-reason { font-size: 12px; color: var(--color-luxe); font-style: italic; }
.ai-match-arrow { color: var(--color-muted-soft); flex-shrink: 0; }

@media (max-width: 744px) { .item-actions { display: none; } }
</style>
