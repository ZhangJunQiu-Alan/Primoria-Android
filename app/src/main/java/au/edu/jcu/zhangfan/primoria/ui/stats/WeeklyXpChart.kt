package au.edu.jcu.zhangfan.primoria.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import au.edu.jcu.zhangfan.primoria.data.repository.DayActivity
import au.edu.jcu.zhangfan.primoria.ui.components.animatedProgress
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular
import java.time.format.TextStyle
import java.util.Locale

/**
 * Seven rounded bars (today solid, earlier days lighter), each labelled with its value, drawn against a
 * dashed line marking the daily XP cap. TalkBack reads a plain-language summary instead.
 */
@Composable
fun WeeklyXpChart(week: List<DayActivity>, dailyCap: Int, modifier: Modifier = Modifier) {
    val effort = PrimoriaTheme.colors.effort
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val capColor = MaterialTheme.colorScheme.outlineVariant
    val baseline = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = MaterialTheme.typography.labelSmall.tabular()
    val capStyle = labelStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val valueStyle = labelStyle.copy(color = effort)
    val measurer = rememberTextMeasurer()
    val grow = animatedProgress(1f)
    val description = week.joinToString(separator = ". ", prefix = "XP over the last seven days, daily limit $dailyCap: ") {
        "${it.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${it.xp} XP"
    }

    Column(modifier.clearAndSetSemantics { contentDescription = description }) {
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val labelSpace = 18.dp.toPx()
            val chartTop = labelSpace
            val chartHeight = size.height - chartTop
            val slot = size.width / week.size
            val barWidth = minOf(slot * 0.42f, 22.dp.toPx())
            val maxValue = dailyCap.coerceAtLeast(1).toFloat()
            val minBar = 8.dp.toPx()

            drawLine(
                color = capColor,
                start = Offset(0f, chartTop),
                end = Offset(size.width, chartTop),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
            drawLine(baseline, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            val cap = measurer.measure("$dailyCap", capStyle)
            drawText(cap, topLeft = Offset(size.width - cap.size.width, chartTop - cap.size.height - 2.dp.toPx()))

            week.forEachIndexed { index, day ->
                val left = index * slot + (slot - barWidth) / 2
                // Empty days get a short neutral stub, never a full-height track that reads as "full".
                if (day.xp <= 0) {
                    val stub = 4.dp.toPx()
                    drawRoundRect(track, Offset(left, size.height - stub), Size(barWidth, stub), CornerRadius(stub / 2))
                    return@forEachIndexed
                }
                val height = (chartHeight * (day.xp / maxValue).coerceIn(0f, 1f) * grow).coerceAtLeast(minBar)
                val top = size.height - height
                val corner = minOf(barWidth / 2, 6.dp.toPx())
                drawRoundRect(
                    color = if (index == week.lastIndex) effort else effort.copy(alpha = 0.55f),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, height),
                    cornerRadius = CornerRadius(corner, corner),
                )
                val value = measurer.measure("${day.xp}", valueStyle)
                val labelTop = (top - value.size.height - 4.dp.toPx()).coerceAtLeast(chartTop + 2.dp.toPx())
                drawText(value, topLeft = Offset(left + (barWidth - value.size.width) / 2, labelTop))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            week.forEachIndexed { index, day ->
                Text(
                    day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == week.lastIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
