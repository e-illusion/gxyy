package com.gxyy.android.ui.item

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.*
import com.gxyy.android.model.toImageUrl
import com.gxyy.android.util.safeApiCall
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyItemsScreen(api: ApiService, onBack: () -> Unit, onItemClick: (Long) -> Unit, onEdit: (Long) -> Unit) {
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var filterStatus by remember { mutableStateOf<String?>(null) }
    var filterCondition by remember { mutableStateOf<String?>(null) }
    var aiMatches by remember { mutableStateOf<List<AiMatchItem>>(emptyList()) }
    var showAiDialog by remember { mutableStateOf(false) }
    var aiLoading by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val snackbar = SnackbarHostState()
    val scope = rememberCoroutineScope()

    val filtered = items.filter {
        (filterStatus == null || it.status == filterStatus) && (filterCondition == null || it.condition == filterCondition)
    }

    LaunchedEffect(Unit) { safeApiCall(snackbar) { api.getMyItems() }?.let { items = it }; loading = false }

    val deleteItem: (Item) -> Unit = { item -> scope.launch { safeApiCall(snackbar) { api.deleteItem(item.id) }; items = items.filter { it.id != item.id }; Toast.makeText(ctx, "已删除", Toast.LENGTH_SHORT).show() } }
    val offShelf: (Item) -> Unit = { item -> scope.launch { safeApiCall(snackbar) { api.offShelfItem(item.id) }; safeApiCall(null) { api.getMyItems() }?.let { items = it }; Toast.makeText(ctx, "已下架", Toast.LENGTH_SHORT).show() } }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            TopAppBar(title = { Text("我的物品", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))

            // Filter row
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filterStatus != null, onClick = { filterStatus = if (filterStatus == null) "ACTIVE" else null },
                    label = { Text(filterStatus?.let { STATUS_LABELS[it] } ?: "状态", fontSize = 13.sp) })
                if (filterStatus != null) STATUS_LABELS.entries.filter { it.key != filterStatus }.forEach { (k, v) ->
                    FilterChip(selected = false, onClick = { filterStatus = k }, label = { Text(v, fontSize = 13.sp) })
                }
                FilterChip(selected = filterCondition != null, onClick = { filterCondition = if (filterCondition == null) "NEW" else null },
                    label = { Text(filterCondition?.let { CONDITION_LABELS[it] } ?: "成色", fontSize = 13.sp) })
                if (filterCondition != null) CONDITION_LABELS.entries.filter { it.key != filterCondition }.take(3).forEach { (k, v) ->
                    FilterChip(selected = false, onClick = { filterCondition = k }, label = { Text(v, fontSize = 13.sp) })
                }
            }

            if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else if (items.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("还没有发布物品", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else LazyColumn(Modifier.fillMaxSize()) {
                items(filtered, key = { it.id }) { item ->
                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onItemClick(item.id) },
                        shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(0.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val img = item.thumbImages?.firstOrNull() ?: item.images?.firstOrNull() ?: ""
                            if (img.isNotEmpty()) AsyncImage(model = img.toImageUrl(), null, Modifier.size(56.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            else Icon(Icons.Default.Photo, null, Modifier.size(56.dp).padding(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(item.categoryName, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 11.sp) }
                                    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(CONDITION_LABELS[item.condition] ?: item.condition, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 11.sp) }
                                    Surface(shape = RoundedCornerShape(50), color = if (item.status == "ACTIVE") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant) { Text(STATUS_LABELS[item.status] ?: item.status, Modifier.padding(horizontal = 6.dp, vertical = 1.dp), fontSize = 11.sp, color = if (item.status == "ACTIVE") Color(0xFF2DA844) else MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                                Text(item.createTime?.substring(0, 10) ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                            }
                            // Action buttons
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(onClick = {
                                    scope.launch { showAiDialog = true; aiLoading = true; aiMatches = emptyList()
                                        safeApiCall(snackbar) { api.matchItems(item.id) }?.let { aiMatches = it }; aiLoading = false }
                                }, enabled = item.status == "ACTIVE", contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text("AI推荐", fontSize = 12.sp, color = Color(0xFF460479)) }
                                if (item.status == "ACTIVE") TextButton(onClick = { onEdit(item.id) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text("编辑", fontSize = 12.sp) }
                                TextButton(onClick = { offShelf(item) }, enabled = item.status == "ACTIVE", contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text("下架", fontSize = 12.sp, color = Color(0xFFB85F0A)) }
                                TextButton(onClick = { deleteItem(item) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text("删除", fontSize = 12.sp, color = MaterialTheme.colorScheme.error) }
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Match dialog
    if (showAiDialog) AlertDialog(onDismissRequest = { showAiDialog = false }, title = { Text("AI 推荐交换") }, text = {
        Column { if (aiLoading) Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else if (aiMatches.isEmpty()) Text("暂无推荐结果")
        else aiMatches.forEach { m -> Card(Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                val mi = m.item; val mImg = mi.thumbImages?.firstOrNull() ?: mi.images?.firstOrNull() ?: ""
                if (mImg.isNotEmpty()) AsyncImage(model = mImg.toImageUrl(), null, Modifier.size(40.dp), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                Column(Modifier.weight(1f).padding(horizontal = 8.dp)) { Text(mi.title, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(m.reason, fontSize = 12.sp, color = Color(0xFF460479)) }
            }
        } } }
    }, confirmButton = { TextButton(onClick = { showAiDialog = false }) { Text("关闭") } })
}
