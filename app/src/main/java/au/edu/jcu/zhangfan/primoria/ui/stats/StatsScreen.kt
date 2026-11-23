package au.edu.jcu.zhangfan.primoria.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.data.repository.LearningStats
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.EmptyState
import au.edu.jcu.zhangfan.primoria.ui.components.MasteryBadge
import au.edu.jcu.zhangfan.primoria.ui.components.SectionHeader
import au.edu.jcu.zhangfan.primoria.ui.components.WeeklyGoalCard
import kotlin.math.roundToInt

@Composable
fun StatsRoute(
    onStartQuiz: (String) -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(state = state, onTopicClick = { onStartQuiz(it.title) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(state: StatsUiState, onTopicClick: (TopicSummary) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Your progress") }) }) { padding ->
        ContentColumn(Modifier.padding(padding)) {
            when (state) {
                StatsUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is StatsUiState.Loaded -> StatsContent(state.stats, onTopicClick)
            }
        }
    }
}

@Composable
private fun StatsContent(stats: LearningStats, onTopicClick: (TopicSummary) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
    ) {
        item { SectionHeader("Effort") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("${stats.totalXp}", style = MaterialTheme.typography.displaySmall)
                        Text(" XP in total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    LinearProgressIndicator(
                        progress = { stats.xpToday / stats.dailyXpCap.toFloat() },
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "${stats.xpToday} of ${stats.dailyXpCap} XP today" },
                    )
                    Text("${stats.xpToday} / ${stats.dailyXpCap} XP today", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "XP counts correct first answers only and stops at ${stats.dailyXpCap} a day, so it measures effort — not time on screen.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        item {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Last 7 days", style = MaterialTheme.typography.titleSmall)
                    WeeklyXpChart(stats.week, stats.dailyXpCap)
                    Text(
                        "Dashed line = daily XP limit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item { WeeklyGoalCard(stats.streak) }

        item { SectionHeader("Knowledge") }
        if (stats.studiedTopics.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Insights,
                    title = "Nothing to show yet",
                    body = "Finish your first quiz to see your mastery and accuracy here.",
                )
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("${(stats.accuracy * 100).roundToInt()}% accuracy", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "${stats.totalAnswers} answers across ${stats.studiedTopics.size} topics. " +
                                "Mastery is tracked separately from XP: it reflects what you know.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            items(stats.studiedTopics, key = { it.id }) { topic ->
                TopicMasteryRow(topic, onClick = { onTopicClick(topic) })
            }
        }
        item { Box(Modifier.padding(bottom = 16.dp)) }
    }
}

@Composable
private fun TopicMasteryRow(topic: TopicSummary, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    topic.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                MasteryBadge(topic.masteryLevel)
            }
            LinearProgressIndicator(
                progress = { topic.mastery.toFloat() },
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Mastery ${(topic.mastery * 100).roundToInt()}% · ${topic.correct} of ${topic.attempts} correct · tap to practise",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
