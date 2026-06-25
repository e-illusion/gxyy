<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '../../api/user'
import { itemApi } from '../../api/item'
import { followApi } from '../../api/follow'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const user = ref(null)
const items = ref([])
const loading = ref(true)
const isFollowing = ref(false)
const followLoading = ref(false)
const followerCount = ref(0)
const followingCount = ref(0)

onMounted(async () => {
  try {
    const targetId = Number(route.params.id)
    const [userRes, itemsRes] = await Promise.all([
      userApi.getUserById(targetId),
      itemApi.list({ ownerId: targetId, size: 50, sortOrder: 'newest' })
    ])
    user.value = userRes.data
    items.value = itemsRes.data.records || []

    // 关注状态和数量
    if (userStore.isLoggedIn) {
      try {
        const [checkRes, cntRes] = await Promise.all([
          followApi.check(targetId),
          followApi.counts(targetId)
        ])
        isFollowing.value = checkRes.data.isFollowing
        followerCount.value = cntRes.data.followers
        followingCount.value = cntRes.data.following
      } catch (e) { /* ignore */ }
    } else {
      try {
        const cntRes = await followApi.counts(targetId)
        followerCount.value = cntRes.data.followers
        followingCount.value = cntRes.data.following
      } catch (e) { /* ignore */ }
    }
  } catch (e) { /* ignore */ }
  finally { loading.value = false }
})

const toggleFollow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  followLoading.value = true
  try {
    if (isFollowing.value) {
      await followApi.unfollow(user.value.id)
      isFollowing.value = false
      followerCount.value = Math.max(0, followerCount.value - 1)
    } else {
      await followApi.follow(user.value.id)
      isFollowing.value = true
      followerCount.value++
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    followLoading.value = false
  }
}

// Follow list dialog
const showFollowDialog = ref(false)
const followDialogTitle = ref('')
const followList = ref([])

const openFollowers = async () => {
  followDialogTitle.value = '粉丝'
  followList.value = []
  showFollowDialog.value = true
  try {
    const res = await followApi.followers(user.value.id)
    followList.value = res.data
  } catch (e) { /* ignore */ }
}

const openFollowing = async () => {
  followDialogTitle.value = '关注'
  followList.value = []
  showFollowDialog.value = true
  try {
    const res = await followApi.following(user.value.id)
    followList.value = res.data
  } catch (e) { /* ignore */ }
}

const goDetail = (id) => router.push(`/item/${id}`)
const isSelf = () => userStore.user?.id === user.value?.id
const goUserProfile = (id) => router.push(`/user/${id}`)
</script>

<template>
  <div class="profile-page" v-loading="loading">
    <template v-if="user">
      <!-- Hero -->
      <div class="profile-hero">
        <el-avatar :size="80" :src="user.avatarThumb || user.avatar" />
        <div class="hero-info">
          <h1 class="hero-name">{{ user.username }}</h1>
          <p class="hero-bio" v-if="user.bio">{{ user.bio }}</p>
          <div class="hero-stats">
            <span class="stat clickable" @click="openFollowers">
              <strong>{{ followerCount }}</strong> 粉丝
            </span>
            <span class="stat clickable" @click="openFollowing">
              <strong>{{ followingCount }}</strong> 关注
            </span>
          </div>
        </div>
        <div class="hero-actions">
          <button class="back-btn" @click="router.back()">
            <el-icon :size="16"><ArrowLeft /></el-icon>
            <span>返回</span>
          </button>
          <button
            v-if="!isSelf()"
            class="follow-btn"
            :class="{ following: isFollowing }"
            :disabled="followLoading"
            @click="toggleFollow"
          >
            {{ followLoading ? '...' : isFollowing ? '已关注' : '关注' }}
          </button>
        </div>
      </div>

      <!-- Contact card -->
      <div class="info-card">
        <h3 class="card-heading">联系方式</h3>
        <dl class="info-list">
          <div class="info-row"><dt>手机号</dt><dd>{{ user.phone || '未填写' }}</dd></div>
          <div class="info-row"><dt>邮箱</dt><dd>{{ user.email || '未填写' }}</dd></div>
          <div class="info-row"><dt>地址</dt><dd>{{ user.address || '未填写' }}</dd></div>
        </dl>
      </div>

      <!-- Items card -->
      <div class="info-card" v-if="items.length > 0">
        <h3 class="card-heading">TA 的在架物品</h3>
        <div class="user-items">
          <div v-for="item in items" :key="item.id" class="user-item-card" @click="goDetail(item.id)">
            <div class="item-thumb">
              <el-image
                v-if="(item.thumbImages && item.thumbImages.length) || (item.images && item.images.length)"
                :src="(item.thumbImages && item.thumbImages[0]) || (item.images && item.images[0])"
                fit="cover"
              >
                <template #error>
                  <div class="thumb-fb"><el-icon :size="20"><PictureFilled /></el-icon></div>
                </template>
              </el-image>
              <div v-else class="thumb-fb"><el-icon :size="20"><PictureFilled /></el-icon></div>
            </div>
            <div class="item-info">
              <span class="item-title">{{ item.title }}</span>
              <span class="item-cat">{{ item.categoryName }}</span>
            </div>
          </div>
        </div>
      </div>
    </template>

    <el-empty v-else description="用户不存在" />

    <!-- Follow List Dialog -->
    <el-dialog v-model="showFollowDialog" :title="followDialogTitle" width="400px">
      <div v-if="followList.length === 0" class="dialog-empty">暂无数据</div>
      <div v-else class="follow-list">
        <div v-for="u in followList" :key="u.id" class="follow-row" @click="goUserProfile(u.id); showFollowDialog = false">
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
.profile-page { max-width: 640px; margin: 0 auto; }

