package com.gxyy.android.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.ChatMessage
import com.gxyy.android.model.ChatSendBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    exchangeId: Long,
    apiService: ApiService,
    navController: NavController
) {
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    fun loadMessages() {
        scope.launch {
            try {
                val res = apiService.getChatMessages(exchangeId)
                messages = res.data ?: emptyList()
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(exchangeId) { loadMessages() }

    // Auto-refresh
    LaunchedEffect(exchangeId) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            try {
                val res = apiService.getChatMessages(exchangeId)
                messages = res.data ?: messages
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("交换聊天") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (messages.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("暂无消息，开始聊天确定交换时间地点吧", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messages) { msg ->
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalAlignment = if (msg.isMine) Alignment.End else Alignment.Start
                        ) {
                            if (!msg.isMine) {
                                Text(msg.senderName, fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(2.dp))
                            Surface(
                                color = if (msg.isMine) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(msg.content, modifier = Modifier.padding(10.dp),
                                    color = if (msg.isMine) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(msg.createTime?.substring(11, 16) ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }

            // Input bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("输入消息...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    if (text.isNotBlank()) {
                        scope.launch {
                            try {
                                val res = apiService.sendChatMessage(ChatSendBody(exchangeId, text))
                                if (res.code == 200 && res.data != null) {
                                    messages = messages + res.data
                                    text = ""
                                } else {
                                    snackbarHostState.showSnackbar(res.message ?: "发送失败")
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("发送失败: ${e.message}")
                            }
                        }
                    }
                }) {
                    Icon(Icons.Default.Send, "发送", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
