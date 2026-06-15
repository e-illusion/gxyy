package com.gxyy.android.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import com.gxyy.android.model.thumbUrl
import com.gxyy.android.ui.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(apiService: ApiService, navController: NavController) {
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var keyword by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    val categories = listOf(
        null to "全部", 1 to "数码电子", 2 to "书籍教材", 3 to "生活用品",
        4 to "服饰鞋包", 5 to "运动器材", 6 to "美妆护肤", 7 to "玩具乐器", 8 to "其他"
    )
    val conditionMap = mapOf(
        "NEW" to "全新", "LIKE_NEW" to "几乎全新",
        "SLIGHTLY_USED" to "轻微使用", "NORMAL_USE" to "正常使用"
    )
    val scope = rememberCoroutineScope()

    fun loadItems() {
        scope.launch {
            loading = true
            try {
                val res = apiService.getItems(
                    page = 1, size = 50,
                    keyword = keyword.ifBlank { null },
                    categoryId = selectedCategoryId
                )
                items = res.data?.records ?: emptyList()
            } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(Unit) { loadItems() }

    Column {
        // 搜索栏
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                placeholder = { Text("搜索物品...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            FilledTonalButton(onClick = { loadItems() }) { Text("搜索") }
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(
                onClick = { navController.navigate(Screen.CreateItem.route) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, "发布", modifier = Modifier.size(20.dp))
            }
        }

        // 分类标签
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.first == selectedCategoryId }.coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 16.dp
        ) {
            categories.forEach { (id, name) ->
                Tab(
                    selected = selectedCategoryId == id,
                    onClick = {
                        selectedCategoryId = id
                        loadItems()
                    },
                    text = { Text(name, fontSize = 13.sp) }
                )
            }
        }

        // 物品网格
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无物品", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.ItemDetail.createRoute(item.id))
                        }
                    ) {
                        // 图片
                        val imgUrl = (item.thumbImages?.firstOrNull() ?: item.images?.firstOrNull())?.let { "${BuildConfig.BASE_URL.trimEnd('/')}$it" }
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column(Modifier.padding(10.dp)) {
                            Text(item.title, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(item.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary)
                                Text(conditionMap[item.condition] ?: item.condition,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(item.ownerName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.UserProfile.createRoute(item.ownerId))
                                })
                        }
                    }
                }
            }
        }
    }
}
