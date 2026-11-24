package au.edu.jcu.zhangfan.primoria.ui.stats

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.data.repository.LearningStats
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.DualRings
import au.edu.jcu.zhangfan.primoria.ui.components.EmptyState
import au.edu.jcu.zhangfan.primoria.ui.components.MasteryBadge
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.SectionHeader
import au.edu.jcu.zhangfan.primoria.ui.components.SubjectArt
import au.edu.jcu.zhangfan.primoria.ui.components.WeeklyGoalRow
import au.edu.jcu.zhangfan.primoria.ui.components.animatedProgress
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular
import kotlin.math.roundToInt

@Composable
fun StatsRoute(
    onStartQuiz: (String) -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(state = state, onTopicClick = { onStartQuiz(it.title) })
}

@Composable
fun StatsScreen(state: StatsUiState, onTopicClick: (TopicSummary) -> Unit) {
    ContentColumn(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
        when (state) {
            StatsUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is StatsUiState.Loaded -> StatsContent(state.stats, onTopicClick)
        }
    }
}

@Composable
private fun StatsContent(stats: LearningStats, onTopicClick: (TopicSummary) -> Unit) {
    val colors = PrimoriaTheme.colors
    val averageMastery = stats.studiedTopics.map { it.mastery }.average().takeIf { !it.isNaN() } ?: 0.0
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("Your progress", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
        }
        item { OverviewCard(averageMastery, stats) }
        item {
            PrimoriaCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Effort · last 7 days", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("${stats.xpToday} / ${stats.dailyXpCap} today", style = MaterialTheme.typography.labelMedium.tabular(), color = colors.effort)
                }
                Spacer(Modifier.height(16.dp))
                WeeklyXpChart(stats.week, stats.dailyXpCap)
                Spacer(Modifier.height(12.dp))
                Text(
                    "XP counts correct first answers only and stops at ${stats.dailyXpCap} a day (dashed line), so it measures effort — not time on screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { PrimoriaCard(Modifier.fillMaxWidth(), contentPadding = 16.dp) { WeeklyGoalRow(stats.streak, ringSize = 56) } }
        item { SectionHeader("Knowledge by topic", Modifier.padding(top = 16.dp)) }
        if (stats.studiedTopics.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Insights,
                    title = "Nothing to show yet",
                    body = "Finish your first quiz to see your mastery and accuracy here.",
                )
            }
        } else {
            items(stats.studiedTopics, key = { it.id }) { topic -> TopicMasteryRow(topic, onClick = { onTopicClick(topic) }) }
        }
    }
}

@Composable
private fun OverviewCard(averageMastery: Double, stats: LearningStats) {
    val colors = PrimoriaTheme.colors
    val effortToday = stats.xpToday / stats.dailyXpCap.toFloat()
    val masteryPercent = (averageMastery * 100).roundToInt()
    PrimoriaCard(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = "Average mastery $masteryPercent percent. Effort today ${stats.xpToday} of ${stats.dailyXpCap} XP."
            },
        ) {
            DualRings(
                outerProgress = averageMastery.toFloat(),
                outerColors = colors.masteryRing,
                innerProgress = effortToday,
                innerColors = colors.effortRing,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                size = 112.dp,
                strokeWidth = 11.dp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RingLegend(colors.mastery, "Mastery · outer", "$masteryPercent%", "average across topics")
                RingLegend(colors.effort, "Effort today · inner", "${stats.xpToday} XP", "of ${stats.dailyXpCap} daily limit")
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = colors.hairline)
        Row(Modifier.fillMaxWidth()) {
            MiniStat("${stats.totalXp}", "Total XP", Modifier.weight(1f))
            MiniStat("${(stats.accuracy * 100).roundToInt()}%", "Accuracy", Modifier.weight(1f))
            MiniStat("${stats.studiedTopics.size}", if (stats.studiedTopics.size == 1) "Topic" else "Topics", Modifier.weight(1f))
            MiniStat("${stats.streak.weeks}", if (stats.streak.weeks == 1) "Goal week" else "Goal weeks", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold).tabular())
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun RingLegend(color: Color, label: String, value: String, caption: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp))
        }
        Text(value, style = MaterialTheme.typography.headlineLarge.tabular())
        Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TopicMasteryRow(topic: TopicSummary, onClick: () -> Unit) {
    val subject = SubjectArt.colors(topic.title)
    val progress = animatedProgress(topic.mastery.toFloat())
    PrimoriaCard(onClick = onClick, contentPadding = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(subject.container),
            ) { Icon(SubjectArt.icon(topic.title), null, tint = subject.content, modifier = Modifier.size(20.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(topic.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("${(topic.mastery * 100).roundToInt()}%", style = MaterialTheme.typography.titleSmall.tabular(), color = PrimoriaTheme.colors.mastery)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(PrimoriaTheme.colors.mastery),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MasteryBadge(topic.masteryLevel)
                    Text(
                        "  ${topic.correct} of ${topic.attempts} correct",
                        style = MaterialTheme.typography.bodySmall.tabular(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
