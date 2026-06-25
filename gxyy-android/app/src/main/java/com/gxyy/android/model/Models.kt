package com.gxyy.android.model

import com.google.gson.annotations.SerializedName

// ======== 通用 ========
data class ApiResult<T>(val code: Int, val message: String, val data: T?)

// ======== 用户 ========
data class User(
    val id: Long = 0, val username: String = "", val phone: String? = null,
    val email: String? = null, val address: String? = null, val avatar: String? = null,
    val bio: String? = null, val createTime: String? = null
)
data class LoginVO(val token: String, val user: User)

data class LoginBody(val username: String, val password: String)
data class RegisterBody(val username: String, val phone: String, val email: String = "", val address: String = "", val password: String)
data class UpdateProfileBody(val bio: String? = null, val phone: String? = null, val email: String? = null, val address: String? = null)

// ======== 物品 ========
data class Item(
    val id: Long = 0, val ownerId: Long = 0, val ownerName: String = "", val ownerAvatar: String? = null,
    val categoryId: Int = 0, val categoryName: String = "", val title: String = "",
    val condition: String = "", val description: String = "", val wantDescription: String? = null,
    val images: List<String>? = null, val thumbImages: List<String>? = null,
    val status: String = "", val createTime: String? = null
)
data class PageResult<T>(val records: List<T>, val total: Long, val size: Long, val current: Long)
data class ItemBody(val title: String, val categoryId: Int, val condition: String, val description: String, val wantDescription: String? = null)

// ======== 收藏 ========
data class FavoriteCheckResult(val isFavorited: Boolean)

// ======== 关注 ========
data class FollowCheckResult(val isFollowing: Boolean)
data class FollowCounts(val followers: Long, val following: Long)

// ======== AI ========
data class AiPolishResult(val polished: String)
data class AiMatchItem(val item: Item, val reason: String)

// ======== 交换请求 ========
data class ExchangeRequest(
    val id: Long = 0, val fromUserId: Long = 0, val fromUserName: String = "", val fromUserAvatar: String? = null,
    val fromItemId: Long = 0, val fromItemTitle: String = "", val fromItemImages: String? = null,
    val toUserId: Long = 0, val toUserName: String = "", val toItemId: Long = 0,
    val toItemTitle: String = "", val toItemImages: String? = null,
    val message: String? = null, val status: String = "", val createTime: String? = null
)
data class ExchangeBody(val toItemId: Long, val fromItemId: Long, val message: String? = null)

// ======== 聊天 ========
data class ChatSendBody(val exchangeId: Long, val content: String)
data class ChatMessage(
    val id: Long = 0, val exchangeId: Long = 0, val senderId: Long = 0, val senderName: String = "",
    val receiverId: Long = 0, val content: String = "",
    @SerializedName("isMine") val isMine: Boolean = false, val createTime: String? = null
)

// ======== 通知 ========
data class Notification(
    val id: Long = 0, val userId: Long = 0, val type: String = "", val content: String = "",
    val relatedId: Long? = null,
    @SerializedName("isRead") val isRead: Any? = null, val createTime: String? = null
) {
    fun isReadBoolean() = when (isRead) { is Boolean -> isRead; is Number -> isRead.toInt() == 1; else -> false }
}
data class UnreadCount(val count: Long)

// ======== 工具 ========
fun String.thumbUrl(): String { val d = lastIndexOf('.'); return if (d > 0) substring(0, d) + "_thumb" + substring(d) else this }
fun String.errorMsg(): String = try { com.google.gson.Gson().fromJson(this, ApiResult::class.java)?.message ?: this } catch (e: Exception) { this }

/** Prepend BASE_URL to relative image paths. Pass-through absolute URLs unchanged. */
fun String.toImageUrl(): String = when {
    startsWith("http") -> this
    startsWith("/") -> "${com.gxyy.android.BuildConfig.BASE_URL.trimEnd('/')}$this"
    else -> this
}
/** Non-null image URL helper */
fun String?.orImageUrl(): String = this?.toImageUrl() ?: ""

// ======== 条件常量 ========
val CONDITION_LABELS = mapOf("NEW" to "全新", "LIKE_NEW" to "几乎全新", "SLIGHTLY_USED" to "轻微使用", "NORMAL_USE" to "正常使用")
val STATUS_LABELS = mapOf("ACTIVE" to "在架", "EXCHANGED" to "已换出", "OFF_SHELF" to "已下架")
val CATEGORIES = listOf(
    null to "全部", 1 to "数码电子", 2 to "书籍教材", 3 to "生活用品", 4 to "服饰鞋包",
    5 to "运动器材", 6 to "美妆护肤", 7 to "玩具乐器", 8 to "其他"
)
