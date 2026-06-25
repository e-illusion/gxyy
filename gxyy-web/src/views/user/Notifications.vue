<script setup>
import { ref, onMounted } from 'vue'
import { notificationApi } from '../../api/notification'

const notifications = ref([])
const loading = ref(false)

const typeConfig = {
  'EXCHANGE_REQUEST': { label: '交换请求', color: 'var(--color-primary)' },
  'EXCHANGE_ACCEPTED': { label: '请求通过', color: '#2DA844' },
  'EXCHANGE_REJECTED': { label: '请求拒绝', color: 'var(--color-error-text)' },
  'NEW_ITEM': { label: '新物品', color: 'var(--color-luxe)' },
}

const getTypeConfig = (type) => typeConfig[type] || { label: type || '通知', color: 'var(--color-muted)' }

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
    <h1 class="page-title">消息通知</h1>

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
          <div class="notif-dot-wrapper">
            <span class="notif-dot" :style="{ background: getTypeConfig(notif.type).color }" />
            <span v-if="!notif.isRead" class="unread-ring" />
          </div>
          <div class="notif-body">
            <p class="notif-content">{{ notif.content }}</p>
            <div class="notif-footer">
              <span
                class="notif-type-badge"
                :style="{ color: getTypeConfig(notif.type).color, background: getTypeConfig(notif.type).color + '18' }"
              >
                {{ getTypeConfig(notif.type).label }}
              </span>
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
.page-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: var(--spacing-lg);
}

/* Notification list */
.notif-list {
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--color-hairline);
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
  padding: var(--spacing-base) var(--spacing-lg);
  background: var(--color-canvas);
  cursor: pointer;
  transition: background var(--transition-fast);
  position: relative;
}
.notif-item:hover { background: var(--color-surface-soft); }
.notif-item.unread { background: #FFFAFB; }

/* Type dot */
.notif-dot-wrapper {
  position: relative;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}
.notif-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.unread-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: var(--color-primary);
  opacity: 0.08;
}

/* Body */
.notif-body { flex: 1; min-width: 0; }
.notif-content {
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-ink);
}
.notif-footer {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-sm);
}
.notif-type-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: var(--radius-tag);
}
.notif-time {
  font-size: 12px;
  color: var(--color-muted-soft);
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 10px;
}
</style>
