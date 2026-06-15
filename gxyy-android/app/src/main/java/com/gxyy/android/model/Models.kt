package com.gxyy.android.model

import com.google.gson.annotations.SerializedName

// ======== 通用返回 ========
data class ApiResult<T>(
    val code: Int,
    val message: String,
    val data: T?
)

// ======== 用户 ========
data class User(
    val id: Long = 0,
    val username: String = "",
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val createTime: String? = null
)

data class LoginVO(
    val token: String,
    val user: User
)

// ======== 请求体 ========
data class LoginBody(val username: String, val password: String)
data class RegisterBody(
    val username: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val password: String
)
data class UpdateProfileBody(
    val bio: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

// ======== 物品 ========
data class Item(
    val id: Long = 0,
    val ownerId: Long = 0,
    val ownerName: String = "",
    val ownerAvatar: String? = null,
    val categoryId: Int = 0,
    val categoryName: String = "",
    val title: String = "",
    val condition: String = "",
    val description: String = "",
    val wantDescription: String? = null,
    val images: List<String>? = null,
    val thumbImages: List<String>? = null,
    val status: String = "",
    val createTime: String? = null
)

data class PageResult<T>(
    val records: List<T>,
    val total: Long,
    val size: Long,
    val current: Long
)

data class ItemBody(
    val title: String,
    val categoryId: Int,
    val condition: String,
    val description: String,
    val wantDescription: String? = null
)

// ======== 交换请求 ========
data class ExchangeRequest(
    val id: Long = 0,
    val fromUserId: Long = 0,
    val fromUserName: String = "",
    val fromUserAvatar: String? = null,
    val fromItemId: Long = 0,
    val fromItemTitle: String = "",
    val fromItemImages: String? = null,
    val toUserId: Long = 0,
    val toUserName: String = "",
    val toItemId: Long = 0,
    val toItemTitle: String = "",
    val toItemImages: String? = null,
    val message: String? = null,
    val status: String = "",
    val createTime: String? = null
)

data class ExchangeBody(
    val toItemId: Long,
    val fromItemId: Long,
    val message: String? = null
)

// ======== 聊天 ========
data class ChatSendBody(val exchangeId: Long, val content: String)

// ======== 通知 ========
data class Notification(
    val id: Long = 0,
    val userId: Long = 0,
    val type: String = "",
    val content: String = "",
    val relatedId: Long? = null,
    @SerializedName("isRead")
    val isRead: Any? = null,  // 后端返回 0/1 或 true/false
    val createTime: String? = null
) {
    fun isReadBoolean(): Boolean = when (isRead) {
        is Boolean -> isRead
        is Number -> isRead.toInt() == 1
        else -> false
    }
}

data class UnreadCount(val count: Long)
data class AiPolishResult(val polished: String)

// 缩略图 URL 工具
fun String.thumbUrl(): String {
    val dot = lastIndexOf('.')
    return if (dot > 0) substring(0, dot) + "_thumb" + substring(dot) else this
}

// ======== 聊天 ========
data class ChatMessage(
    val id: Long = 0,
    val exchangeId: Long = 0,
    val senderId: Long = 0,
    val senderName: String = "",
    val receiverId: Long = 0,
    val content: String = "",
    @SerializedName("isMine")
    val isMine: Boolean = false,
    val createTime: String? = null
)
