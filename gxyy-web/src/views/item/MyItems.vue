<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { itemApi } from '../../api/item'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const items = ref([])
const loading = ref(false)

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

onMounted(fetchItems)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>我的物品</h2>
      <el-button type="primary" @click="goCreate">
        <el-icon><Plus /></el-icon> 发布物品
      </el-button>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && items.length === 0" description="还没有发布物品">
        <el-button type="primary" @click="goCreate">去发布</el-button>
      </el-empty>

      <el-table v-else :data="items" stripe style="width: 100%">
        <el-table-column label="物品" min-width="250">
          <template #default="{ row }">
            <div class="item-cell" @click="goDetail(row.id)">
              <el-image
                v-if="(row.thumbImages && row.thumbImages.length > 0) || (row.images && row.images.length > 0)"
                :src="(row.thumbImages && row.thumbImages[0]) || (row.images && row.images[0])"
                fit="cover"
                style="width: 64px; height: 64px; border-radius: 4px"
              >
                <template #error>
                  <div style="width:64px;height:64px;background:#f5f7fa;display:flex;align-items:center;justify-content:center">
                    <el-icon :size="24"><PictureFilled /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else style="width:64px;height:64px;background:#f5f7fa;display:flex;align-items:center;justify-content:center;border-radius:4px">
                <el-icon :size="24"><PictureFilled /></el-icon>
              </div>
              <div style="margin-left: 12px">
                <p class="item-name">{{ row.title }}</p>
                <p class="item-cat">{{ row.categoryName }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="condition" label="成色" width="100">
          <template #default="{ row }">{{ conditionMap[row.condition] }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ statusMap[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="goDetail(row.id)">查看</el-button>
              <el-button size="small" :disabled="row.status !== 'ACTIVE'" @click="router.push(`/item/${row.id}/edit`)">编辑</el-button>
              <el-button size="small" type="warning" :disabled="row.status !== 'ACTIVE'" @click="handleOffShelf(row)">下架</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.item-cell {
  display: flex;
  align-items: center;
  cursor: pointer;
}
.item-name {
  font-weight: 600;
}
.item-cat {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.action-buttons {
  display: flex;
  gap: 6px;
  flex-wrap: nowrap;
}
</style>
