package au.edu.jcu.zhangfan.primoria.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

private val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

private val PrimoriaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Access to Primoria-specific colour roles: `PrimoriaTheme.colors.effort`. */
object PrimoriaTheme {
    val colors: PrimoriaExtendedColors
        @Composable @ReadOnlyComposable get() = LocalExtendedColors.current
}

@Composable
fun PrimoriaTheme(
    darkTheme: Boolean = false,
    largeText: Boolean = false,
    content: @Composable () -> Unit,
) {
    // "Larger text" scales on top of the system font size rather than replacing it.
    val density = LocalDensity.current
    val scaledDensity = if (largeText) Density(density.density, density.fontScale * LARGE_TEXT_SCALE) else density
    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalExtendedColors provides if (darkTheme) DarkExtended else LightExtended,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = PrimoriaTypography,
            shapes = PrimoriaShapes,
            content = content,
        )
    }
}

private const val LARGE_TEXT_SCALE = 1.15f
