package com.gxyy.android.ui.home

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.*
import com.gxyy.android.model.toImageUrl
import com.gxyy.android.model.orImageUrl
import com.gxyy.android.util.safeApiCall
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(api: ApiService, onItemClick: (Long) -> Unit, onUserClick: (Long) -> Unit, onCreateClick: () -> Unit) {
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var keyword by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Int?>(null) }
    var condition by remember { mutableStateOf<String?>(null) }
    var sortOrder by remember { mutableStateOf("newest") }
    var page by remember { mutableIntStateOf(1) }
    var favIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val ctx = LocalContext.current
    val snackbar = SnackbarHostState()
    val scope = rememberCoroutineScope()

    val fetchItems: suspend () -> Unit = {
        loading = true
        safeApiCall(snackbar) {
            api.getItems(page = page, size = 30, keyword = keyword.ifBlank { null },
                categoryId = categoryId, condition = condition, sortOrder = sortOrder)
        }?.let { items = it.records }
        loading = false
    }

    LaunchedEffect(Unit) { fetchItems(); safeApiCall(null) { api.getFavorites() }?.let { favIds = it.map { f -> f.id }.toSet() } }
    LaunchedEffect(page, categoryId, condition, sortOrder) { fetchItems() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, "发布物品", tint = Color.White)
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            // Search pill
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = keyword, onValueChange = { keyword = it },
                    modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(50),
                    placeholder = { Text("搜索物品...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outline),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = { if (keyword.isNotEmpty()) IconButton({ keyword = ""; page = 1 }) { Icon(Icons.Default.Close, null) } })
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = { page = 1; scope.launch { fetchItems() } },
                    modifier = Modifier.size(48.dp), shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Search, null, tint = Color.White)
                }
            }

            // Category pills
            ScrollableTabRow(selectedTabIndex = CATEGORIES.indexOfFirst { it.first == categoryId }.coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(), edgePadding = 12.dp, divider = {},
                indicator = { Box(Modifier.height(0.dp)) }) {
                val catList = CATEGORIES
                catList.forEach { (id, name) ->
                    Tab(selected = categoryId == id, onClick = { categoryId = id; page = 1 }, modifier = Modifier.padding(horizontal = 2.dp)) {
                        Surface(shape = RoundedCornerShape(50), color = if (categoryId == id) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                            modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(name, Modifier.padding(horizontal = 14.dp, vertical = 6.dp), fontSize = 14.sp,
                                fontWeight = if (categoryId == id) FontWeight.Bold else FontWeight.Normal,
                                color = if (categoryId == id) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Filter row
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = condition != null, onClick = { condition = if (condition == null) "NEW" else null; page = 1 },
                    label = { Text(condition?.let { CONDITION_LABELS[it] } ?: "成色", fontSize = 13.sp) },
                    trailingIcon = { if (condition != null) Icon(Icons.Default.Close, null, Modifier.size(16.dp)) })
                if (condition != null) {
                    val altConditions = CONDITION_LABELS.entries.filter { it.key != condition }.take(3)
                    altConditions.forEach { (k, v) ->
                        FilterChip(selected = false, onClick = { condition = k; page = 1 }, label = { Text(v, fontSize = 13.sp) })
                    }
                }
                Spacer(Modifier.weight(1f))
                FilterChip(selected = true, onClick = { sortOrder = if (sortOrder == "newest") "oldest" else "newest"; page = 1 },
                    label = { Text(if (sortOrder == "newest") "最新" else "最早", fontSize = 13.sp) })
            }

            if (loading && items.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            else if (items.isEmpty()) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无物品", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            else {
                LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalItemSpacing = 8.dp) {
                    items(items, key = { it.id }) { item ->
                        Card(onClick = { onItemClick(item.id) }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                            Box {
                                val imgUrl = item.thumbImages?.firstOrNull() ?: item.images?.firstOrNull() ?: ""
                                if (imgUrl.isNotEmpty()) {
                                    AsyncImage(model = imgUrl.toImageUrl(), contentDescription = item.title,
                                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                                        contentScale = ContentScale.Crop)
                                } else {
                                    Box(Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Photo, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                                    }
                                }
                                IconButton(onClick = {
                                    scope.launch {
                                        if (favIds.contains(item.id)) {
                                            safeApiCall(snackbar) { api.removeFavorite(item.id) }
                                            favIds = favIds - item.id; Toast.makeText(ctx, "已取消收藏", Toast.LENGTH_SHORT).show()
                                        } else {
                                            safeApiCall(snackbar) { api.addFavorite(item.id) }
                                            favIds = favIds + item.id; Toast.makeText(ctx, "已收藏", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp).background(Color.White.copy(alpha = 0.85f), CircleShape)) {
                                    Icon(if (favIds.contains(item.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        null, tint = if (favIds.contains(item.id)) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column(Modifier.padding(10.dp)) {
                                Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(item.categoryName, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 11.sp) }
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(CONDITION_LABELS[item.condition] ?: item.condition, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 11.sp) }
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onUserClick(item.ownerId) }) {
                                    if (item.ownerAvatar != null) AsyncImage(model = item.ownerAvatar.toImageUrl(), null, Modifier.size(16.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                    else Icon(Icons.Default.Person, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.width(4.dp))
                                    Text(item.ownerName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
