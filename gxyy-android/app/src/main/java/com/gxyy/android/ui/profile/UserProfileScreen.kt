package com.gxyy.android.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.gxyy.android.BuildConfig
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.Item
import com.gxyy.android.model.User
import com.gxyy.android.model.thumbUrl
import com.gxyy.android.ui.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: Long,
    apiService: ApiService,
    navController: NavController
) {
    var user by remember { mutableStateOf<User?>(null) }
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        try {
            val userRes = apiService.getUserById(userId)
            user = userRes.data
            val itemsRes = apiService.getItems(page = 1, size = 50)
            items = itemsRes.data?.records?.filter { it.ownerId == userId } ?: emptyList()
        } catch (_: Exception) {}
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.username ?: "用户主页") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        user?.let { u ->
            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            ) {
                // 用户信息卡片
                Card(Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = u.avatar?.let { "${BuildConfig.BASE_URL.trimEnd('/')}${it.thumbUrl()}" },
                            contentDescription = "头像",
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(u.username, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        u.bio?.ifBlank { null }?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(it, fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // 联系方式
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("联系方式", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        DetailRow("手机号", u.phone ?: "未填写")
                        DetailRow("邮箱", u.email ?: "未填写")
                        DetailRow("地址", u.address ?: "未填写")
                    }
                }

                // TA 的物品
                if (items.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("TA 的在架物品", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    items.forEach { item ->
                        Card(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    navController.navigate(Screen.ItemDetail.createRoute(item.id))
                                }
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                val img = (item.thumbImages?.firstOrNull() ?: item.images?.firstOrNull())?.let { "${BuildConfig.BASE_URL.trimEnd('/')}$it" }
                                AsyncImage(
                                    model = img,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(item.title, fontWeight = FontWeight.Bold)
                                    Text(item.categoryName, fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text("$label：", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(60.dp))
        Text(value, fontSize = 14.sp)
    }
}
