<script setup>
import { ref, onMounted } from 'vue'
import { notificationApi } from '../../api/notification'

const notifications = ref([])
const loading = ref(false)

const typeMap = {
  'EXCHANGE_REQUEST': { icon: 'Bell', color: '#409eff', label: '交换请求' },
  'EXCHANGE_ACCEPTED': { icon: 'CircleCheck', color: '#67c23a', label: '请求通过' },
  'EXCHANGE_REJECTED': { icon: 'CircleClose', color: '#f56c6c', label: '请求拒绝' },
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await notificationApi.list()
    notifications.value = res.data
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

const handleMarkRead = async (notif) => {
  if (notif.isRead) return
  try {
    await notificationApi.markRead(notif.id)
    notif.isRead = 1
  } catch (e) { /* ignore */ }
}

onMounted(fetchNotifications)
</script>

<template>
  <div>
    <h2 style="margin-bottom: 20px">消息通知</h2>

    <div v-loading="loading">
      <el-empty v-if="!loading && notifications.length === 0" description="暂无通知" />

      <div v-else class="notif-list">
        <div
          v-for="notif in notifications"
          :key="notif.id"
          class="notif-item"
          :class="{ unread: !notif.isRead }"
          @click="handleMarkRead(notif)"
        >
          <el-icon :size="24" :color="typeMap[notif.type]?.color || '#999'">
            <component :is="typeMap[notif.type]?.icon || 'InfoFilled'" />
          </el-icon>
          <div class="notif-body">
            <p class="notif-content">{{ notif.content }}</p>
            <div class="notif-footer">
              <el-tag size="small">{{ typeMap[notif.type]?.label || notif.type }}</el-tag>
              <span class="notif-time">{{ notif.createTime }}</span>
            </div>
          </div>
          <div v-if="!notif.isRead" class="unread-dot" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  cursor: pointer;
  transition: background 0.2s;
  position: relative;
}
.notif-item:hover {
  background: #f5f7fa;
}
.notif-item.unread {
  background: #ecf5ff;
}
.notif-body {
  flex: 1;
}
.notif-content {
  font-size: 14px;
  line-height: 1.6;
}
.notif-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}
.notif-time {
  font-size: 12px;
  color: #999;
}
.unread-dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 8px;
}
</style>