.profile-hero {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-lg);
  padding: var(--spacing-xl);
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-lg);
}
.hero-info { flex: 1; }
.hero-name { font-size: 22px; font-weight: 600; color: var(--color-ink); margin: 0; }
.hero-bio { font-size: 14px; color: var(--color-muted); margin-top: 4px; }

/* Stats row */
.hero-stats { display: flex; gap: var(--spacing-base); margin-top: var(--spacing-sm); }
.hero-stats .stat { font-size: 13px; color: var(--color-muted); cursor: pointer; }
.hero-stats .stat strong { color: var(--color-ink); font-weight: 600; }
.hero-stats .stat:hover { color: var(--color-ink); }

.hero-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--spacing-sm);
  flex-shrink: 0;
}

/* Back button */
.back-btn {
  display: flex; align-items: center; gap: 4px;
  padding: 8px 14px; font-size: 13px; font-weight: 500;
  color: var(--color-ink); background: var(--color-surface-soft);
  border: none; border-radius: var(--radius-full);
  cursor: pointer; font-family: inherit;
  transition: background var(--transition-fast);
}
.back-btn:hover { background: var(--color-surface-strong); }

/* Follow button - Airbnb primary style */
.follow-btn {
  padding: 8px 20px; font-size: 14px; font-weight: 500;
  color: var(--color-on-primary); background: var(--color-primary);
  border: none; border-radius: var(--radius-button);
  cursor: pointer; font-family: inherit;
  transition: all var(--transition-fast);
}
.follow-btn:hover { background: var(--color-primary-dark); }
.follow-btn.following {
  background: var(--color-canvas);
  color: var(--color-ink);
  border: 1px solid var(--color-ink);
}
.follow-btn.following:hover { background: var(--color-surface-soft); }
.follow-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* Info card */
.info-card {
  background: var(--color-canvas);
  border: 1px solid var(--color-hairline);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl); margin-bottom: var(--spacing-lg);
}
.card-heading { font-size: 16px; font-weight: 600; color: var(--color-ink); margin-bottom: var(--spacing-base); }
.info-list { display: flex; flex-direction: column; }
.info-row { display: flex; padding: var(--spacing-sm) 0; }
.info-row dt { width: 64px; font-size: 14px; font-weight: 500; color: var(--color-muted); flex-shrink: 0; }
.info-row dd { font-size: 14px; color: var(--color-ink); }

.user-items { display: flex; flex-direction: column; gap: var(--spacing-sm); }
.user-item-card {
  display: flex; align-items: center; gap: var(--spacing-md);
  cursor: pointer; padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}
.user-item-card:hover { background: var(--color-surface-soft); }
.item-thumb { width: 64px; height: 64px; border-radius: var(--radius-sm); overflow: hidden; flex-shrink: 0; }
.item-thumb .el-image { width: 100%; height: 100%; }
.thumb-fb { width: 64px; height: 64px; border-radius: var(--radius-sm); background: var(--color-surface-soft); display: flex; align-items: center; justify-content: center; color: var(--color-muted-soft); }
.item-title { font-size: 14px; font-weight: 600; color: var(--color-ink); display: block; }
.item-cat { font-size: 12px; color: var(--color-muted); margin-top: 2px; }
.info-card:last-child { margin-bottom: 0; }

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
</style>
