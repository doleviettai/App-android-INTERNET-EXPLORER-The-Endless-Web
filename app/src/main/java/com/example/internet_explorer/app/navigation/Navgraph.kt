package com.example.internet_explorer.app.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.internet_explorer.app.feature.browser.BrowserScreen
import com.example.internet_explorer.app.feature.home.HomeScreen
import com.example.internet_explorer.app.feature.inbox.InboxScreen
import com.example.internet_explorer.app.feature.notebook.NotebookScreen
import com.example.internet_explorer.app.feature.puzzle.PuzzleScreen
import com.example.internet_explorer.app.ui.components.BlinkingCursor
import com.example.internet_explorer.app.ui.components.IntegrityStaticOverlay
import com.example.internet_explorer.app.ui.theme.*

private sealed class BottomNavItem(val route: String, val label: String) {
    data object Home : BottomNavItem("home", "Browser")
    data object Notebook : BottomNavItem("notebook", "Notebook")
    data object Puzzle : BottomNavItem("puzzle", "Puzzle")
    data object Inbox : BottomNavItem("inbox", "Inbox")
}

private val bottomItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Notebook,
    BottomNavItem.Puzzle,
    BottomNavItem.Inbox
)

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(BgSurface)
                        .border(BorderStroke(1.dp, BorderAscii))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🟢 SYSTEM ONLINE // LEVEL: UNVERIFIED",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentTerminal,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "NEXUS-7",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentTerminal,
                        fontWeight = FontWeight.Bold
                    )
                    BlinkingCursor(modifier = Modifier.padding(start = 4.dp))
                }
            },
            bottomBar = {
                if (bottomItems.any { it.route == currentRoute }) {
                    // Custom Bottom Navigation Bar phẳng theo GDD mục 12 & 8
                    Column {
                        // Đường viền ngăn cách phía trên của Bottom Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderAscii)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(BgSurface),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomItems.forEach { item ->
                                val selected = currentRoute == item.route
                                val color = if (selected) AccentTerminal else TextMuted
                                val textLabel = if (selected) "[ ${item.label.toUpperCase()} ]" else "  ${item.label.toUpperCase()}  "

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = textLabel,
                                        color = color,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(onWebsiteClick = { entityId -> navController.navigate("browser/$entityId") })
                }
                composable("browser/{entityId}") { entry ->
                    val entityId = entry.arguments?.getString("entityId") ?: return@composable
                    BrowserScreen(entityId = entityId)
                }
                composable(BottomNavItem.Notebook.route) { NotebookScreen() }
                composable(BottomNavItem.Puzzle.route) {
                    PuzzleScreen(
                        onViewNotebook = {
                            navController.navigate(BottomNavItem.Notebook.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(BottomNavItem.Inbox.route) { InboxScreen() }
            }
        }

        // Tích hợp Lớp phủ quét CRT động & chớp glitch tăng dần theo tiến độ điều tra
        IntegrityStaticOverlay(modifier = Modifier.fillMaxSize())
    }
}