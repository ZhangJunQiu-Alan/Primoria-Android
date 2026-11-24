package au.edu.jcu.zhangfan.primoria.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// "Quiet editorial study": near-neutral paper, white cards, one indigo for actions.
// Teal only ever means mastery and amber only ever means effort.

internal val LightColors = lightColorScheme(
    primary = Color(0xFF3730A3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAEAFB),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF52525B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAEAFB),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = Color(0xFF0F766E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDF3EF),
    onTertiaryContainer = Color(0xFF053B36),
    error = Color(0xFFB42318),
    onError = Color.White,
    errorContainer = Color(0xFFFDECEA),
    onErrorContainer = Color(0xFF55160C),
    background = Color(0xFFF8F7F4),
    onBackground = Color(0xFF18181B),
    surface = Color(0xFFF8F7F4),
    onSurface = Color(0xFF18181B),
    surfaceVariant = Color(0xFFEFEEEA),
    onSurfaceVariant = Color(0xFF5B5B63),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F2EF),
    surfaceContainer = Color(0xFFEFEEEA),
    surfaceContainerHigh = Color(0xFFEAE9E5),
    surfaceContainerHighest = Color(0xFFE4E3DF),
    outline = Color(0xFF8A8A93),
    outlineVariant = Color(0xFFE5E3DC),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF262555),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF18181B),
    secondaryContainer = Color(0xFF262555),
    onSecondaryContainer = Color(0xFFE0E7FF),
    tertiary = Color(0xFF5EEAD4),
    onTertiary = Color(0xFF042F2B),
    tertiaryContainer = Color(0xFF0F2F2C),
    onTertiaryContainer = Color(0xFFCCFBF1),
    error = Color(0xFFFDA29B),
    onError = Color(0xFF55160C),
    errorContainer = Color(0xFF3D1512),
    onErrorContainer = Color(0xFFFEE4E2),
    background = Color(0xFF0F1117),
    onBackground = Color(0xFFEDEDF0),
    surface = Color(0xFF0F1117),
    onSurface = Color(0xFFEDEDF0),
    surfaceVariant = Color(0xFF1C1F29),
    onSurfaceVariant = Color(0xFF9E9EA8),
    surfaceContainerLowest = Color(0xFF0B0D12),
    surfaceContainerLow = Color(0xFF141720),
    surfaceContainer = Color(0xFF1A1D27),
    surfaceContainerHigh = Color(0xFF21242F),
    surfaceContainerHighest = Color(0xFF292D39),
    outline = Color(0xFF6B6F7B),
    outlineVariant = Color(0xFF262A35),
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
    effort = Color(0xFFB45309),
    onEffort = Color.White,
    effortContainer = Color(0xFFFBF0DF),
    onEffortContainer = Color(0xFF5A2A04),
    mastery = Color(0xFF0F766E),
    onMastery = Color.White,
    masteryContainer = Color(0xFFDDF3EF),
    onMasteryContainer = Color(0xFF053B36),
    cardSurface = Color(0xFFFFFFFF),
    hairline = Color(0xFFE8E6E0),
    heroGradient = listOf(Color(0xFFF8F7F4), Color(0xFFF8F7F4)),
    quizBackdrop = listOf(Color(0xFFF8F7F4), Color(0xFFF8F7F4)),
    effortRing = listOf(Color(0xFFB45309), Color(0xFFD97706)),
    masteryRing = listOf(Color(0xFF0F766E), Color(0xFF14B8A6)),
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
    effort = Color(0xFFF5A524),
    onEffort = Color(0xFF3A1D02),
    effortContainer = Color(0xFF2E2111),
    onEffortContainer = Color(0xFFFDE3B8),
    mastery = Color(0xFF5EEAD4),
    onMastery = Color(0xFF042F2B),
    masteryContainer = Color(0xFF0F2F2C),
    onMasteryContainer = Color(0xFFCCFBF1),
    cardSurface = Color(0xFF171A23),
    hairline = Color(0xFF242833),
    heroGradient = listOf(Color(0xFF0F1117), Color(0xFF0F1117)),
    quizBackdrop = listOf(Color(0xFF0F1117), Color(0xFF0F1117)),
    effortRing = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
    masteryRing = listOf(Color(0xFF14B8A6), Color(0xFF5EEAD4)),
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
