package com.gxyy.android.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gxyy.android.data.ApiService
import com.gxyy.android.model.LoginBody
import com.gxyy.android.util.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    navController: NavController
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("登录") },
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
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("GXYY", style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary)
            Text("校园以物易物平台", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(value = username, onValueChange = { username = it },
                label = { Text("用户名") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("密码") }, modifier = Modifier.fillMaxWidth())

            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = {
                scope.launch {
                    loading = true; errorMsg = null
                    try {
                        val res = apiService.login(LoginBody(username, password))
                        val data = res.data
                        if (data != null) {
                            tokenManager.saveAuth(data.token, data.user.username, data.user.avatar)
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            errorMsg = res.message
                        }
                    } catch (e: Exception) {
                        errorMsg = "登录失败: ${e.message}"
                    }
                    loading = false
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("登录")
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = {
                navController.navigate("register") { popUpTo("login") { inclusive = true } }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("没有账号？立即注册")
            }
        }
    }
}
