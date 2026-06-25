<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../api/index'

const route = useRoute()
const router = useRouter()

const exchangeId = ref(Number(route.params.id))
const messages = ref([])
const newMsg = ref('')
const sending = ref(false)
const loading = ref(true)
const chatBox = ref(null)

const fetchMessages = async () => {
  try {
    const res = await api.get(`/api/chat/${exchangeId.value}`)
    messages.value = res.data
    await nextTick()
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!newMsg.value.trim()) return
  sending.value = true
  try {
    const res = await api.post('/api/chat', {
      exchangeId: exchangeId.value,
      content: newMsg.value,
    })
    messages.value.push(res.data)
    newMsg.value = ''
    await nextTick()
    scrollToBottom()
  } catch (e) {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (chatBox.value) {
    chatBox.value.scrollTop = chatBox.value.scrollHeight
  }
}

onMounted(fetchMessages)

// Auto-refresh every 3 seconds
const timer = setInterval(fetchMessages, 3000)
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="chat-container">
    <!-- Header -->
    <div class="chat-header">
      <button class="back-btn" @click="router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <span class="chat-title">交换聊天</span>
    </div>

    <!-- Messages -->
    <div v-loading="loading" class="chat-messages" ref="chatBox">
      <div v-if="messages.length === 0 && !loading" class="empty-chat">
        <p>暂无消息</p>
        <span>开始聊天确定交换时间和地点吧</span>
      </div>
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['msg-item', msg.isMine ? 'msg-mine' : 'msg-other']"
      >
        <span class="msg-sender" v-if="!msg.isMine">{{ msg.senderName }}</span>
        <div class="msg-bubble">{{ msg.content }}</div>
        <span class="msg-time">{{ msg.createTime?.substring(11, 16) }}</span>
      </div>
    </div>

    <!-- Input -->
    <div class="chat-input-bar">
      <input
        v-model="newMsg"
        class="chat-text-input"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
        :disabled="sending"
      />
      <button class="send-btn" :disabled="sending || !newMsg.trim()" @click="sendMessage">
        <el-icon :size="18" v-if="!sending"><Promotion /></el-icon>
        <span v-else>...</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  max-width: 640px;
  margin: 0 auto;
}

/* Header */
.chat-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--color-hairline);
}
.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
  background: var(--color-surface-soft);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast);
}
.back-btn:hover { background: var(--color-surface-strong); }
.chat-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-ink);
}

/* Messages area */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-base) 0;
}
.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xs);
  margin-top: 60px;
  color: var(--color-muted-soft);
  font-size: 14px;
}

/* Message items */
.msg-item {
  display: flex;
  flex-direction: column;
  margin-bottom: var(--spacing-base);
}
.msg-mine { align-items: flex-end; }
.msg-other { align-items: flex-start; }

.msg-sender {
  font-size: 12px;
  color: var(--color-muted);
  margin-bottom: 4px;
  padding: 0 4px;
}

.msg-bubble {
  max-width: 70%;
  padding: 10px 16px;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
}
.msg-mine .msg-bubble {
  background: var(--color-primary);
  color: var(--color-on-primary);
  border-bottom-right-radius: 4px;
}
.msg-other .msg-bubble {
  background: var(--color-surface-soft);
  color: var(--color-ink);
  border-bottom-left-radius: 4px;
}

.msg-time {
  font-size: 11px;
  color: var(--color-muted-soft);
  margin-top: 4px;
  padding: 0 4px;
}

/* Input bar */
.chat-input-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) 0;
  border-top: 1px solid var(--color-hairline);
}
.chat-text-input {
  flex: 1;
  height: 44px;
  padding: 0 var(--spacing-base);
  font-size: 14px;
  color: var(--color-ink);
  background: var(--color-surface-soft);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-full);
  outline: none;
  font-family: inherit;
  transition: border-color var(--transition-fast);
}
.chat-text-input:focus {
  border-color: var(--color-ink);
}
.chat-text-input::placeholder {
  color: var(--color-muted-soft);
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  flex-shrink: 0;
  transition: background var(--transition-fast);
}
.send-btn:hover { background: var(--color-primary-dark); }
.send-btn:disabled { background: var(--color-primary-disabled); cursor: not-allowed; }
</style>
