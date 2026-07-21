package com.example.internet_explorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.internet_explorer.app.data.repository.GameStateRepository
import com.example.internet_explorer.app.feature.cleanup.CleanupScreen
import com.example.internet_explorer.app.feature.onboarding.OnboardingPreferences
import com.example.internet_explorer.app.feature.onboarding.OnboardingScreen
import com.example.internet_explorer.app.navigation.AppNavGraph
import com.example.internet_explorer.app.ui.components.BootSequenceScreen
import com.example.internet_explorer.app.ui.theme.InternetExplorerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GameStateRepository.init(applicationContext)

        setContent {
            InternetExplorerTheme {
                var showBoot by remember { mutableStateOf(true) }
                var showOnboarding by remember {
                    mutableStateOf(!OnboardingPreferences.hasCompletedOnboarding(applicationContext))
                }
                // Trace ngưỡng nguy hiểm (gameplay-mechanics-phase2.md) -- khi true, CHẶN
                // TOÀN APP bằng CleanupScreen bất kể đang ở route nào trong AppNavGraph.
                val isLockedOut by GameStateRepository.isLockedOut.collectAsState()

                when {
                    showBoot -> BootSequenceScreen(onFinished = { showBoot = false })
                    showOnboarding -> OnboardingScreen(onFinished = {
                        OnboardingPreferences.setCompleted(applicationContext)
                        showOnboarding = false
                    })
                    isLockedOut -> CleanupScreen()
                    else -> AppNavGraph()
                }
            }
        }
    }
}