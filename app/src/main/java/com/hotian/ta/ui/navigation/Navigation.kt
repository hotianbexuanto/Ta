package com.hotian.ta.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
    object Chat : Screen("chat")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: ChatViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route
    ) {
        composable(
            route = Screen.GroupList.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            GroupListScreen(
                viewModel = viewModel,
                onGroupClick = { groupId ->
                    viewModel.switchGroup(groupId)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            ChatScreen(
                viewModel = viewModel,
                onNavigateToGroupList = { navController.navigate(Screen.GroupList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(400)
                ) + fadeOut(animationSpec = tween(400))
            }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
