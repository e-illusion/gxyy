package com.gxyy.android.ui.exchange

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.ExchangeRequest
import com.gxyy.android.ui.Screen
import kotlinx.coroutines.launch

@Composable
fun ExchangeRequestsScreen(apiService: ApiService, navController: NavController) {
    var requests by remember { mutableStateOf<List<ExchangeRequest>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var tab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val statusMap = mapOf(
        "PENDING" to "待确认", "ACCEPTED" to "已同意",
        "REJECTED" to "已拒绝", "CANCELLED" to "已取消"
    )

    fun loadRequests() {
        scope.launch {
            loading = true
            try {
                requests = apiService.getExchangeRequests().data ?: emptyList()
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadRequests() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("全部") })
            }

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (requests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无交换请求")
                }
            } else {
                LazyColumn {
                    items(requests) { req ->
                        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    AssistChip(onClick = {}, label = { Text(statusMap[req.status] ?: req.status) })
                                    Text(req.createTime ?: "", fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("${req.fromUserName} 用《${req.fromItemTitle}》换《${req.toItemTitle}》",
                                    fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    TextButton(onClick = {
                                        navController.navigate(Screen.UserProfile.createRoute(req.fromUserId))
                                    }, contentPadding = PaddingValues(0.dp)) {
                                        Text("查看 ${req.fromUserName} 资料", fontSize = 12.sp)
                                    }
                                    TextButton(onClick = {
                                        navController.navigate(Screen.UserProfile.createRoute(req.toUserId))
                                    }, contentPadding = PaddingValues(0.dp)) {
                                        Text("查看 ${req.toUserName} 资料", fontSize = 12.sp)
                                    }
                                }
                                req.message?.let {
                                    Spacer(Modifier.height(4.dp))
                                    Text("$it", fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (req.status == "PENDING") {
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            scope.launch {
                                                apiService.acceptExchange(req.id)
                                                loadRequests()
                                            }
                                        }, colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )) { Text("同意") }
                                        OutlinedButton(onClick = {
                                            scope.launch {
                                                apiService.rejectExchange(req.id)
                                                loadRequests()
                                            }
                                        }) { Text("拒绝") }
                                    }
                                }
                                if (req.status == "ACCEPTED") {
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = {
                                        navController.navigate(Screen.Chat.createRoute(req.id))
                                    }) { Text("💬 聊天") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
