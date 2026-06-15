package com.gxyy.android.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.gxyy.android.ui.Screen
import com.gxyy.android.util.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: Long,
    apiService: ApiService,
    tokenManager: TokenManager,
    navController: NavController
) {
    var item by remember { mutableStateOf<Item?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showExchangeDialog by remember { mutableStateOf(false) }
    var myItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedItemId by remember { mutableStateOf<Long?>(null) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val conditionMap = mapOf(
        "NEW" to "全新", "LIKE_NEW" to "几乎全新",
        "SLIGHTLY_USED" to "轻微使用", "NORMAL_USE" to "正常使用"
    )
    val statusMap = mapOf(
        "ACTIVE" to "在架", "EXCHANGED" to "已换出", "OFF_SHELF" to "已下架"
    )

    LaunchedEffect(itemId) {
        try {
            val res = apiService.getItemDetail(itemId)
            item = res.data
        } catch (_: Exception) {}
        loading = false
    }

    fun loadMyItems() {
        scope.launch {
            try {
                val res = apiService.getMyItems()
                myItems = res.data?.filter {
                    it.status == "ACTIVE" && it.id != itemId
                } ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    item?.let { it ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // 图片
            val imgUrl = it.images?.firstOrNull()?.let { img -> "${BuildConfig.BASE_URL.trimEnd('/')}$img" }
            AsyncImage(
                model = imgUrl,
                contentDescription = it.title,
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop
            )

            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(it.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    AssistChip(onClick = {}, label = { Text(statusMap[it.status] ?: it.status) })
                }

                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(onClick = {}, label = { Text(it.categoryName) })
                    SuggestionChip(onClick = {}, label = { Text(conditionMap[it.condition] ?: it.condition) })
                }

                Spacer(Modifier.height(20.dp))
                Text("物品描述", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text(it.description, fontSize = 15.sp, lineHeight = 24.sp)

                if (!it.wantDescription.isNullOrBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Text("想换什么", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(it.wantDescription, fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                // 发布者信息
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        navController.navigate(Screen.UserProfile.createRoute(it.ownerId))
                    }) {
                    AsyncImage(
                        model = it.ownerAvatar?.let { a -> "${BuildConfig.BASE_URL.trimEnd('/')}$a" },
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(it.ownerName, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        Text("发布于 ${it.createTime ?: ""}", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 操作按钮
                if (it.status == "ACTIVE") {
                    val currentUsername = tokenManager.getUsername()
                    if (currentUsername != it.ownerName) {
                        Button(
                            onClick = {
                                loadMyItems()
                                showExchangeDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SwapHoriz, null)
                            Spacer(Modifier.width(8.dp))
                            Text("想换！")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.EditItem.createRoute(it.id)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("编辑物品") }
                    }
                }
            }
        }
    }

    // 交换弹窗
    if (showExchangeDialog) {
        AlertDialog(
            onDismissRequest = { showExchangeDialog = false },
            title = { Text("选择交换物品") },
            text = {
                Column {
                    myItems.forEach { myItem ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedItemId == myItem.id,
                                onClick = { selectedItemId = myItem.id }
                            )
                            Text(myItem.title, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    if (myItems.isEmpty()) {
                        Text("你没有可用的在架物品，请先发布物品")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("留言（选填）") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                selectedItemId?.let { sid ->
                                    apiService.createExchange(
                                        com.gxyy.android.model.ExchangeBody(
                                            toItemId = itemId,
                                            fromItemId = sid,
                                            message = message.ifBlank { null }
                                        )
                                    )
                                    showExchangeDialog = false
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    enabled = selectedItemId != null
                ) { Text("发起交换") }
            },
            dismissButton = {
                TextButton(onClick = { showExchangeDialog = false }) { Text("取消") }
            }
        )
    }
}
