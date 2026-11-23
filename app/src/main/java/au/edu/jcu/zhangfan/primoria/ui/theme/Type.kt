package au.edu.jcu.zhangfan.primoria.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import au.edu.jcu.zhangfan.primoria.R

/** Plus Jakarta Sans (SIL Open Font License), bundled so it works offline. */
val JakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double) = TextStyle(
    fontFamily = JakartaSans,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking.sp,
)

internal val PrimoriaTypography = Typography(
    displayLarge = style(40, 46, FontWeight.Bold, -0.8),
    displayMedium = style(32, 38, FontWeight.Bold, -0.5),
    displaySmall = style(28, 34, FontWeight.Bold, -0.4),
    headlineLarge = style(24, 30, FontWeight.Bold, -0.3),
    headlineMedium = style(21, 28, FontWeight.SemiBold, -0.2),
    headlineSmall = style(18, 24, FontWeight.SemiBold, -0.1),
    titleLarge = style(17, 23, FontWeight.SemiBold, 0.0),
    titleMedium = style(15, 21, FontWeight.Medium, 0.1),
    titleSmall = style(14, 20, FontWeight.SemiBold, 0.1),
    bodyLarge = style(16, 24, FontWeight.Normal, 0.1),
    bodyMedium = style(14, 21, FontWeight.Normal, 0.15),
    bodySmall = style(12, 18, FontWeight.Normal, 0.2),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.1),
    labelMedium = style(12, 16, FontWeight.SemiBold, 0.3),
    labelSmall = style(11, 14, FontWeight.Medium, 0.4),
)

/** Tabular numerals keep XP, percentages and counters from jittering as they change. */
fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")
