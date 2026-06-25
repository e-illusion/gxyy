package com.gxyy.android.ui.item

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.*
import com.gxyy.android.model.toImageUrl
import com.gxyy.android.model.orImageUrl
import com.gxyy.android.util.safeApiCall
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(itemId: Long, api: ApiService, currentUserId: Long = 0, onBack: () -> Unit, onLogin: () -> Unit, onEdit: (Long) -> Unit, onUser: (Long) -> Unit, onChat: (Long) -> Unit) {
    var item by remember { mutableStateOf<Item?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showExchangeDialog by remember { mutableStateOf(false) }
    var myItems by remember { mutableStateOf<List<Item>>(emptyList()) }
    var selectedItemId by remember { mutableStateOf<Long?>(null) }
    var exchangeMsg by remember { mutableStateOf("") }
    var isFav by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var aiMatches by remember { mutableStateOf<List<AiMatchItem>>(emptyList()) }
    var showAiDialog by remember { mutableStateOf(false) }
    var aiLoading by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val snackbar = SnackbarHostState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(itemId) {
        safeApiCall(snackbar) { api.getItemDetail(itemId) }?.let { item = it }
        safeApiCall(null) { api.checkFavorite(itemId) }?.let { isFav = it.isFavorited }
        loading = false
    }

    val loadMyItems: () -> Unit = {
        scope.launch {
            safeApiCall(snackbar) { api.getMyItems() }?.let { myItems = it.filter { i -> i.status == "ACTIVE" && i.id != itemId } }
        }
    }

    val toggleFav: () -> Unit = {
        scope.launch {
            if (isFav) { safeApiCall(snackbar) { api.removeFavorite(itemId) }; isFav = false; Toast.makeText(ctx, "已取消收藏", Toast.LENGTH_SHORT).show() }
            else { safeApiCall(snackbar) { api.addFavorite(itemId) }; isFav = true; Toast.makeText(ctx, "已收藏", Toast.LENGTH_SHORT).show() }
        }
    }

    val openAiMatch: () -> Unit = {
        scope.launch {
            showAiDialog = true; aiLoading = true; aiMatches = emptyList()
            safeApiCall(snackbar) { api.matchItems(itemId) }?.let { aiMatches = it }
            aiLoading = false
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        if (loading) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else item?.let { it ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                // Image with preview
                val imgUrl = it.images?.firstOrNull() ?: ""
                Box(Modifier.fillMaxWidth().clickable { showPreview = true }) {
                    if (imgUrl.isNotEmpty()) AsyncImage(model = imgUrl.toImageUrl(), null, Modifier.fillMaxWidth().aspectRatio(1.2f), contentScale = ContentScale.Crop)
                    else Box(Modifier.fillMaxWidth().aspectRatio(1.2f).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Icon(Icons.Default.Photo, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }

                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(it.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(50), color = if (it.status == "ACTIVE") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant) {
                            Text(STATUS_LABELS[it.status] ?: it.status, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 13.sp, color = if (it.status == "ACTIVE") Color(0xFF2DA844) else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(it.categoryName, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 13.sp) }
                        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) { Text(CONDITION_LABELS[it.condition] ?: it.condition, Modifier.padding(horizontal = 8.dp, vertical = 3.dp), fontSize = 13.sp) }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                    Text("物品描述", fontWeight = FontWeight.Bold, fontSize = 16.sp); Spacer(Modifier.height(6.dp))
                    Text(it.description, fontSize = 15.sp, lineHeight = 24.sp)
                    if (!it.wantDescription.isNullOrBlank()) { Spacer(Modifier.height(16.dp)); Text("想换什么", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(6.dp)); Text(it.wantDescription, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary) }
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))

                    // Owner
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onUser(it.ownerId) }.padding(8.dp)) {
                        if (it.ownerAvatar != null) AsyncImage(model = it.ownerAvatar.toImageUrl(), null, Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.Person, null, Modifier.size(48.dp).padding(8.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.padding(start = 12.dp)) { Text(it.ownerName, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("发布于 ${it.createTime ?: ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Action card (Airbnb reservation-card style)
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                        Column(Modifier.padding(20.dp)) {
                            if (it.status == "ACTIVE") {
                                val isOwner = currentUserId > 0 && it.ownerId == currentUserId
                                if (!isOwner) {
                                    Button(onClick = { loadMyItems(); showExchangeDialog = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Icon(Icons.Default.SwapHoriz, null); Spacer(Modifier.width(6.dp)); Text("我想换！") }
                                } else {
                                    OutlinedButton(onClick = { onEdit(it.id) }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) { Text("编辑物品") }
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(onClick = openAiMatch, Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF460479))) { Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("AI 推荐交换") }
                                }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = toggleFav, Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                                    colors = if (isFav) ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonColors()) {
                                    Icon(if (isFav) Icons.Default.Favorite else Icons.Outlined.Star, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(if (isFav) "已收藏" else "收藏物品")
                                }
                            }
                            if (isFav) { Spacer(Modifier.height(6.dp)); Text("发起交换不会立即成交，需双方同意", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)) }
                        }
                    }
                }
            }
        } ?: Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("物品不存在", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }

    // Exchange dialog
    if (showExchangeDialog) AlertDialog(onDismissRequest = { showExchangeDialog = false }, title = { Text("选择交换物品") }, text = {
        Column { myItems.forEach { mi -> Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = selectedItemId == mi.id, onClick = { selectedItemId = mi.id }); Text(mi.title, Modifier.padding(start = 8.dp)) } }
            if (myItems.isEmpty()) Text("你没有可用的在架物品", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp)); OutlinedTextField(value = exchangeMsg, onValueChange = { exchangeMsg = it }, label = { Text("留言（选填）") }, modifier = Modifier.fillMaxWidth()) }
    }, confirmButton = { Button(onClick = { scope.launch { safeApiCall(snackbar) { api.createExchange(ExchangeBody(itemId, selectedItemId!!, exchangeMsg.ifBlank { null })) }; showExchangeDialog = false } }, enabled = selectedItemId != null) { Text("发起交换") } },
        dismissButton = { TextButton(onClick = { showExchangeDialog = false }) { Text("取消") } })

    // AI Match dialog
    if (showAiDialog) AlertDialog(onDismissRequest = { showAiDialog = false }, title = { Text("AI 推荐交换") }, text = {
        Column { if (aiLoading) { Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        else if (aiMatches.isEmpty()) Text("暂无推荐结果", color = MaterialTheme.colorScheme.onSurfaceVariant)
        else aiMatches.forEach { m -> Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val mi = m.item; val mImg = mi.thumbImages?.firstOrNull() ?: mi.images?.firstOrNull() ?: ""
                if (mImg.isNotEmpty()) AsyncImage(model = mImg.toImageUrl(), null, Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                else Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Photo, null, Modifier.size(20.dp)) }
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(mi.title, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(mi.categoryName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(m.reason, fontSize = 12.sp, color = Color(0xFF460479)) }
            }
        } } }
    }, confirmButton = { TextButton(onClick = { showAiDialog = false }) { Text("关闭") } })

    // Image preview
    if (showPreview) Dialog(onDismissRequest = { showPreview = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color.Black).clickable { showPreview = false }, contentAlignment = Alignment.Center) {
            val imgUrl = item?.images?.firstOrNull() ?: ""
            if (imgUrl.isNotEmpty()) AsyncImage(model = imgUrl.toImageUrl(), null, Modifier.fillMaxWidth(), contentScale = ContentScale.Fit)
        }
    }
}
