package com.example.internet_explorer.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.example.internet_explorer.app.ui.components.ScanlineOverlay

private sealed class BottomNavItem(val route: String, val label: String, val emoji: String) {
    data object Home : BottomNavItem("home", "Browser", "🌐")
    data object Notebook : BottomNavItem("notebook", "Notebook", "📓")
    data object Puzzle : BottomNavItem("puzzle", "Puzzle", "🧩")
    data object Inbox : BottomNavItem("inbox", "Inbox", "📧")
}

private val bottomItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Notebook,
    BottomNavItem.Puzzle,
    BottomNavItem.Inbox
)

/**
 * NavGraph đầy đủ cho core loop (mục 5 GDD):
 * Home (danh sách website) -> mở 1 website -> Notebook xem manh mối
 * -> Puzzle giải log -> unlock clue -> quay lại Notebook -> Inbox đọc email secret mới mở.
 *
 * Bottom bar chỉ hiện ở 4 route cấp cao nhất, tự ẩn khi đang xem chi tiết 1 website
 * (route "browser/{entityId}") để có cảm giác "đang đọc trang", giống hành vi trình duyệt thật.
 */
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🟢 NEXUS-7 CONNECTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    BlinkingCursor(modifier = Modifier.padding(start = 4.dp))
                }
            },
            bottomBar = {
                if (bottomItems.any { it.route == currentRoute }) {
                    NavigationBar {
                        bottomItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Text(item.emoji) },
                                label = { Text(item.label) }
                            )
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

        // Hiệu ứng quét CRT phủ toàn màn hình, vẽ sau cùng để nằm trên mọi nội dung.
        // Không chặn thao tác chạm vì Canvas không có modifier clickable/pointerInput.
        ScanlineOverlay(modifier = Modifier.fillMaxSize())
    }
}