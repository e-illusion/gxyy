package com.gxyy.android.ui.auth

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
import com.gxyy.android.model.RegisterBody
import com.gxyy.android.ui.Screen
import com.gxyy.android.util.TokenManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    navController: NavController
) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("注册") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)
        ) {
            Text("创建账号", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(value = username, onValueChange = { username = it },
                label = { Text("用户名 *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it },
                label = { Text("手机号 *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("邮箱") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = address, onValueChange = { address = it },
                label = { Text("地址（选填）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("密码 *") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("确认密码 *") }, modifier = Modifier.fillMaxWidth())

            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = {
                if (password != confirmPassword) {
                    errorMsg = "两次密码不一致"; return@Button
                }
                scope.launch {
                    loading = true; errorMsg = null
                    try {
                        val res = apiService.register(RegisterBody(username, phone, email, address, password))
                        if (res.code == 200) {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Register.route) { inclusive = true }
                            }
                        } else {
                            errorMsg = res.message
                        }
                    } catch (e: Exception) {
                        errorMsg = "注册失败: ${e.message}"
                    }
                    loading = false
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !loading) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("注册")
            }
        }
    }
}
