package au.edu.jcu.zhangfan.primoria.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import au.edu.jcu.zhangfan.primoria.ui.home.HomeRoute as HomeScreenRoute
import au.edu.jcu.zhangfan.primoria.ui.onboarding.OnboardingRoute as OnboardingScreenRoute
import au.edu.jcu.zhangfan.primoria.ui.privacy.MyDataRoute as MyDataScreenRoute
import au.edu.jcu.zhangfan.primoria.ui.quiz.QuizRoute as QuizScreenRoute
import au.edu.jcu.zhangfan.primoria.ui.settings.SettingsRoute as SettingsScreenRoute
import au.edu.jcu.zhangfan.primoria.ui.stats.StatsRoute as StatsScreenRoute

@Composable
fun PrimoriaNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier,
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable<OnboardingRoute> {
            OnboardingScreenRoute(
                onFinished = {
                    navController.navigate(MainGraph) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                },
            )
        }

        navigation<MainGraph>(startDestination = HomeRoute) {
            composable<HomeRoute> {
                HomeScreenRoute(
                    onStartQuiz = { topic -> navController.navigate(QuizRoute(topic)) },
                    onStartSavedQuiz = { id, title -> navController.navigate(QuizRoute(title, id)) },
                    onOpenStats = { navController.navigateToTopLevel(TopLevelDestination.STATS) },
                    onOpenSettings = { navController.navigateToTopLevel(TopLevelDestination.SETTINGS) },
                )
            }
            composable<StatsRoute> {
                StatsScreenRoute(onStartQuiz = { topic -> navController.navigate(QuizRoute(topic.title, topic.id)) })
            }
            composable<SettingsRoute> {
                SettingsScreenRoute(onOpenMyData = { navController.navigate(MyDataRoute) })
            }
            composable<MyDataRoute> {
                MyDataScreenRoute(onBack = { navController.popBackStack() })
            }
            composable<QuizRoute>(
                deepLinks = listOf(navDeepLink<QuizRoute>(basePath = QUIZ_DEEP_LINK_BASE)),
            ) { entry ->
                val quiz = entry.toRoute<QuizRoute>()
                QuizScreenRoute(
                    onClose = { navController.popBackStack() },
                    onTryTopic = { topic ->
                        // Replace the finished quiz so Back returns home rather than to old results.
                        navController.navigate(quiz.copy(topic = topic)) {
                            popUpTo<QuizRoute> { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

/**
 * Standard top-level navigation: one copy of each tab on the back stack, with each tab's
 * scroll and state saved and restored when the learner switches back.
 */
fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
