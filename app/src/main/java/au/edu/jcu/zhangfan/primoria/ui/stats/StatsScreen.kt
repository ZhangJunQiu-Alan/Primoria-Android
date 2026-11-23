package au.edu.jcu.zhangfan.primoria.ui.stats

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TaskAlt
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
import au.edu.jcu.zhangfan.primoria.ui.components.StatTile
import au.edu.jcu.zhangfan.primoria.ui.components.SubjectArt
import au.edu.jcu.zhangfan.primoria.ui.components.WeeklyGoalRow
import au.edu.jcu.zhangfan.primoria.ui.components.animatedProgress
import au.edu.jcu.zhangfan.primoria.ui.components.gradientBackground
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text("Your progress", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp))
        }
        item { RingsCard(averageMastery, stats) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        "${stats.totalXp}", "XP in total", Icons.Outlined.Bolt, colors.effort, colors.effortContainer,
                        Modifier.weight(1f),
                    )
                    StatTile(
                        "${(stats.accuracy * 100).roundToInt()}%", "Accuracy", Icons.Outlined.TaskAlt, colors.mastery, colors.masteryContainer,
                        Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(
                        "${stats.studiedTopics.size}", if (stats.studiedTopics.size == 1) "Topic studied" else "Topics studied",
                        Icons.Outlined.School, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer,
                        Modifier.weight(1f),
                    )
                    StatTile(
                        "${stats.streak.weeks}", if (stats.streak.weeks == 1) "Goal week" else "Goal weeks in a row",
                        Icons.Outlined.CalendarMonth, colors.effort, colors.effortContainer,
                        Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            PrimoriaCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Effort · last 7 days", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("${stats.xpToday} / ${stats.dailyXpCap} today", style = MaterialTheme.typography.labelLarge.tabular(), color = colors.effort)
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
        item { PrimoriaCard(Modifier.fillMaxWidth()) { WeeklyGoalRow(stats.streak, ringSize = 72) } }
        item { SectionHeader("Knowledge by topic", Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) }
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
private fun RingsCard(averageMastery: Double, stats: LearningStats) {
    val colors = PrimoriaTheme.colors
    val effortToday = stats.xpToday / stats.dailyXpCap.toFloat()
    val masteryPercent = (averageMastery * 100).roundToInt()
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .gradientBackground(colors.heroGradient)
            .padding(20.dp)
            .clearAndSetSemantics {
                contentDescription = "Average mastery $masteryPercent percent. Effort today ${stats.xpToday} of ${stats.dailyXpCap} XP."
            },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            DualRings(
                outerProgress = averageMastery.toFloat(),
                outerColors = colors.masteryRing,
                innerProgress = effortToday,
                innerColors = colors.effortRing,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
            )
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RingLegend(colors.mastery, "Mastery", "$masteryPercent%", "average across topics")
                RingLegend(colors.effort, "Effort today", "${stats.xpToday} XP", "of ${stats.dailyXpCap} daily limit")
            }
        }
    }
}

@Composable
private fun RingLegend(color: Color, label: String, value: String, caption: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 6.dp))
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
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(subject.container),
            ) { Icon(SubjectArt.icon(topic.title), null, tint = subject.content, modifier = Modifier.size(22.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(topic.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text("${(topic.mastery * 100).roundToInt()}%", style = MaterialTheme.typography.titleSmall.tabular(), color = PrimoriaTheme.colors.mastery)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
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
