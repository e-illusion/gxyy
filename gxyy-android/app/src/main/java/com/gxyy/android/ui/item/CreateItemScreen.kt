package com.gxyy.android.ui.item

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gxyy.android.data.ApiService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(apiService: ApiService, navController: NavController) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(1) }
    var condition by remember { mutableStateOf("LIKE_NEW") }
    var description by remember { mutableStateOf("") }
    var wantDescription by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var aiLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val categories = listOf(
        1 to "数码电子", 2 to "书籍教材", 3 to "生活用品",
        4 to "服饰鞋包", 5 to "运动器材", 6 to "美妆护肤", 7 to "玩具乐器", 8 to "其他"
    )
    val conditionOptions = listOf(
        "NEW" to "全新", "LIKE_NEW" to "几乎全新",
        "SLIGHTLY_USED" to "轻微使用", "NORMAL_USE" to "正常使用"
    )

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> imageUris = uris }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布物品") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("物品名称") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            // 分类
            var catExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                OutlinedTextField(
                    value = categories.find { it.first == categoryId }?.second ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("分类") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    categories.forEach { (id, name) ->
                        DropdownMenuItem(text = { Text(name) }, onClick = {
                            categoryId = id; catExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 成色
            var condExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = condExpanded, onExpandedChange = { condExpanded = it }) {
                OutlinedTextField(
                    value = conditionOptions.find { it.first == condition }?.second ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("成色") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = condExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = condExpanded, onDismissRequest = { condExpanded = false }) {
                    conditionOptions.forEach { (v, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            condition = v; condExpanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // 描述（含AI润色）
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("物品描述") },
                    modifier = Modifier.weight(1f), minLines = 4)
                IconButton(
                    onClick = {
                        scope.launch {
                            aiLoading = true
                            try {
                                val res = apiService.polishDescription(mapOf("description" to description))
                                description = res.data?.polished ?: description
                                snackbarHostState.showSnackbar("AI润色完成")
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("AI润色失败")
                            }
                            aiLoading = false
                        }
                    },
                    enabled = description.isNotBlank() && !aiLoading
                ) {
                    if (aiLoading) CircularProgressIndicator(Modifier.size(24.dp))
                    else Icon(Icons.Default.AutoAwesome, "AI润色")
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(value = wantDescription, onValueChange = { wantDescription = it },
                label = { Text("想换什么（选填）") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            Spacer(Modifier.height(16.dp))

            // 图片选择
            TextButton(onClick = { imagePicker.launch("image/*") }) {
                Text("选择图片 (已选${imageUris.size}张)")
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        try {
                            val parts = imageUris.map { uri ->
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val bytes = inputStream?.readBytes() ?: return@launch
                                inputStream.close()
                                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                                MultipartBody.Part.createFormData("images", "image.jpg", requestBody)
                            }
                            val res = apiService.createItem(
                                title = MultipartBody.Part.createFormData("title", title),
                                categoryId = MultipartBody.Part.createFormData("categoryId", categoryId.toString()),
                                condition = MultipartBody.Part.createFormData("condition", condition),
                                description = MultipartBody.Part.createFormData("description", description),
                                wantDescription = MultipartBody.Part.createFormData("wantDescription", wantDescription),
                                images = parts
                            )
                            if (res.code == 200) {
                                navController.popBackStack()
                            } else {
                                snackbarHostState.showSnackbar(res.message ?: "发布失败")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("发布失败: ${e.message}")
                        }
                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && description.isNotBlank() && !loading
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("发布")
            }
        }
    }
}
