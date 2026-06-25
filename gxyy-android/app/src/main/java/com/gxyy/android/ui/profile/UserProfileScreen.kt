package com.gxyy.android.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun UserProfileScreen(userId: Long, api: ApiService, onBack: () -> Unit, onItemClick: (Long) -> Unit, onFollowList: (Long, String) -> Unit) {
    var user by remember { mutableStateOf<User?>(null) }
    var items by remember { mutableStateOf<List<Item>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var isFollowing by remember { mutableStateOf(false) }
    var followLoading by remember { mutableStateOf(false) }
    var followerCount by remember { mutableLongStateOf(0) }
    var followingCount by remember { mutableLongStateOf(0) }
    val ctx = LocalContext.current
    val snackbar = SnackbarHostState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        safeApiCall(snackbar) { api.getUserById(userId) }?.let { user = it }
        safeApiCall(snackbar) { api.getItems(ownerId = userId, size = 50) }?.let { items = it.records }
        safeApiCall(null) { api.checkFollow(userId) }?.let { isFollowing = it.isFollowing }
        safeApiCall(null) { api.getFollowCounts(userId) }?.let { followerCount = it.followers; followingCount = it.following }
        loading = false
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        if (loading) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else user?.let { u ->
            Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
                // Hero card
                Card(Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (u.avatar != null) AsyncImage(model = u.avatar.toImageUrl(), null, Modifier.size(80.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.Person, null, Modifier.size(80.dp).padding(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(u.username, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        if (!u.bio.isNullOrBlank()) { Spacer(Modifier.height(4.dp)); Text(u.bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }

                        // Follow stats
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            TextButton(onClick = { onFollowList(userId, "followers") }) { Text("$followerCount 粉丝", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) }
                            TextButton(onClick = { onFollowList(userId, "following") }) { Text("$followingCount 关注", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) }
                        }

                        // Follow button
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            scope.launch { followLoading = true
                                if (isFollowing) { safeApiCall(snackbar) { api.unfollow(userId) }; isFollowing = false; followerCount = (followerCount - 1).coerceAtLeast(0); Toast.makeText(ctx, "已取消关注", Toast.LENGTH_SHORT).show() }
                                else { safeApiCall(snackbar) { api.follow(userId) }; isFollowing = true; followerCount++; Toast.makeText(ctx, "已关注", Toast.LENGTH_SHORT).show() }
                                followLoading = false }
                        }, enabled = !followLoading, shape = RoundedCornerShape(8.dp)) {
                            Text(if (isFollowing) "已关注" else "关注", color = if (isFollowing) MaterialTheme.colorScheme.onSurface else Color.White)
                        }
                    }
                }

                // Contact info
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("联系方式", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        DetailRow("手机号", u.phone ?: "未填写"); DetailRow("邮箱", u.email ?: "未填写"); DetailRow("地址", u.address ?: "未填写")
                    }
                }

                // Items
                if (items.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("TA 的在架物品", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    items.forEach { item ->
                        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onItemClick(item.id) },
                            shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), elevation = CardDefaults.cardElevation(0.dp)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                val img = item.thumbImages?.firstOrNull() ?: item.images?.firstOrNull() ?: ""
                                if (img.isNotEmpty()) AsyncImage(model = img.toImageUrl(), null, Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                                else Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Photo, null, Modifier.size(24.dp)) }
                                Spacer(Modifier.width(12.dp))
                                Column { Text(item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp); Text(item.categoryName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$label：", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(64.dp))
        Text(value, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
