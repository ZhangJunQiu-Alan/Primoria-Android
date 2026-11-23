package au.edu.jcu.zhangfan.primoria.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import au.edu.jcu.zhangfan.primoria.data.repository.DayActivity
import java.time.format.TextStyle
import java.util.Locale

/** Seven-day XP bars drawn against the daily cap, with a spoken summary for TalkBack. */
@Composable
fun WeeklyXpChart(week: List<DayActivity>, dailyCap: Int, modifier: Modifier = Modifier) {
    val barColor = MaterialTheme.colorScheme.secondary
    val emptyColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val capColor = MaterialTheme.colorScheme.outline
    val description = week.joinToString(separator = ". ", prefix = "XP over the last seven days: ") {
        "${it.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${it.xp} XP"
    }

    Column(modifier.clearAndSetSemantics { contentDescription = description }) {
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val slot = size.width / week.size
            val barWidth = slot * 0.55f
            val maxValue = dailyCap.coerceAtLeast(1).toFloat()

            week.forEachIndexed { index, day ->
                val left = index * slot + (slot - barWidth) / 2
                // A thin placeholder keeps empty days visible without implying failure.
                val fraction = (day.xp / maxValue).coerceIn(0f, 1f)
                val barHeight = (size.height * fraction).coerceAtLeast(4.dp.toPx())
                drawRoundRect(
                    color = if (day.xp > 0) barColor else emptyColor,
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                )
            }
            drawLine(
                color = capColor,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            week.forEach { day ->
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
