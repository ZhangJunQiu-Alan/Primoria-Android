package au.edu.jcu.zhangfan.primoria.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

internal val LightColors = lightColorScheme(
    primary = Color(0xFF3835BB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBE9FC),
    onPrimaryContainer = Color(0xFF292667),
    secondary = Color(0xFF575D6D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBE9FC),
    onSecondaryContainer = Color(0xFF292667),
    tertiary = Color(0xFF145D43),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE5F2C9),
    onTertiaryContainer = Color(0xFF145D43),
    error = Color(0xFF9A3F38),
    onError = Color.White,
    errorContainer = Color(0xFFFAE6DF),
    onErrorContainer = Color(0xFF78332E),
    background = Color(0xFFF8F7EE),
    onBackground = Color(0xFF202440),
    surface = Color(0xFFF8F7EE),
    onSurface = Color(0xFF202440),
    surfaceVariant = Color(0xFFEEEEE7),
    onSurfaceVariant = Color(0xFF575D6D),
    surfaceContainerLowest = Color(0xFFFFFFF9),
    surfaceContainerLow = Color(0xFFF3F2EA),
    surfaceContainer = Color(0xFFEEEEE7),
    surfaceContainerHigh = Color(0xFFE9EAE3),
    surfaceContainerHighest = Color(0xFFE4E5DC),
    outline = Color(0xFF797D8D),
    outlineVariant = Color(0xFFDEDFD9),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFBAB5FF),
    onPrimary = Color(0xFF1D1A48),
    primaryContainer = Color(0xFF2B2A58),
    onPrimaryContainer = Color(0xFFE1DEFF),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF18181B),
    secondaryContainer = Color(0xFF2B2A58),
    onSecondaryContainer = Color(0xFFE1DEFF),
    tertiary = Color(0xFFAADAAD),
    onTertiary = Color(0xFF042F2B),
    tertiaryContainer = Color(0xFF233D31),
    onTertiaryContainer = Color(0xFFD5F1D0),
    error = Color(0xFFFDA29B),
    onError = Color(0xFF55160C),
    errorContainer = Color(0xFF3D1512),
    onErrorContainer = Color(0xFFFEE4E2),
    background = Color(0xFF131522),
    onBackground = Color(0xFFF1F0FA),
    surface = Color(0xFF131522),
    onSurface = Color(0xFFF1F0FA),
    surfaceVariant = Color(0xFF1C1F29),
    onSurfaceVariant = Color(0xFFB5B8CA),
    surfaceContainerLowest = Color(0xFF0B0D12),
    surfaceContainerLow = Color(0xFF141720),
    surfaceContainer = Color(0xFF1A1D27),
    surfaceContainerHigh = Color(0xFF21242F),
    surfaceContainerHighest = Color(0xFF292D39),
    outline = Color(0xFF6B6F7B),
    outlineVariant = Color(0xFF32364A),
)

/**
 * Roles Material 3 has no slot for. Effort (warm) and mastery (cool) never share a colour,
 * so learners can tell "showing up" from "understanding" at a glance.
 */
@Immutable
data class PrimoriaExtendedColors(
    val effort: Color,
    val onEffort: Color,
    val effortContainer: Color,
    val onEffortContainer: Color,
    val mastery: Color,
    val onMastery: Color,
    val masteryContainer: Color,
    val onMasteryContainer: Color,
    val cardSurface: Color,
    val hairline: Color,
    val heroGradient: List<Color>,
    val quizBackdrop: List<Color>,
    val effortRing: List<Color>,
    val masteryRing: List<Color>,
    val subjects: List<SubjectColors>,
    val isDark: Boolean,
)

@Immutable
data class SubjectColors(val container: Color, val content: Color)

internal val LightExtended = PrimoriaExtendedColors(
    effort = Color(0xFFA45416),
    onEffort = Color.White,
    effortContainer = Color(0xFFFFEDCC),
    onEffortContainer = Color(0xFF783A11),
    mastery = Color(0xFF145D43),
    onMastery = Color.White,
    masteryContainer = Color(0xFFE5F2C9),
    onMasteryContainer = Color(0xFF145D43),
    cardSurface = Color(0xFFFFFFF9),
    hairline = Color(0xFFDEDFD9),
    heroGradient = listOf(Color(0xFFF8F7EE), Color(0xFFF8F7EE)),
    quizBackdrop = listOf(Color(0xFFF8F7EE), Color(0xFFF8F7EE)),
    effortRing = listOf(Color(0xFFA45416), Color(0xFFD97706)),
    masteryRing = listOf(Color(0xFF145D43), Color(0xFF14B8A6)),
    subjects = listOf(
        SubjectColors(Color(0xFFE7EBFB), Color(0xFF2B3A8F)),
        SubjectColors(Color(0xFFE2F2EA), Color(0xFF1E5B3E)),
        SubjectColors(Color(0xFFFBEEDF), Color(0xFF8A4510)),
        SubjectColors(Color(0xFFF0E9FB), Color(0xFF5B2E91)),
        SubjectColors(Color(0xFFFBE8EC), Color(0xFF8C2541)),
        SubjectColors(Color(0xFFE2F1F8), Color(0xFF155873)),
        SubjectColors(Color(0xFFF7F0DA), Color(0xFF6B4D0B)),
        SubjectColors(Color(0xFFECECEE), Color(0xFF3F3F46)),
    ),
    isDark = false,
)

internal val DarkExtended = PrimoriaExtendedColors(
    effort = Color(0xFFF2BB74),
    onEffort = Color(0xFF3A1D02),
    effortContainer = Color(0xFF3F2D1C),
    onEffortContainer = Color(0xFFFFE3B3),
    mastery = Color(0xFFAADAAD),
    onMastery = Color(0xFF042F2B),
    masteryContainer = Color(0xFF233D31),
    onMasteryContainer = Color(0xFFD5F1D0),
    cardSurface = Color(0xFF1D2131),
    hairline = Color(0xFF32364A),
    heroGradient = listOf(Color(0xFF131522), Color(0xFF131522)),
    quizBackdrop = listOf(Color(0xFF131522), Color(0xFF131522)),
    effortRing = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
    masteryRing = listOf(Color(0xFF14B8A6), Color(0xFFAADAAD)),
    subjects = listOf(
        SubjectColors(Color(0xFF1D2340), Color(0xFFC0CCFF)),
        SubjectColors(Color(0xFF16291F), Color(0xFFA7DFC0)),
        SubjectColors(Color(0xFF2E2216), Color(0xFFF7C99A)),
        SubjectColors(Color(0xFF261D36), Color(0xFFD9C4FA)),
        SubjectColors(Color(0xFF2F1A21), Color(0xFFF5BDCB)),
        SubjectColors(Color(0xFF15262F), Color(0xFFA9DCF0)),
        SubjectColors(Color(0xFF2A2414), Color(0xFFEBD596)),
        SubjectColors(Color(0xFF1F2129), Color(0xFFD0D0D8)),
    ),
    isDark = true,
)
