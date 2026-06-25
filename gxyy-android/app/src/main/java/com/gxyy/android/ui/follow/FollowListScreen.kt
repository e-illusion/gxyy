package com.gxyy.android.ui.follow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(api: ApiService, userId: Long, type: String, onUserClick: (Long) -> Unit, onBack: () -> Unit) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val snackbar = SnackbarHostState()

    LaunchedEffect(userId, type) {
        loading = true
        safeApiCall(snackbar) {
            if (type == "followers") api.getFollowers(userId) else api.getFollowing(userId)
        }?.let { users = it }
        loading = false
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            TopAppBar(title = { Text(if (type == "followers") "粉丝" else "关注", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))

            if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else if (users.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else LazyColumn(Modifier.fillMaxSize()) {
                items(users, key = { it.id }) { user ->
                    Row(Modifier.fillMaxWidth().clickable { onUserClick(user.id) }.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (user.avatar != null) AsyncImage(model = user.avatar.toImageUrl(), null, Modifier.size(44.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.Person, null, Modifier.size(44.dp).padding(6.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(user.username, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            if (!user.bio.isNullOrBlank()) Text(user.bio, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
