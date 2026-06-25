<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { exchangeApi } from '../../api/exchange'
import { ElMessage } from 'element-plus'

const router = useRouter()

const requests = ref([])
const loading = ref(false)
const activeTab = ref('all')

const statusMap = {
  'PENDING': { label: '待确认', type: 'warning' },
  'ACCEPTED': { label: '已同意', type: 'success' },
  'REJECTED': { label: '已拒绝', type: 'danger' },
  'CANCELLED': { label: '已取消', type: 'info' },
}

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'PENDING', label: '待处理' },
  { key: 'ACCEPTED', label: '已同意' },
]

const fetchRequests = async () => {
  loading.value = true
  try {
    const res = await exchangeApi.list()
    requests.value = res.data
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

const filteredRequests = computed(() => {
  if (activeTab.value === 'all') return requests.value
  return requests.value.filter(r => r.status === activeTab.value)
})

const handleAccept = async (req) => {
  try {
    await exchangeApi.accept(req.id)
    ElMessage.success('已同意交换')
    fetchRequests()
  } catch (e) { /* ignore */ }
}

const handleReject = async (req) => {
  try {
    await exchangeApi.reject(req.id)
    ElMessage.success('已拒绝')
    fetchRequests()
  } catch (e) { /* ignore */ }
}

const handleCancel = async (req) => {
  try {
    await exchangeApi.cancel(req.id)
    ElMessage.success('已取消')
    fetchRequests()
  } catch (e) { /* ignore */ }
}

onMounted(fetchRequests)
</script>

<template>
  <div>
    <h1 class="page-title">交换请求</h1>

    <!-- Tab pills -->
    <div class="tab-strip">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-pill"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && requests.length === 0" description="暂无交换请求" />

      <div v-else class="request-list">
        <div v-for="req in filteredRequests" :key="req.id" class="request-card">
          <!-- Header -->
          <div class="req-header">
            <span
              class="req-status"
              :class="{
                'status-pending': req.status === 'PENDING',
                'status-done': req.status === 'ACCEPTED',
                'status-rejected': req.status === 'REJECTED',
                'status-cancelled': req.status === 'CANCELLED',
              }"
            >
              {{ statusMap[req.status]?.label || req.status }}
            </span>
            <span class="req-time">{{ req.createTime }}</span>
          </div>

          <!-- Exchange items row -->
          <div class="req-body">
            <!-- From item -->
            <div class="req-item-side">
              <div class="req-thumb" v-if="req.fromItemImages">
                <el-image :src="req.fromItemImages" fit="cover">
                  <template #error>
                    <div class="thumb-fb"><el-icon :size="24"><PictureFilled /></el-icon></div>
                  </template>
                </el-image>
              </div>
              <div v-else class="thumb-fb"><el-icon :size="24"><PictureFilled /></el-icon></div>
              <div class="req-item-info">
                <span class="req-label">对方提供</span>
                <span class="req-title">{{ req.fromItemTitle }}</span>
                <span class="req-user" @click="router.push(`/user/${req.fromUserId}`)">{{ req.fromUserName }}</span>
              </div>
            </div>

            <!-- Switch icon -->
            <div class="req-switch-icon">
              <el-icon :size="20"><Switch /></el-icon>
            </div>

            <!-- To item -->
            <div class="req-item-side">
              <div class="req-thumb" v-if="req.toItemImages">
                <el-image :src="req.toItemImages" fit="cover">
                  <template #error>
                    <div class="thumb-fb"><el-icon :size="24"><PictureFilled /></el-icon></div>
                  </template>
                </el-image>
              </div>
              <div v-else class="thumb-fb"><el-icon :size="24"><PictureFilled /></el-icon></div>
              <div class="req-item-info">
                <span class="req-label">换取</span>
                <span class="req-title">{{ req.toItemTitle }}</span>
                <span class="req-user" @click="router.push(`/user/${req.toUserId}`)">{{ req.toUserName }}</span>
              </div>
            </div>
          </div>

          <!-- Message -->
          <p class="req-message" v-if="req.message">{{ req.message }}</p>

          <!-- Actions -->
          <div class="req-actions" v-if="req.status === 'PENDING'">
            <button class="act-btn accept" @click="handleAccept(req)">同意交换</button>
            <button class="act-btn reject" @click="handleReject(req)">拒绝</button>
            <button class="act-btn cancel" @click="handleCancel(req)">取消</button>
          </div>
          <div class="req-actions" v-if="req.status === 'ACCEPTED'">
            <button class="act-btn chat" @click="router.push(`/chat/${req.id}`)">进入聊天</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: var(--spacing-lg);
}

