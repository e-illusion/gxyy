<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '../../api/user'
import { itemApi } from '../../api/item'

const route = useRoute()
const router = useRouter()

const user = ref(null)
const items = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const [userRes, itemsRes] = await Promise.all([
      userApi.getUserById(route.params.id),
      itemApi.list({ page: 1, size: 50 })
    ])
    user.value = userRes.data
    // 过滤该用户的在架物品
    items.value = (itemsRes.data.records || []).filter(
      i => i.ownerId === Number(route.params.id)
    )
  } catch (e) {
    /* ignore */
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="profile-container" v-loading="loading">
    <template v-if="user">
      <el-card>
        <div class="user-header">
          <el-avatar :size="80" :src="user.avatarThumb || user.avatar" />
          <div class="user-meta">
            <h2>{{ user.username }}</h2>
            <p class="user-bio" v-if="user.bio">{{ user.bio }}</p>
            <el-button text @click="router.back()" style="margin-top:8px">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
          </div>
        </div>
      </el-card>

      <el-card style="margin-top: 20px">
        <template #header><h3>联系方式</h3></template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="手机号">
            {{ user.phone || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ user.email || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">
            {{ user.address || '未填写' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card style="margin-top: 20px" v-if="items.length > 0">
        <template #header><h3>TA 的在架物品</h3></template>
        <div class="user-items">
          <div
            v-for="item in items"
            :key="item.id"
            class="user-item-card"
            @click="router.push(`/item/${item.id}`)"
          >
            <el-image
              v-if="(item.thumbImages && item.thumbImages.length) || (item.images && item.images.length)"
              :src="(item.thumbImages && item.thumbImages[0]) || (item.images && item.images[0])"
              fit="cover"
              style="width:80px;height:80px;border-radius:4px"
            />
            <div style="margin-left:12px">
              <p class="item-title">{{ item.title }}</p>
              <p class="item-cat">{{ item.categoryName }}</p>
            </div>
          </div>
        </div>
      </el-card>
    </template>

    <el-empty v-else description="用户不存在" />
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 640px;
  margin: 0 auto;
}
.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
}
.user-meta h2 {
  margin: 0;
}
.user-bio {
  color: #666;
  margin-top: 4px;
}
.user-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.user-item-card {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background 0.2s;
}
.user-item-card:hover {
  background: #f5f7fa;
}
.item-title {
  font-weight: 600;
}
.item-cat {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
