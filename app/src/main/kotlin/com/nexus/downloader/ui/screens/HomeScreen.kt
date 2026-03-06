package com.nexus.downloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.nexus.downloader.ui.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NEXUS",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Next-Gen Download Manager",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate(Screen.Downloads.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("My Downloads")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate(Screen.Browser.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Browser")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { navController.navigate(Screen.Settings.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }
    }
}
