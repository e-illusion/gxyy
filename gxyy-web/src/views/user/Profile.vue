<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '../../api/user'
import { followApi } from '../../api/follow'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const profile = ref({
  username: '',
  phone: '',
  email: '',
  address: '',
  bio: '',
  avatar: '',
})

const followerCount = ref(0)
const followingCount = ref(0)

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

const avatarInput = ref(null)
const triggerUpload = () => { avatarInput.value?.click() }
const handleAvatarChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  uploadLoading.value = true
  try {
    const res = await userApi.uploadAvatar(file)
    profile.value.avatar = res.data
    userStore.setUser({ ...userStore.user, avatar: res.data })
    ElMessage.success('头像更新成功')
  } catch (e) { ElMessage.error('上传失败') }
  finally { uploadLoading.value = false }
}

const fetchCounts = async () => {
  try {
    const profileId = userStore.user?.id || profile.value.id
    if (profileId) {
      const res = await followApi.counts(profileId)
      followerCount.value = res.data.followers
      followingCount.value = res.data.following
    }
  } catch (e) { /* ignore */ }
}

// Follow list dialog
const showFollowDialog = ref(false)
const followDialogTitle = ref('')
const followList = ref([])
const openFollowers = async () => {
  const id = userStore.user?.id || profile.value.id
  if (!id) return
  followDialogTitle.value = '粉丝'
  followList.value = []
  showFollowDialog.value = true
  try { const r = await followApi.followers(id); followList.value = r.data } catch (e) { /* ignore */ }
}
const openFollowing = async () => {
  const id = userStore.user?.id || profile.value.id
  if (!id) return
  followDialogTitle.value = '关注'
  followList.value = []
  showFollowDialog.value = true
  try { const r = await followApi.following(id); followList.value = r.data } catch (e) { /* ignore */ }
}

onMounted(async () => {
  await fetchProfile()
  fetchCounts()
})
</script>

<template>
  <div class="profile-page">
    <!-- Header card -->
    <div class="profile-hero">
      <div class="avatar-area">
        <el-avatar :size="80" :src="profile.avatar" />
        <button class="upload-btn" :disabled="uploadLoading" @click="triggerUpload">
          {{ uploadLoading ? '上传中...' : '更换头像' }}
        </button>
        <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="handleAvatarChange" />
      </div>
      <div class="hero-info">
        <h1 class="hero-name">{{ profile.username }}</h1>
        <p class="hero-bio" v-if="profile.bio && !editing">{{ profile.bio }}</p>
        <div class="hero-stats">
          <span class="stat clickable" @click="openFollowers"><strong>{{ followerCount }}</strong> 粉丝</span>
          <span class="stat clickable" @click="openFollowing"><strong>{{ followingCount }}</strong> 关注</span>
        </div>
      </div>
      <button v-if="!editing" class="edit-btn" @click="startEdit">编辑资料</button>
    </div>

    <!-- Info card -->
    <div class="info-card">
      <h3 class="card-heading">{{ editing ? '编辑资料' : '个人信息' }}</h3>

      <template v-if="editing">
        <div class="edit-form">
          <div class="field">
            <label class="field-label">个人简介</label>
            <el-input v-model="editForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己..." />
          </div>
          <div class="field">
            <label class="field-label">手机号</label>
            <el-input v-model="editForm.phone" />
          </div>
          <div class="field">
            <label class="field-label">邮箱</label>
            <el-input v-model="editForm.email" />
          </div>
          <div class="field">
            <label class="field-label">地址</label>
            <el-input v-model="editForm.address" placeholder="如紫菘13栋" />
          </div>
          <div class="edit-actions">
            <button class="save-btn" :disabled="loading" @click="saveProfile">
              {{ loading ? '保存中...' : '保存' }}
            </button>
            <button class="cancel-btn" @click="cancelEdit">取消</button>
          </div>
        </div>
      </template>

      <template v-else>
        <dl class="info-list">
          <div class="info-row"><dt>用户名</dt><dd>{{ profile.username }}</dd></div>
          <div class="info-row"><dt>手机号</dt><dd>{{ profile.phone || '未填写' }}</dd></div>
          <div class="info-row"><dt>邮箱</dt><dd>{{ profile.email || '未填写' }}</dd></div>
          <div class="info-row"><dt>地址</dt><dd>{{ profile.address || '未填写' }}</dd></div>
          <div class="info-row"><dt>简介</dt><dd>{{ profile.bio || '暂无简介' }}</dd></div>
        </dl>
      </template>
    </div>
    <!-- Follow List Dialog -->
    <el-dialog v-model="showFollowDialog" :title="followDialogTitle" width="400px">
      <div v-if="followList.length === 0" class="dialog-empty">暂无数据</div>
      <div v-else class="follow-list">
        <div v-for="u in followList" :key="u.id" class="follow-row" @click="router.push(`/user/${u.id}`); showFollowDialog = false">
          <el-avatar :size="40" :src="u.avatar" />
          <div class="follow-info">
            <span class="follow-name">{{ u.username }}</span>
            <span class="follow-bio" v-if="u.bio">{{ u.bio.substring(0, 30) }}{{ u.bio.length > 30 ? '...' : '' }}</span>
          </div>
          <el-icon :size="14" class="follow-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 640px;
  margin: 0 auto;
}

