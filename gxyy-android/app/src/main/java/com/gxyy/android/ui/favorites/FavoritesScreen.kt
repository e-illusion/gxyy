package com.gxyy.android.ui.favorites

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
fun FavoritesScreen(api: ApiService, onItemClick: (Long) -> Unit, onBack: () -> Unit) {
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var filterStatus by remember { mutableStateOf<String?>(null) }
    var filterCondition by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    val snackbar = SnackbarHostState()
    val scope = rememberCoroutineScope()

    val filtered = items.filter {
        (filterStatus == null || it.status == filterStatus) && (filterCondition == null || it.condition == filterCondition)
    }

    LaunchedEffect(Unit) { safeApiCall(snackbar) { api.getFavorites() }?.let { items = it }; loading = false }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            TopAppBar(title = { Text("我的收藏", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))

            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filterStatus != null, onClick = { filterStatus = if (filterStatus == null) "ACTIVE" else null },
                    label = { Text(filterStatus?.let { STATUS_LABELS[it] } ?: "状态", fontSize = 13.sp) })
                if (filterStatus != null) {
                    val altStatuses = STATUS_LABELS.entries.filter { it.key != filterStatus }
                    altStatuses.forEach { (k, v) ->
                        FilterChip(selected = false, onClick = { filterStatus = k }, label = { Text(v, fontSize = 13.sp) })
                    }
                }
                FilterChip(selected = filterCondition != null, onClick = { filterCondition = if (filterCondition == null) "NEW" else null },
                    label = { Text(filterCondition?.let { CONDITION_LABELS[it] } ?: "成色", fontSize = 13.sp) })
                if (filterCondition != null) {
                    val altConds = CONDITION_LABELS.entries.filter { it.key != filterCondition }.take(3)
                    altConds.forEach { (k, v) ->
                        FilterChip(selected = false, onClick = { filterCondition = k }, label = { Text(v, fontSize = 13.sp) })
                    }
                }
            }

            if (loading) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            else if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("还没有收藏任何物品", color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(12.dp)); Button(onClick = onBack) { Text("去首页看看") } } }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("没有符合筛选条件的物品", color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); TextButton(onClick = { filterStatus = null; filterCondition = null }) { Text("清除筛选") } } }
            } else {
                LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Fixed(2), modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalItemSpacing = 8.dp) {
                    items(filtered, key = { it.id }) { item ->
                        Card(onClick = { onItemClick(item.id) }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                            Box {
                                val imgUrl = item.thumbImages?.firstOrNull() ?: item.images?.firstOrNull() ?: ""
                                if (imgUrl.isNotEmpty()) AsyncImage(model = imgUrl.toImageUrl(), null, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)), contentScale = ContentScale.Crop)
                                else Box(Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Icon(Icons.Default.Photo, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp)) }
                                IconButton(onClick = {
                                    scope.launch {
                                        safeApiCall(snackbar) { api.removeFavorite(item.id) }
                                        items = items.filter { it.id != item.id }
                                        Toast.makeText(ctx, "已取消收藏", Toast.LENGTH_SHORT).show()
                                    }
                                }, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(32.dp).background(Color.White.copy(alpha = 0.85f), CircleShape)) {
                                    Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                if (item.status != "ACTIVE") {
                                    Surface(Modifier.align(Alignment.TopStart).padding(6.dp), shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
                                        Text(STATUS_LABELS[item.status] ?: item.status, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            Column(Modifier.padding(10.dp)) {
                                Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(item.categoryName, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 11.sp) }
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(CONDITION_LABELS[item.condition] ?: item.condition, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 11.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
