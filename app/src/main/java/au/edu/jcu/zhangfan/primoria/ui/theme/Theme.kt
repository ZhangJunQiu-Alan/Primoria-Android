package au.edu.jcu.zhangfan.primoria.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF2B5C8A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = Color(0xFF8A5A00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDB1),
    onSecondaryContainer = Color(0xFF2C1700),
    tertiary = Color(0xFF3F6A4E),
    tertiaryContainer = Color(0xFFC1ECCE),
    onTertiaryContainer = Color(0xFF002111),
    background = Color(0xFFF8F9FC),
    surface = Color(0xFFF8F9FC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA2C9FF),
    onPrimary = Color(0xFF00315B),
    primaryContainer = Color(0xFF0D4471),
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = Color(0xFFFFB955),
    onSecondary = Color(0xFF482A00),
    secondaryContainer = Color(0xFF693F00),
    onSecondaryContainer = Color(0xFFFFDDB1),
    tertiary = Color(0xFFA6D0B3),
    tertiaryContainer = Color(0xFF275138),
    onTertiaryContainer = Color(0xFFC1ECCE),
)

@Composable
fun PrimoriaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    largeText: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    // "Larger text" scales on top of the system font size rather than replacing it.
    val density = LocalDensity.current
    val scaledDensity = if (largeText) Density(density.density, density.fontScale * LARGE_TEXT_SCALE) else density
    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

private const val LARGE_TEXT_SCALE = 1.15f
