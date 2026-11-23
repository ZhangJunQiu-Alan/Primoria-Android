package au.edu.jcu.zhangfan.primoria.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import au.edu.jcu.zhangfan.primoria.domain.logic.StreakSummary

/**
 * Commitment 04 — the weekly goal is framed as a rhythm to aim for, never as something
 * the learner is about to lose.
 */
@Composable
fun WeeklyGoalCard(streak: StreakSummary, modifier: Modifier = Modifier) {
    val done = streak.activeDaysThisWeek.coerceAtMost(streak.weeklyGoal)
    val headline = if (streak.goalMetThisWeek) {
        "Weekly goal reached — nice rhythm."
    } else {
        "This week: $done of ${streak.weeklyGoal} active days"
    }
    val streakText = when (streak.weeks) {
        0 -> "Your first goal week starts whenever you're ready."
        1 -> "1 goal week so far"
        else -> "${streak.weeks} goal weeks in a row"
    }
    val freezeText = "${streak.freezesLeft} freezes available for busy weeks"

    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = listOf(headline, streakText, freezeText).joinToString(" ") { it.trimEnd('.') + "." } },
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.secondary)
                Text(headline, style = MaterialTheme.typography.titleSmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(streak.weeklyGoal) { index ->
                    val filled = index < done
                    Icon(
                        imageVector = if (filled) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (filled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Text(streakText, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    Icons.Outlined.AcUnit,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    freezeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
