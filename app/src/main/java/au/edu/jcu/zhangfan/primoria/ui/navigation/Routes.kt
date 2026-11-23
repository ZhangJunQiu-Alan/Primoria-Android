package au.edu.jcu.zhangfan.primoria.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Type-safe destinations for the Navigation component.

@Serializable data object OnboardingRoute

/** Nested graph holding everything after onboarding. */
@Serializable data object MainGraph

@Serializable data object HomeRoute

@Serializable data object StatsRoute

@Serializable data object SettingsRoute

@Serializable data object MyDataRoute

/** [topic] is free text typed by the learner; also reachable via `primoria://quiz/{topic}`. */
@Serializable data class QuizRoute(val topic: String)

const val QUIZ_DEEP_LINK_BASE = "primoria://quiz"

enum class TopLevelDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: Any,
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, HomeRoute),
    STATS("Progress", Icons.Filled.ShowChart, Icons.AutoMirrored.Outlined.ShowChart, StatsRoute),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, SettingsRoute),
}
