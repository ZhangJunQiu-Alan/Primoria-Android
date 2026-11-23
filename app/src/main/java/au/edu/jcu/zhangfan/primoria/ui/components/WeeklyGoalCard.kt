package au.edu.jcu.zhangfan.primoria.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import au.edu.jcu.zhangfan.primoria.domain.logic.StreakSummary
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular

/** Plain-language weekly rhythm text, shared by Home and Progress. */
object WeeklyGoalCopy {
    fun headline(streak: StreakSummary): String {
        val done = streak.activeDaysThisWeek.coerceAtMost(streak.weeklyGoal)
        return if (streak.goalMetThisWeek) "Weekly goal reached" else "$done of ${streak.weeklyGoal} days this week"
    }

    fun streakLine(streak: StreakSummary): String = when (streak.weeks) {
        0 -> "Your first goal week starts whenever you're ready"
        1 -> "1 goal week so far"
        else -> "${streak.weeks} goal weeks in a row"
    }

    fun freezeLine(streak: StreakSummary): String = "${streak.freezesLeft} freezes for busy weeks"

    fun spoken(streak: StreakSummary): String =
        listOf(headline(streak), streakLine(streak), freezeLine(streak)).joinToString(" ") { "$it." }
}

/**
 * Commitment 04 — the weekly goal is a rhythm to aim for, never something about to be lost.
 * Ring + three short lines; used inside the Home hero and the Progress screen.
 */
@Composable
fun WeeklyGoalRow(streak: StreakSummary, modifier: Modifier = Modifier, ringSize: Int = 64) {
    val colors = PrimoriaTheme.colors
    val done = streak.activeDaysThisWeek.coerceAtMost(streak.weeklyGoal)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = WeeklyGoalCopy.spoken(streak) },
    ) {
        ProgressRing(
            progress = done / streak.weeklyGoal.toFloat(),
            colors = colors.effortRing,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
            size = ringSize.dp,
            strokeWidth = 7.dp,
        ) {
            Text("$done/${streak.weeklyGoal}", style = MaterialTheme.typography.titleSmall.tabular())
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(WeeklyGoalCopy.headline(streak), style = MaterialTheme.typography.titleLarge)
            Text(WeeklyGoalCopy.streakLine(streak), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.AcUnit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Text(WeeklyGoalCopy.freezeLine(streak), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
