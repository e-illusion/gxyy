package com.gxyy.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.gxyy.android.data.ApiService
import com.gxyy.android.ui.auth.LoginScreen
import com.gxyy.android.ui.auth.RegisterScreen
import com.gxyy.android.ui.chat.ChatScreen
import com.gxyy.android.ui.exchange.ExchangeRequestsScreen
import com.gxyy.android.ui.home.HomeScreen
import com.gxyy.android.ui.item.CreateItemScreen
import com.gxyy.android.ui.item.EditItemScreen
import com.gxyy.android.ui.item.ItemDetailScreen
import com.gxyy.android.ui.item.MyItemsScreen
import com.gxyy.android.ui.favorites.FavoritesScreen
import com.gxyy.android.ui.follow.FollowListScreen
import com.gxyy.android.ui.profile.NotificationsScreen
import com.gxyy.android.ui.profile.ProfileScreen
import com.gxyy.android.ui.profile.UserProfileScreen
import com.gxyy.android.util.TokenManager

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Login : Screen("login")
    object Register : Screen("register")
    object CreateItem : Screen("item/create")
    object ItemDetail : Screen("item/{id}") {
        fun createRoute(id: Long) = "item/$id"
    }
    object EditItem : Screen("item/{id}/edit") {
        fun createRoute(id: Long) = "item/$id/edit"
    }
    object MyItems : Screen("my-items")
    object ExchangeRequests : Screen("exchange-requests")
    object Chat : Screen("chat/{id}") {
        fun createRoute(id: Long) = "chat/$id"
    }
    object Notifications : Screen("notifications")
    object UserProfile : Screen("user/{id}") {
        fun createRoute(id: Long) = "user/$id"
    }
    object Profile : Screen("profile")
    object Favorites : Screen("favorites")
    object FollowList : Screen("follow-list/{userId}/{type}") {
        fun createRoute(userId: Long, type: String) = "follow-list/$userId/$type"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GxyyApp() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager.getInstance(context) }
    val apiService = remember { ApiService.create { tokenManager.getToken() } }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // Load userId from API if not cached (supports old login sessions)
    var myUserId by remember { mutableLongStateOf(tokenManager.getUserId()) }
    LaunchedEffect(Unit) {
        if (myUserId == 0L && tokenManager.getToken() != null) {
            try {
                val res = apiService.getProfile()
                if (res.code == 200 && res.data != null) myUserId = res.data.id
            } catch (_: Exception) {}
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home, "首页",
            { Icon(Icons.Filled.Home, contentDescription = "首页") },
            { Icon(Icons.Outlined.Home, contentDescription = "首页") }),
        BottomNavItem(Screen.ExchangeRequests, "交换",
            { Icon(Icons.Filled.SwapHoriz, contentDescription = "交换") },
            { Icon(Icons.Outlined.SwapHoriz, contentDescription = "交换") }),
        BottomNavItem(Screen.Notifications, "消息",
            { Icon(Icons.Filled.Notifications, contentDescription = "消息") },
            { Icon(Icons.Outlined.Notifications, contentDescription = "消息") }),
        BottomNavItem(Screen.Profile, "我的",
            { Icon(Icons.Filled.Person, contentDescription = "我的") },
            { Icon(Icons.Outlined.Person, contentDescription = "我的") }),
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                if (currentRoute == item.screen.route)
                                    item.selectedIcon() else item.unselectedIcon()
                            },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(api = apiService,
                    onItemClick = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                    onUserClick = { id -> navController.navigate(Screen.UserProfile.createRoute(id)) },
                    onCreateClick = { navController.navigate(Screen.CreateItem.route) })
            }
            composable(Screen.Login.route) {
                LoginScreen(apiService, tokenManager, navController)
            }
            composable(Screen.Register.route) {
                RegisterScreen(apiService, tokenManager, navController)
            }
            composable(Screen.CreateItem.route) {
                CreateItemScreen(apiService, navController)
            }
            composable(
                Screen.ItemDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                ItemDetailScreen(itemId = id, api = apiService,
                    currentUserId = myUserId,
                    onBack = { navController.popBackStack() },
                    onLogin = { navController.navigate(Screen.Login.route) },
                    onEdit = { iid -> navController.navigate(Screen.EditItem.createRoute(iid)) },
                    onUser = { uid -> navController.navigate(Screen.UserProfile.createRoute(uid)) },
                    onChat = { eid -> navController.navigate(Screen.Chat.createRoute(eid)) })
            }
            composable(
                Screen.EditItem.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                EditItemScreen(id, apiService, navController)
            }
            composable(Screen.MyItems.route) {
                MyItemsScreen(api = apiService,
                    onBack = { navController.popBackStack() },
                    onItemClick = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                    onEdit = { id -> navController.navigate(Screen.EditItem.createRoute(id)) })
            }
            composable(Screen.ExchangeRequests.route) {
                ExchangeRequestsScreen(apiService, navController)
            }
            composable(
                Screen.Chat.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                ChatScreen(id, apiService, navController)
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(apiService, navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(apiService, tokenManager, navController)
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(api = apiService,
                    onItemClick = { id -> navController.navigate(Screen.ItemDetail.createRoute(id)) },
                    onBack = { navController.popBackStack() })
            }
            composable(
                Screen.FollowList.route,
                arguments = listOf(navArgument("userId") { type = NavType.LongType }, navArgument("type") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getLong("userId") ?: 0L
                val type = backStackEntry.arguments?.getString("type") ?: "followers"
                FollowListScreen(api = apiService, userId = userId, type = type,
                    onUserClick = { id -> navController.navigate(Screen.UserProfile.createRoute(id)) },
                    onBack = { navController.popBackStack() })
            }
            composable(
                Screen.UserProfile.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                UserProfileScreen(userId = id, api = apiService,
                    onBack = { navController.popBackStack() },
                    onItemClick = { iid -> navController.navigate(Screen.ItemDetail.createRoute(iid)) },
                    onFollowList = { uid, type -> navController.navigate(Screen.FollowList.createRoute(uid, type)) })
            }
        }
    }
}
