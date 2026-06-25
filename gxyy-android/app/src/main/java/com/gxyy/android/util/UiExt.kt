package com.gxyy.android.util

import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.gxyy.android.model.ApiResult

/** Show error from ApiResult on screen via Snackbar + Toast */
@Composable
fun ShowError(result: ApiResult<*>?, snackbarHostState: SnackbarHostState) {
    val ctx = LocalContext.current
    LaunchedEffect(result) {
        if (result != null && result.code != 200) {
            val msg = result.message.ifBlank { "请求失败" }
            snackbarHostState.showSnackbar(msg)
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

/** Extract error message from any Throwable */
fun Throwable.userMessage(): String {
    val body = this.message ?: ""
    return try {
        val gson = com.google.gson.Gson()
        val r = gson.fromJson(body, ApiResult::class.java)
        r?.message?.ifBlank { "网络错误" } ?: "网络错误"
    } catch (e: Exception) {
        body.ifBlank { "网络错误，请检查连接" }
    }
}

/** Safe API call wrapper — returns data or null and shows error */
suspend fun <T> safeApiCall(
    snackbar: SnackbarHostState? = null,
    block: suspend () -> ApiResult<T>
): T? {
    return try {
        val res = block()
        if (res.code == 200) res.data
        else {
            snackbar?.showSnackbar(res.message.ifBlank { "操作失败" })
            null
        }
    } catch (e: Exception) {
        snackbar?.showSnackbar(e.userMessage())
        null
    }
}
