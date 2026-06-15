<script setup>
import { ref, onMounted } from 'vue'
import { userApi } from '../../api/user'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const profile = ref({
  username: '',
  phone: '',
  email: '',
  address: '',
  bio: '',
  avatar: '',
})

const editing = ref(false)
const editForm = ref({ bio: '', phone: '', email: '', address: '' })
const loading = ref(false)
const uploadLoading = ref(false)

const fetchProfile = async () => {
  try {
    const res = await userApi.getProfile()
    profile.value = res.data
    editForm.value = {
      bio: res.data.bio || '',
      phone: res.data.phone || '',
      email: res.data.email || '',
      address: res.data.address || '',
    }
  } catch (e) { /* ignore */ }
}

const startEdit = () => { editing.value = true }

const saveProfile = async () => {
  loading.value = true
  try {
    const res = await userApi.updateProfile(editForm.value)
    profile.value = res.data
    userStore.setUser(res.data)
    editing.value = false
    ElMessage.success('保存成功')
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
}

const cancelEdit = () => {
  editing.value = false
  editForm.value = {
    bio: profile.value.bio || '',
    phone: profile.value.phone || '',
    email: profile.value.email || '',
    address: profile.value.address || '',
  }
}

// 头像上传
const avatarInput = ref(null)
const triggerUpload = () => {
  avatarInput.value?.click()
}
const handleAvatarChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  uploadLoading.value = true
  try {
    const res = await userApi.uploadAvatar(file)
    profile.value.avatar = res.data
    userStore.setUser({ ...userStore.user, avatar: res.data })
    ElMessage.success('头像更新成功')
  } catch (e) {
    ElMessage.error('上传失败')
  }
  finally { uploadLoading.value = false }
}

onMounted(fetchProfile)
</script>

<template>
  <div class="profile-container">
    <el-card>
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="80" :src="profile.avatar" />
          <el-button
            size="small"
            :loading="uploadLoading"
            style="margin-top: 12px"
            @click="triggerUpload"
          >
            更换头像
          </el-button>
          <input
            ref="avatarInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleAvatarChange"
          />
        </div>
        <div class="user-info">
          <h2>{{ profile.username }}</h2>
          <p class="user-bio" v-if="profile.bio && !editing">{{ profile.bio || '这个人很懒，什么都没写...' }}</p>
        </div>
        <el-button v-if="!editing" @click="startEdit">编辑资料</el-button>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header><h3>{{ editing ? '编辑资料' : '个人信息' }}</h3></template>

      <template v-if="editing">
        <el-form label-position="top" style="max-width: 400px">
          <el-form-item label="个人简介">
            <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己..." />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="editForm.email" />
          </el-form-item>
          <el-form-item label="地址">
            <el-input v-model="editForm.address" placeholder="如紫菘13栋" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="saveProfile">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </el-form-item>
        </el-form>
      </template>

      <template v-else>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ profile.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ profile.email || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ profile.address || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="简介">{{ profile.bio || '暂无简介' }}</el-descriptions-item>
        </el-descriptions>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.profile-container {
  max-width: 640px;
  margin: 0 auto;
}
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.user-info {
  flex: 1;
}
.user-bio {
  color: #666;
  margin-top: 8px;
}
</style>
