package au.edu.jcu.zhangfan.primoria.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import au.edu.jcu.zhangfan.primoria.ui.navigation.MainGraph
import au.edu.jcu.zhangfan.primoria.ui.navigation.OnboardingRoute
import au.edu.jcu.zhangfan.primoria.ui.navigation.PrimoriaNavHost
import au.edu.jcu.zhangfan.primoria.ui.navigation.TopLevelDestination
import au.edu.jcu.zhangfan.primoria.ui.navigation.navigateToTopLevel

@Composable
fun PrimoriaApp(
    appState: AppState,
    widthSizeClass: WindowWidthSizeClass,
) {
    when (appState) {
        AppState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        is AppState.Ready -> ReadyApp(
            onboardingComplete = appState.settings.onboardingComplete,
            widthSizeClass = widthSizeClass,
        )
    }
}

@Composable
private fun ReadyApp(onboardingComplete: Boolean, widthSizeClass: WindowWidthSizeClass) {
    val navController = rememberNavController()
    // Fixed once per composition: later changes are handled by the effect below.
    val startDestination: Any = remember { if (onboardingComplete) MainGraph else OnboardingRoute }

    // "Delete everything" resets onboarding, so send the learner back to the start with a clean stack.
    LaunchedEffect(onboardingComplete) {
        if (!onboardingComplete && navController.currentDestination?.hasRoute<OnboardingRoute>() == false) {
            navController.navigate(OnboardingRoute) { popUpTo(0) { inclusive = true } }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showNavigation = TopLevelDestination.entries.any { currentDestination.isOn(it) }
    val useRail = widthSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        bottomBar = {
            if (showNavigation && !useRail) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination.isOn(destination)
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = { Icon(if (selected) destination.selectedIcon else destination.unselectedIcon, null) },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (showNavigation && useRail) {
                NavigationRail {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination.isOn(destination)
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navController.navigateToTopLevel(destination) },
                            icon = { Icon(if (selected) destination.selectedIcon else destination.unselectedIcon, null) },
                            label = { Text(destination.label) },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                    }
                }
            }
            PrimoriaNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun NavDestination?.isOn(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
