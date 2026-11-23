package au.edu.jcu.zhangfan.primoria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.domain.model.ThemeMode
import au.edu.jcu.zhangfan.primoria.ui.app.AppState
import au.edu.jcu.zhangfan.primoria.ui.app.MainViewModel
import au.edu.jcu.zhangfan.primoria.ui.app.PrimoriaApp
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep the splash screen up until settings are read, so onboarding never flashes.
        installSplashScreen().setKeepOnScreenCondition { viewModel.appState.value is AppState.Loading }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState by viewModel.appState.collectAsStateWithLifecycle()
            val settings = (appState as? AppState.Ready)?.settings
            val darkTheme = when (settings?.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                else -> isSystemInDarkTheme()
            }
            PrimoriaTheme(darkTheme = darkTheme, largeText = settings?.largeText == true) {
                PrimoriaApp(
                    appState = appState,
                    widthSizeClass = calculateWindowSizeClass(this).widthSizeClass,
                )
            }
        }
    }
}
