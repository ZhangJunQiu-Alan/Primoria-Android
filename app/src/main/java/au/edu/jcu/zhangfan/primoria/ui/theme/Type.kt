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
    displayLarge = style(34, 40, FontWeight.Bold, -0.8),
    displayMedium = style(28, 34, FontWeight.Bold, -0.6),
    displaySmall = style(24, 30, FontWeight.Bold, -0.5),
    headlineLarge = style(22, 28, FontWeight.Bold, -0.4),
    headlineMedium = style(19, 26, FontWeight.SemiBold, -0.2),
    headlineSmall = style(16, 22, FontWeight.SemiBold, -0.1),
    titleLarge = style(15, 21, FontWeight.SemiBold, -0.1),
    titleMedium = style(14, 20, FontWeight.Medium, 0.0),
    titleSmall = style(13, 18, FontWeight.SemiBold, 0.0),
    bodyLarge = style(15, 22, FontWeight.Normal, 0.0),
    bodyMedium = style(14, 20, FontWeight.Normal, 0.0),
    bodySmall = style(12, 17, FontWeight.Normal, 0.1),
    labelLarge = style(14, 20, FontWeight.SemiBold, 0.0),
    labelMedium = style(12, 16, FontWeight.SemiBold, 0.2),
    labelSmall = style(11, 14, FontWeight.Medium, 0.4),
)

/** Tabular numerals keep XP, percentages and counters from jittering as they change. */
fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = "tnum")
