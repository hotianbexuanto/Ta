package com.hotian.ta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hotian.ta.ui.screen.ChatScreen
import com.hotian.ta.ui.screen.GroupListScreen
import com.hotian.ta.ui.screen.SettingsScreen
import com.hotian.ta.viewmodel.ChatViewModel

sealed class Screen(val route: String) {
    object GroupList : Screen("group_list")
    object Chat : Screen("chat/{groupId}") {
        fun createRoute(groupId: Long) = "chat/$groupId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: ChatViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.GroupList.route
    ) {
        composable(Screen.GroupList.route) {
            GroupListScreen(
                viewModel = viewModel,
                onGroupClick = { groupId ->
                    viewModel.switchGroup(groupId)
                    navController.navigate(Screen.Chat.createRoute(groupId))
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("groupId") { type = NavType.LongType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getLong("groupId") ?: 0L
            ChatScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
