package com.gxyy.android.data

import com.gxyy.android.BuildConfig
import com.gxyy.android.model.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiService {

    // ======== 认证 ========
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterBody): ApiResult<User>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginBody): ApiResult<LoginVO>

    // ======== 用户 ========
    @GET("api/users/me")
    suspend fun getProfile(): ApiResult<User>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body body: UpdateProfileBody): ApiResult<User>

    @Multipart
    @POST("api/users/me/avatar")
    suspend fun uploadAvatar(@Part file: okhttp3.MultipartBody.Part): ApiResult<String>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): ApiResult<User>

    // ======== 物品 ========
    @GET("api/items")
    suspend fun getItems(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10,
        @Query("keyword") keyword: String? = null,
        @Query("categoryId") categoryId: Int? = null
    ): ApiResult<PageResult<Item>>

    @GET("api/items/{id}")
    suspend fun getItemDetail(@Path("id") id: Long): ApiResult<Item>

    @Multipart
    @POST("api/items")
    suspend fun createItem(
        @Part title: okhttp3.MultipartBody.Part,
        @Part categoryId: okhttp3.MultipartBody.Part,
        @Part condition: okhttp3.MultipartBody.Part,
        @Part description: okhttp3.MultipartBody.Part,
        @Part wantDescription: okhttp3.MultipartBody.Part,
        @Part images: List<okhttp3.MultipartBody.Part>
    ): ApiResult<Item>

    @PUT("api/items/{id}")
    suspend fun updateItem(@Path("id") id: Long, @Body body: ItemBody): ApiResult<Item>

    @DELETE("api/items/{id}")
    suspend fun deleteItem(@Path("id") id: Long): ApiResult<Void>

    @GET("api/items/my")
    suspend fun getMyItems(): ApiResult<List<Item>>

    @PUT("api/items/{id}/off-shelf")
    suspend fun offShelfItem(@Path("id") id: Long): ApiResult<Void>

    // ======== 交换请求 ========
    @POST("api/exchange-requests")
    suspend fun createExchange(@Body body: ExchangeBody): ApiResult<ExchangeRequest>

    @GET("api/exchange-requests")
    suspend fun getExchangeRequests(): ApiResult<List<ExchangeRequest>>

    @PUT("api/exchange-requests/{id}/accept")
    suspend fun acceptExchange(@Path("id") id: Long): ApiResult<ExchangeRequest>

    @PUT("api/exchange-requests/{id}/reject")
    suspend fun rejectExchange(@Path("id") id: Long): ApiResult<ExchangeRequest>

    @PUT("api/exchange-requests/{id}/cancel")
    suspend fun cancelExchange(@Path("id") id: Long): ApiResult<Void>

    // ======== 通知 ========
    @GET("api/notifications")
    suspend fun getNotifications(): ApiResult<List<Notification>>

    @PUT("api/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: Long): ApiResult<Void>

    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): ApiResult<UnreadCount>

    // ======== 聊天 ========
    @GET("api/chat/{exchangeId}")
    suspend fun getChatMessages(@Path("exchangeId") exchangeId: Long): ApiResult<List<ChatMessage>>

    @POST("api/chat")
    suspend fun sendChatMessage(@Body body: ChatSendBody): ApiResult<ChatMessage>

    // ======== AI ========
    @POST("api/ai/polish")
    suspend fun polishDescription(@Body body: Map<String, String>): ApiResult<AiPolishResult>

    companion object {
        fun create(tokenProvider: () -> String?): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val request = chain.request().newBuilder()
                tokenProvider()?.let { request.addHeader("Authorization", "Bearer $it") }
                chain.proceed(request.build())
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
