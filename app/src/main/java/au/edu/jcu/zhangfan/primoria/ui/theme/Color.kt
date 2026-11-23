package au.edu.jcu.zhangfan.primoria.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Warm "paper" light theme and obsidian-slate dark theme with an indigo core.

internal val LightColors = lightColorScheme(
    primary = Color(0xFF4338CA),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E5FF),
    onPrimaryContainer = Color(0xFF1E1B5E),
    secondary = Color(0xFF4E5566),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4E5FF),
    onSecondaryContainer = Color(0xFF1E1B5E),
    tertiary = Color(0xFF0B7A69),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD7F5EE),
    onTertiaryContainer = Color(0xFF053B33),
    error = Color(0xFFB91C3B),
    onError = Color.White,
    errorContainer = Color(0xFFFDE4E9),
    onErrorContainer = Color(0xFF5B0B1B),
    background = Color(0xFFF7F6F2),
    onBackground = Color(0xFF151821),
    surface = Color(0xFFF7F6F2),
    onSurface = Color(0xFF151821),
    surfaceVariant = Color(0xFFEBE8E0),
    onSurfaceVariant = Color(0xFF4E5566),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F0EA),
    surfaceContainer = Color(0xFFEBE8E0),
    surfaceContainerHigh = Color(0xFFE3DFD5),
    surfaceContainerHighest = Color(0xFFDAD5C9),
    outline = Color(0xFF72788A),
    outlineVariant = Color(0xFFDBD7CC),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFA5A8FF),
    onPrimary = Color(0xFF16144A),
    primaryContainer = Color(0xFF2A2766),
    onPrimaryContainer = Color(0xFFE4E5FF),
    secondary = Color(0xFFA2ACC0),
    onSecondary = Color(0xFF151821),
    secondaryContainer = Color(0xFF2A2766),
    onSecondaryContainer = Color(0xFFE4E5FF),
    tertiary = Color(0xFF4DE1C1),
    onTertiary = Color(0xFF03332A),
    tertiaryContainer = Color(0xFF0E3B33),
    onTertiaryContainer = Color(0xFFB8F5E7),
    error = Color(0xFFFF8CA1),
    onError = Color(0xFF4A0716),
    errorContainer = Color(0xFF591222),
    onErrorContainer = Color(0xFFFFD6DE),
    background = Color(0xFF0E1117),
    onBackground = Color(0xFFF1F3F7),
    surface = Color(0xFF0E1117),
    onSurface = Color(0xFFF1F3F7),
    surfaceVariant = Color(0xFF1B202C),
    onSurfaceVariant = Color(0xFFA2ACC0),
    surfaceContainerLowest = Color(0xFF090B10),
    surfaceContainerLow = Color(0xFF141821),
    surfaceContainer = Color(0xFF1B202C),
    surfaceContainerHigh = Color(0xFF232938),
    surfaceContainerHighest = Color(0xFF2C3346),
    outline = Color(0xFF667085),
    outlineVariant = Color(0xFF2A3142),
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
    effort = Color(0xFFB84B09),
    onEffort = Color.White,
    effortContainer = Color(0xFFFFECD9),
    onEffortContainer = Color(0xFF5C2300),
    mastery = Color(0xFF0B7A69),
    onMastery = Color.White,
    masteryContainer = Color(0xFFD7F5EE),
    onMasteryContainer = Color(0xFF053B33),
    cardSurface = Color(0xFFFFFFFF),
    hairline = Color(0xB3DBD7CC),
    heroGradient = listOf(Color(0xFFE4E3FF), Color(0xFFF7F6F2), Color(0xFFFFEAD6)),
    quizBackdrop = listOf(Color(0xFFEDECFC), Color(0xFFF7F6F2)),
    effortRing = listOf(Color(0xFFD95F12), Color(0xFFF58220), Color(0xFFFBBF24)),
    masteryRing = listOf(Color(0xFF0B7A69), Color(0xFF14B8A6), Color(0xFF4DE1C1)),
    subjects = listOf(
        SubjectColors(Color(0xFFE6EEFF), Color(0xFF173B8C)),
        SubjectColors(Color(0xFFE3F5EC), Color(0xFF114E36)),
        SubjectColors(Color(0xFFFFF0E0), Color(0xFF733407)),
        SubjectColors(Color(0xFFF3E8FF), Color(0xFF4F1D7A)),
        SubjectColors(Color(0xFFFDE8EE), Color(0xFF7A1636)),
        SubjectColors(Color(0xFFE2F4FC), Color(0xFF0E4B68)),
        SubjectColors(Color(0xFFFBF3DA), Color(0xFF614305)),
        SubjectColors(Color(0xFFEAECEF), Color(0xFF273142)),
    ),
    isDark = false,
)

internal val DarkExtended = PrimoriaExtendedColors(
    effort = Color(0xFFFFAD5C),
    onEffort = Color(0xFF3D1800),
    effortContainer = Color(0xFF4A240B),
    onEffortContainer = Color(0xFFFFD9B3),
    mastery = Color(0xFF4DE1C1),
    onMastery = Color(0xFF03332A),
    masteryContainer = Color(0xFF0E3B33),
    onMasteryContainer = Color(0xFFB8F5E7),
    cardSurface = Color(0xFF1B202C),
    hairline = Color(0x14FFFFFF),
    heroGradient = listOf(Color(0xFF242256), Color(0xFF141821), Color(0xFF2E1E14)),
    quizBackdrop = listOf(Color(0xFF151935), Color(0xFF0E1117)),
    effortRing = listOf(Color(0xFFF58220), Color(0xFFFFAD5C), Color(0xFFFDE047)),
    masteryRing = listOf(Color(0xFF14B8A6), Color(0xFF4DE1C1), Color(0xFF8CF5DD)),
    subjects = listOf(
        SubjectColors(Color(0xFF182544), Color(0xFFB8D0FF)),
        SubjectColors(Color(0xFF132E24), Color(0xFFA7E8CB)),
        SubjectColors(Color(0xFF362112), Color(0xFFFFD0A6)),
        SubjectColors(Color(0xFF2B1B3D), Color(0xFFDEC2FF)),
        SubjectColors(Color(0xFF381823), Color(0xFFFFC2D4)),
        SubjectColors(Color(0xFF122B38), Color(0xFFA8E0F7)),
        SubjectColors(Color(0xFF332911), Color(0xFFF5DF99)),
        SubjectColors(Color(0xFF212733), Color(0xFFCED6E3)),
    ),
    isDark = true,
)
