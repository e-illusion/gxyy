package com.gxyy.android.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gxyy.android.BuildConfig
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.UpdateProfileBody
import com.gxyy.android.model.User
import com.gxyy.android.model.thumbUrl
import com.gxyy.android.ui.Screen
import com.gxyy.android.util.TokenManager
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun ProfileScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    navController: NavController
) {
    var user by remember { mutableStateOf<User?>(null) }
    var loading by remember { mutableStateOf(true) }
    var editing by remember { mutableStateOf(false) }
    var editBio by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var uploadLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 头像上传
    val avatarPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                uploadLoading = true
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", requestBody)
                        val res = apiService.uploadAvatar(part)
                        if (res.code == 200) {
                            user = apiService.getProfile().data
                            snackbarHostState.showSnackbar("头像更新成功")
                        }
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("上传失败: ${e.message}")
                }
                uploadLoading = false
            }
        }
    }

    val isLoggedIn = remember { tokenManager.getToken() != null }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            try {
                user = apiService.getProfile().data
                editBio = user?.bio ?: ""
                editPhone = user?.phone ?: ""
                editEmail = user?.email ?: ""
                editAddress = user?.address ?: ""
            } catch (_: Exception) {}
            loading = false
        } else {
            loading = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        if (!isLoggedIn) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("请先登录", fontSize = 18.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.navigate(Screen.Login.route) }) {
                        Text("去登录")
                    }
                }
            }
            return@Scaffold
        }

        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // 头像 + 用户信息
            Card(Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = user?.avatar?.let { "${BuildConfig.BASE_URL.trimEnd('/')}${it.thumbUrl()}" },
                        contentDescription = "头像",
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { avatarPicker.launch("image/*") },
                        enabled = !uploadLoading) {
                        if (uploadLoading) CircularProgressIndicator(Modifier.size(16.dp))
                        else Text("更换头像")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(user?.username ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    user?.address?.ifBlank { null }?.let { addr ->
                        Text(addr, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(user?.bio?.ifBlank { "这个人很懒，什么都没写..." } ?: "",
                        fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 菜单
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column {
                    ListItem(
                        headlineContent = { Text("我的物品") },
                        leadingContent = { Icon(Icons.Default.Inventory2, null) },
                        modifier = Modifier.clickable { navController.navigate(Screen.MyItems.route) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("交换请求") },
                        leadingContent = { Icon(Icons.Default.SwapHoriz, null) },
                        modifier = Modifier.clickable { navController.navigate(Screen.ExchangeRequests.route) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("消息通知") },
                        leadingContent = { Icon(Icons.Default.Notifications, null) },
                        modifier = Modifier.clickable { navController.navigate(Screen.Notifications.route) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("编辑资料") },
                        leadingContent = { Icon(Icons.Default.Edit, null) },
                        modifier = Modifier.clickable { editing = true }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 退出登录
            Button(
                onClick = {
                    scope.launch {
                        tokenManager.clear()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { Text("退出登录") }
        }
    }

    // 编辑弹窗
    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("编辑资料") },
            text = {
                Column {
                    OutlinedTextField(value = editBio, onValueChange = { editBio = it },
                        label = { Text("个人简介") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it },
                        label = { Text("手机号") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = editEmail, onValueChange = { editEmail = it },
                        label = { Text("邮箱") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = editAddress, onValueChange = { editAddress = it },
                        label = { Text("地址") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        try {
                            user = apiService.updateProfile(
                                UpdateProfileBody(editBio, editPhone, editEmail, editAddress)
                            ).data
                            editing = false
                        } catch (_: Exception) {}
                    }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text("取消") }
            }
        )
    }
}
