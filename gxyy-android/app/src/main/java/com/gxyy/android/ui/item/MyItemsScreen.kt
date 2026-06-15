package com.gxyy.android.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.Item
import com.gxyy.android.ui.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItemsScreen(apiService: ApiService, navController: NavController) {
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val conditionMap = mapOf(
        "NEW" to "全新", "LIKE_NEW" to "几乎全新",
        "SLIGHTLY_USED" to "轻微使用", "NORMAL_USE" to "正常使用"
    )

    fun loadItems() {
        scope.launch {
            loading = true
            try {
                items = apiService.getMyItems().data ?: emptyList()
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadItems() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的物品") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("还没有发布物品")
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(items) { item ->
                    ListItem(
                        headlineContent = { Text(item.title, fontWeight = FontWeight.Bold) },
                        supportingContent = {
                            Column {
                                Text("${item.categoryName} · ${conditionMap[item.condition]}")
                                Text(item.status, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.ItemDetail.createRoute(item.id))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
