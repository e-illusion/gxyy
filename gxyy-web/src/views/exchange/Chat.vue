<script setup>
import { ref, onMounted, nextTick } from 'vue'
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
import { onUnmounted } from 'vue'
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="chat-container">
    <div class="chat-header">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <span class="chat-title">交换聊天</span>
    </div>

    <div v-loading="loading" class="chat-messages" ref="chatBox">
      <div v-if="messages.length === 0 && !loading" class="empty-chat">
        <p>暂无消息，开始聊天确定交换时间和地点吧</p>
      </div>
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message-item', msg.isMine ? 'mine' : 'other']"
      >
        <div class="message-sender" v-if="!msg.isMine">{{ msg.senderName }}</div>
        <div class="message-bubble">{{ msg.content }}</div>
        <div class="message-time">{{ msg.createTime?.substring(11, 16) }}</div>
      </div>
    </div>

    <div class="chat-input">
      <el-input
        v-model="newMsg"
        placeholder="输入消息..."
        @keyup.enter="sendMessage"
        :disabled="sending"
      >
        <template #append>
          <el-button :loading="sending" @click="sendMessage">发送</el-button>
        </template>
      </el-input>
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
.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}
.chat-title {
  font-size: 18px;
  font-weight: 600;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 0;
}
.empty-chat {
  text-align: center;
  color: #999;
  margin-top: 60px;
}
.message-item {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
}
.message-item.mine {
  align-items: flex-end;
}
.message-item.other {
  align-items: flex-start;
}
.message-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
}
.mine .message-bubble {
  background-color: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.other .message-bubble {
  background-color: #f0f0f0;
  color: #333;
  border-bottom-left-radius: 4px;
}
.message-time {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}
.chat-input {
  padding: 12px 0;
  border-top: 1px solid #eee;
}
</style>