/* Tab strip */
.tab-strip {
  display: flex;
  gap: var(--spacing-xs);
  margin-bottom: var(--spacing-xl);
  border-bottom: 1px solid var(--color-hairline);
  padding-bottom: var(--spacing-sm);
}
.tab-pill {
  padding: var(--spacing-sm) var(--spacing-base);
  font-size: 14px;
  font-weight: 500;
  color: var(--color-muted);
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  border-radius: var(--radius-sm);
  transition: color var(--transition-fast), background var(--transition-fast);
}
.tab-pill:hover { color: var(--color-ink); background: var(--color-surface-soft); }
.tab-pill.active { color: var(--color-ink); background: var(--color-surface-strong); }

/* Request card */
.request-card {
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-base);
}

.req-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}
.req-time {
  font-size: 12px;
  color: var(--color-muted-soft);
}

/* Status badge */
.req-status {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: var(--radius-tag);
}
.status-pending { background: #FDF6EC; color: #B85F0A; }
.status-done { background: #E8F5E9; color: #2DA844; }
.status-rejected { background: #FEF2F2; color: var(--color-error-text); }
.status-cancelled { background: var(--color-surface-soft); color: var(--color-muted-soft); }

/* Exchange items */
.req-body {
  display: flex;
  align-items: center;
  gap: var(--spacing-base);
  margin-bottom: var(--spacing-md);
}
.req-item-side {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex: 1;
  min-width: 0;
}
.req-thumb {
  flex-shrink: 0;
  width: 72px;
  height: 72px;
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.req-thumb .el-image {
  width: 100%;
  height: 100%;
}
.thumb-fb {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-sm);
  background: var(--color-surface-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-muted-soft);
  flex-shrink: 0;
}
.req-item-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.req-label {
  font-size: 11px;
  color: var(--color-muted-soft);
}
.req-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.req-user {
  font-size: 12px;
  color: var(--color-muted);
  cursor: pointer;
  margin-top: 2px;
}
.req-user:hover { color: var(--color-primary); }

.req-switch-icon {
  color: var(--color-primary);
  flex-shrink: 0;
}

.req-message {
  font-size: 13px;
  color: var(--color-body);
  padding: var(--spacing-sm) 0;
  border-top: 1px solid var(--color-hairline);
  margin-bottom: 0;
}

/* Action buttons */
.req-actions {
  display: flex;
  gap: var(--spacing-sm);
  justify-content: flex-end;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-hairline);
}
.act-btn {
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 500;
  border: none;
  border-radius: var(--radius-button);
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast), opacity var(--transition-fast);
}
.act-btn.accept { background: #2DA844; color: #fff; }
.act-btn.accept:hover { opacity: 0.9; }
.act-btn.reject { background: var(--color-error-text); color: #fff; }
.act-btn.reject:hover { opacity: 0.9; }
.act-btn.cancel { background: var(--color-surface-soft); color: var(--color-ink); border: 1px solid var(--color-hairline); }
.act-btn.cancel:hover { background: var(--color-surface-strong); }
.act-btn.chat { background: var(--color-primary); color: #fff; }
.act-btn.chat:hover { background: var(--color-primary-dark); }
</style>
