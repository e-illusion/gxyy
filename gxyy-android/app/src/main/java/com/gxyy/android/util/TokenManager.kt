package com.gxyy.android.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gxyy_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_AVATAR = stringPreferencesKey("avatar")

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    private var cachedToken: String? = null
    private var cachedUsername: String? = null
    private var cachedAvatar: String? = null
    private var cacheLoaded = false

    private suspend fun loadCache() {
        if (!cacheLoaded) {
            cachedToken = tokenFlow.first()
            cachedUsername = context.dataStore.data.map { it[KEY_USERNAME] }.first()
            cachedAvatar = context.dataStore.data.map { it[KEY_AVATAR] }.first()
            cacheLoaded = true
        }
    }

    fun getToken(): String? {
        if (!cacheLoaded) {
            runBlocking { loadCache() }
        }
        return cachedToken
    }

    fun getUsername(): String? {
        if (!cacheLoaded) {
            runBlocking { loadCache() }
        }
        return cachedUsername
    }

    fun getAvatar(): String? {
        if (!cacheLoaded) {
            runBlocking { loadCache() }
        }
        return cachedAvatar
    }

    suspend fun saveAuth(token: String, username: String, avatar: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USERNAME] = username
            if (avatar != null) prefs[KEY_AVATAR] = avatar
        }
        // 更新内存缓存
        cachedToken = token
        cachedUsername = username
        cachedAvatar = avatar
        cacheLoaded = true
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
        cachedToken = null
        cachedUsername = null
        cachedAvatar = null
        cacheLoaded = false
    }
}