/* Hero card */
.profile-hero {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
}
.avatar-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
}
.upload-btn {
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-primary);
  background: none;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-full);
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast);
}
.upload-btn:hover { background: #FFF5F7; }
.upload-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.hero-info { flex: 1; }
.hero-name {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-ink);
}
.hero-bio {
  font-size: 14px;
  color: var(--color-muted);
  margin-top: 4px;
}
.hero-stats { display: flex; gap: var(--spacing-base); margin-top: var(--spacing-sm); }
.hero-stats .stat { font-size: 13px; color: var(--color-muted); }
.hero-stats .stat strong { color: var(--color-ink); font-weight: 600; }
.stat.clickable { cursor: pointer; transition: color var(--transition-fast); }
.stat.clickable:hover { color: var(--color-primary); }

/* Follow list dialog */
.dialog-empty { text-align: center; padding: var(--spacing-xl); color: var(--color-muted-soft); }
.follow-list { display: flex; flex-direction: column; }
.follow-row {
  display: flex; align-items: center; gap: var(--spacing-md);
  padding: var(--spacing-md); border-radius: var(--radius-sm);
  cursor: pointer; transition: background var(--transition-fast);
}
.follow-row:hover { background: var(--color-surface-soft); }
.follow-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.follow-name { font-size: 14px; font-weight: 600; color: var(--color-ink); }
.follow-bio { font-size: 12px; color: var(--color-muted); margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.follow-arrow { color: var(--color-muted-soft); flex-shrink: 0; }

.edit-btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
  background: var(--color-canvas);
  border: 1px solid var(--color-ink);
  border-radius: var(--radius-button);
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast);
}
.edit-btn:hover { background: var(--color-surface-soft); }

/* Info card */
.info-card {
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
}
.card-heading {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
  margin-bottom: var(--spacing-lg);
}

/* Info list */
.info-list {
  display: flex;
  flex-direction: column;
}
.info-row {
  display: flex;
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--color-hairline-soft);
}
.info-row:last-child { border-bottom: none; }
.info-row dt {
  width: 80px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-muted);
  flex-shrink: 0;
}
.info-row dd {
  font-size: 14px;
  color: var(--color-ink);
}

/* Edit form */
.edit-form { display: flex; flex-direction: column; }
.field { margin-bottom: var(--spacing-base); }
.field-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-muted);
  margin-bottom: 6px;
}
.edit-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
}
.save-btn {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-on-primary);
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-button);
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast);
}
.save-btn:hover { background: var(--color-primary-dark); }
.save-btn:disabled { background: var(--color-primary-disabled); cursor: not-allowed; }
.cancel-btn {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink);
  background: var(--color-surface-soft);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-button);
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast);
}
.cancel-btn:hover { background: var(--color-surface-strong); }
</style>
