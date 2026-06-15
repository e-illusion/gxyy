<script setup>
import { ref, onMounted } from 'vue'
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

const fetchRequests = async () => {
  loading.value = true
  try {
    const res = await exchangeApi.list()
    requests.value = res.data
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

const filteredRequests = () => {
  if (activeTab.value === 'all') return requests.value
  if (activeTab.value === 'received') return requests.value.filter(r => r.status !== 'PENDING' || r.toUserId)
  if (activeTab.value === 'sent') return requests.value.filter(r => r.status === 'PENDING')
  return requests.value.filter(r => r.status === activeTab.value.toUpperCase())
}

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
    <h2 style="margin-bottom: 20px">交换请求</h2>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待处理" name="PENDING" />
      <el-tab-pane label="已同意" name="ACCEPTED" />
    </el-tabs>

    <div v-loading="loading">
      <el-empty v-if="!loading && requests.length === 0" description="暂无交换请求" />

      <div v-else class="request-list">
        <el-card
          v-for="req in filteredRequests()"
          :key="req.id"
          class="request-card"
        >
          <div class="req-header">
            <el-tag :type="statusMap[req.status]?.type || 'info'">
              {{ statusMap[req.status]?.label || req.status }}
            </el-tag>
            <span class="req-time">{{ req.createTime }}</span>
          </div>

          <div class="req-items">
            <div class="req-item">
              <el-image
                v-if="req.fromItemImages"
                :src="req.fromItemImages"
                fit="cover"
                style="width: 80px; height: 80px; border-radius: 4px"
              >
                <template #error>
                  <div style="width:80px;height:80px;background:#f5f7fa;display:flex;align-items:center;justify-content:center;border-radius:4px">
                    <el-icon :size="32"><PictureFilled /></el-icon>
                  </div>
                </template>
              </el-image>
              <div style="margin-left: 12px">
                <p class="req-label">对方提供</p>
                <p class="req-title">{{ req.fromItemTitle }}</p>
                <p class="req-user clickable" @click="router.push(`/user/${req.fromUserId}`)">来自 {{ req.fromUserName }}</p>
              </div>
            </div>

            <div style="text-align: center; padding: 0 16px">
              <el-icon :size="24" color="#409eff"><Switch /></el-icon>
            </div>

            <div class="req-item">
              <el-image
                v-if="req.toItemImages"
                :src="req.toItemImages"
                fit="cover"
                style="width: 80px; height: 80px; border-radius: 4px"
              >
                <template #error>
                  <div style="width:80px;height:80px;background:#f5f7fa;display:flex;align-items:center;justify-content:center;border-radius:4px">
                    <el-icon :size="32"><PictureFilled /></el-icon>
                  </div>
                </template>
              </el-image>
              <div style="margin-left: 12px">
                <p class="req-label">换取</p>
                <p class="req-title">{{ req.toItemTitle }}</p>
                <p class="req-user clickable" @click="router.push(`/user/${req.toUserId}`)">来自 {{ req.toUserName }}</p>
              </div>
            </div>
          </div>

          <p class="req-message" v-if="req.message">💬 {{ req.message }}</p>

          <div class="req-actions" v-if="req.status === 'PENDING'">
            <el-button type="success" @click="handleAccept(req)">同意交换</el-button>
            <el-button type="danger" @click="handleReject(req)">拒绝</el-button>
            <el-button @click="handleCancel(req)">取消请求</el-button>
          </div>
          <div class="req-actions" v-if="req.status === 'ACCEPTED'">
            <el-button type="primary" @click="router.push(`/chat/${req.id}`)">💬 聊天</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.request-card {
  margin-bottom: 16px;
}
.req-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.req-time {
  font-size: 12px;
  color: #999;
}
.req-items {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.req-item {
  display: flex;
  align-items: center;
  flex: 1;
}
.req-label {
  font-size: 11px;
  color: #999;
}
.req-title {
  font-size: 15px;
  font-weight: 600;
}
.req-user {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.req-message {
  color: #666;
  font-size: 13px;
  padding: 8px 0;
  border-top: 1px solid #ebeef5;
}
.req-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}
.clickable {
  cursor: pointer;
}
.clickable:hover {
  color: #409eff;
}
</style>
