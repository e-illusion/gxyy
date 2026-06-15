package com.gxyy.android.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.Notification
import kotlinx.coroutines.launch

@Composable
fun NotificationsScreen(apiService: ApiService, navController: NavController) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val typeIcons = mapOf(
        "EXCHANGE_REQUEST" to Icons.Default.Notifications,
        "EXCHANGE_ACCEPTED" to Icons.Default.CheckCircle,
        "EXCHANGE_REJECTED" to Icons.Default.Cancel,
    )
    val typeColors = mapOf(
        "EXCHANGE_REQUEST" to Color(0xFF409EFF),
        "EXCHANGE_ACCEPTED" to Color(0xFF67C23A),
        "EXCHANGE_REJECTED" to Color(0xFFF56C6C),
    )

    fun loadNotifications() {
        scope.launch {
            loading = true
            try {
                notifications = apiService.getNotifications().data ?: emptyList()
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadNotifications() }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (notifications.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text("暂无消息", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn {
            items(notifications) { notif ->
                Surface(
                    color = if (notif.isReadBoolean()) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable {
                        if (!notif.isReadBoolean()) {
                            scope.launch { apiService.markRead(notif.id) }
                            loadNotifications()
                        }
                    }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            typeIcons[notif.type] ?: Icons.Default.Info,
                            contentDescription = null,
                            tint = typeColors[notif.type] ?: Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(notif.content, fontSize = 14.sp, lineHeight = 20.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(notif.createTime ?: "", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!notif.isReadBoolean()) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
