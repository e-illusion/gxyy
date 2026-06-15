package com.gxyy.android.ui.item

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.ItemBody
import com.gxyy.android.ui.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemScreen(
    itemId: Long,
    apiService: ApiService,
    navController: NavController
) {
    var title by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf(1) }
    var condition by remember { mutableStateOf("LIKE_NEW") }
    var description by remember { mutableStateOf("") }
    var wantDescription by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var pageLoading by remember { mutableStateOf(true) }
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

    // 加载已有物品数据
    LaunchedEffect(itemId) {
        try {
            val res = apiService.getItemDetail(itemId)
            res.data?.let { item ->
                title = item.title
                categoryId = item.categoryId
                condition = item.condition
                description = item.description
                wantDescription = item.wantDescription ?: ""
            }
        } catch (_: Exception) {
            snackbarHostState.showSnackbar("加载物品信息失败")
        }
        pageLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑物品") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (pageLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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

            // 描述
            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("物品描述") },
                modifier = Modifier.fillMaxWidth(), minLines = 4)

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(value = wantDescription, onValueChange = { wantDescription = it },
                label = { Text("想换什么（选填）") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        try {
                            apiService.updateItem(
                                itemId,
                                ItemBody(
                                    title = title,
                                    categoryId = categoryId,
                                    condition = condition,
                                    description = description,
                                    wantDescription = wantDescription.ifBlank { null }
                                )
                            )
                            navController.popBackStack()
                        } catch (_: Exception) {
                            snackbarHostState.showSnackbar("保存失败")
                        }
                        loading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && description.isNotBlank() && !loading
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("保存修改")
            }
        }
    }
}
