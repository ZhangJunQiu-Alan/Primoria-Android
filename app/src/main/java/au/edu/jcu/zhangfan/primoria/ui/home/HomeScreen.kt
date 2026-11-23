package au.edu.jcu.zhangfan.primoria.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.MasteryBadge
import au.edu.jcu.zhangfan.primoria.ui.components.SectionHeader
import au.edu.jcu.zhangfan.primoria.ui.components.WeeklyGoalCard
import kotlin.math.roundToInt

@Composable
fun HomeRoute(
    onStartQuiz: (String) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        topicInput = viewModel.topicInput,
        canStart = viewModel.canStart,
        onTopicInputChange = viewModel::onTopicInputChange,
        onSubmitTopic = { viewModel.consumeTopic()?.let(onStartQuiz) },
        onPickTopic = onStartQuiz,
        onOpenStats = onOpenStats,
        onOpenSettings = onOpenSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    topicInput: String,
    canStart: Boolean,
    onTopicInputChange: (String) -> Unit,
    onSubmitTopic: () -> Unit,
    onPickTopic: (String) -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Primoria") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
    ) { padding ->
        ContentColumn(Modifier.padding(padding).imePadding()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            ) {
                item {
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("What do you want to learn today?", style = MaterialTheme.typography.titleLarge)
                            OutlinedTextField(
                                value = topicInput,
                                onValueChange = onTopicInputChange,
                                label = { Text("Any topic") },
                                placeholder = { Text("e.g. Photosynthesis, the French Revolution") },
                                singleLine = true,
                                trailingIcon = {
                                    if (topicInput.isNotEmpty()) {
                                        IconButton(onClick = { onTopicInputChange("") }) {
                                            Icon(Icons.Outlined.Clear, contentDescription = "Clear topic")
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Go,
                                ),
                                keyboardActions = KeyboardActions(onGo = {
                                    focusManager.clearFocus()
                                    if (canStart) onSubmitTopic()
                                }),
                                modifier = Modifier.fillMaxWidth().testTag("topic_input"),
                            )
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    onSubmitTopic()
                                },
                                enabled = canStart,
                                modifier = Modifier.fillMaxWidth().testTag("start_quiz"),
                            ) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                                Text("Start a quiz", Modifier.padding(start = 8.dp))
                            }
                            Text(
                                text = if (state.aiEnabled) {
                                    "AI writes new questions for any topic. Only the topic is sent to DeepSeek."
                                } else {
                                    "AI is off, so quizzes use saved and offline questions. You can turn it on in Settings."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                state.streak?.let { streak ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            WeeklyGoalCard(streak)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(
                                    onClick = onOpenStats,
                                    label = { Text("${state.xpToday} / ${state.dailyXpCap} XP today") },
                                    leadingIcon = { Icon(Icons.Outlined.Bolt, null, tint = MaterialTheme.colorScheme.secondary) },
                                )
                                AssistChip(onClick = onOpenSettings, label = { Text("Change goal") })
                            }
                        }
                    }
                }

                item { SectionHeader("Continue learning") }
                if (state.recentTopics.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "Topics you practise will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp),
                            )
                        }
                    }
                } else {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.recentTopics, key = { it.id }) { topic ->
                                RecentTopicCard(topic, onClick = { onPickTopic(topic.title) })
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Offline library")
                        Text(
                            "Hand-checked questions that work without internet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.libraryTopics.forEach { title ->
                                SuggestionChip(
                                    onClick = { onPickTopic(title) },
                                    label = { Text(title) },
                                    icon = { Icon(Icons.Outlined.LibraryBooks, null) },
                                )
                            }
                        }
                    }
                }
                item { Column(Modifier.padding(bottom = 16.dp)) {} }
            }
        }
    }
}

@Composable
private fun RecentTopicCard(topic: TopicSummary, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.width(200.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(topic.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            MasteryBadge(topic.masteryLevel)
            Text(
                "${(topic.accuracy * 100).roundToInt()}% correct · ${topic.attempts} answers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
