package com.nexus.downloader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nexus.downloader.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Browser : Screen("browser")
    object Downloads : Screen("downloads")
    object Playlist : Screen("playlist")
    object Settings : Screen("settings")
}

@Composable
fun NexusNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Browser.route) {
            BrowserScreen(navController = navController)
        }
        composable(Screen.Downloads.route) {
            DownloadsScreen(navController = navController)
        }
        composable(Screen.Playlist.route) {
            PlaylistScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
