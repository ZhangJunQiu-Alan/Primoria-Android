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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.data.repository.LearningStats
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.LearningMetric
import au.edu.jcu.zhangfan.primoria.ui.components.EmptyState
import au.edu.jcu.zhangfan.primoria.ui.components.MasteryBadge
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.SectionHeader
import au.edu.jcu.zhangfan.primoria.ui.components.SubjectArt
import au.edu.jcu.zhangfan.primoria.ui.home.WeeklyGoalSummary
import au.edu.jcu.zhangfan.primoria.ui.components.animatedProgress
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular
import kotlin.math.roundToInt

@Composable
fun StatsRoute(
    onStartQuiz: (TopicSummary) -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(state = state, onTopicClick = onStartQuiz)
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
        item {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LearningMetric("Mastery", "${(averageMastery * 100).roundToInt()}%", "Across studied topics",
                    colors.masteryContainer, colors.onMasteryContainer, Modifier.weight(1f).fillMaxHeight())
                LearningMetric("Effort today", "${stats.xpToday} XP", "${stats.dailyXpCap} XP daily limit",
                    colors.effortContainer, colors.onEffortContainer, Modifier.weight(1f).fillMaxHeight())
            }
        }
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
                    "XP is earned on correct first answers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { WeeklyGoalSummary(stats.streak) }
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
        if (stats.studiedTopics.isNotEmpty()) {
            item {
                PrimoriaCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProgressTotal("${stats.totalXp} XP", "Total effort", Modifier.weight(1f))
                        ProgressTotal("${(stats.accuracy * 100).roundToInt()}%", "Accuracy", Modifier.weight(1f))
                        ProgressTotal(stats.streak.weeks.toString(), "Goal weeks", Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressTotal(value: String, label: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium.tabular())
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
