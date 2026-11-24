package au.edu.jcu.zhangfan.primoria.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.MasteryBadge
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.SectionHeader
import au.edu.jcu.zhangfan.primoria.ui.components.SubjectArt
import au.edu.jcu.zhangfan.primoria.ui.components.WeeklyGoalRow
import au.edu.jcu.zhangfan.primoria.ui.components.animatedProgress
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular
import java.time.LocalTime
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
    val greeting = remember { greetingFor(LocalTime.now()) }
    val listState = rememberLazyListState()
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize().imePadding(),
        ) {
            item {
                ContentColumn {
                    Column(
                        Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(start = 20.dp, end = 20.dp, top = 12.dp),
                    ) {
                        TopBar(state, onOpenStats)
                        Spacer(Modifier.height(28.dp))
                        Text(greeting, style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "What would you like to understand today?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(20.dp))
                        PromptBar(
                            topicInput = topicInput,
                            canStart = canStart,
                            aiEnabled = state.aiEnabled,
                            loading = state.loading,
                            onTopicInputChange = onTopicInputChange,
                            onSubmit = onSubmitTopic,
                        )
                    }
                }
            }
            if (state.loading) {
                item { ContentColumn { LinearProgressIndicator(Modifier.fillMaxWidth().padding(20.dp)) } }
            } else {
                state.streak?.let { streak ->
                    item {
                        ContentColumn {
                            PrimoriaCard(
                                onClick = onOpenSettings,
                                contentPadding = 16.dp,
                                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp),
                            ) { WeeklyGoalRow(streak, ringSize = 56) }
                        }
                    }
                }
                if (state.recentTopics.isNotEmpty()) {
                    item {
                        ContentColumn {
                            Column(Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SectionHeader("Continue learning", Modifier.padding(horizontal = 20.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                ) {
                                    val single = state.recentTopics.size == 1
                                    items(state.recentTopics, key = { it.id }) { topic ->
                                        TopicCard(
                                            topic,
                                            onClick = { onPickTopic(topic.title) },
                                            modifier = if (single) Modifier.fillParentMaxWidth() else Modifier.width(264.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    ContentColumn {
                        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                SectionHeader("Offline library")
                                Text(
                                    "Hand-checked questions that work without internet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            state.libraryTopics.chunked(2).forEach { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    pair.forEach { title ->
                                        LibraryTile(title, onClick = { onPickTopic(title) }, modifier = Modifier.weight(1f))
                                    }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
        // Once content scrolls under the status bar, give it a solid backdrop so icons stay legible.
        AnimatedVisibility(visible = scrolled, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsTopHeight(WindowInsets.statusBars),
            )
        }
    }
}

private fun greetingFor(time: LocalTime): String = when (time.hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

@Composable
private fun TopBar(state: HomeUiState, onOpenStats: () -> Unit) {
    val colors = PrimoriaTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                "P",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Text(
            "Primoria",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 10.dp),
        )
        Spacer(Modifier.weight(1f))
        Surface(
            onClick = onOpenStats,
            shape = RoundedCornerShape(999.dp),
            color = colors.effortContainer,
            contentColor = colors.onEffortContainer,
            modifier = Modifier.semantics { contentDescription = "${state.xpToday} of ${state.dailyXpCap} XP today. Open progress" },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            ) {
                Icon(Icons.Outlined.Bolt, null, tint = colors.effort, modifier = Modifier.size(16.dp))
                Text(
                    "${state.xpToday} / ${state.dailyXpCap} XP",
                    style = MaterialTheme.typography.labelMedium.tabular(),
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun PromptBar(
    topicInput: String,
    canStart: Boolean,
    aiEnabled: Boolean,
    loading: Boolean,
    onTopicInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        if (canStart) onSubmit()
    }
    val primary = MaterialTheme.colorScheme.primary
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PrimoriaCard(shape = RoundedCornerShape(18.dp), contentPadding = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = topicInput,
                    onValueChange = onTopicInputChange,
                    placeholder = { Text("Any topic, e.g. black holes", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    leadingIcon = { Icon(Icons.Outlined.AutoAwesome, null, tint = primary, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (topicInput.isNotEmpty()) {
                            IconButton(onClick = { onTopicInputChange("") }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear topic", modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { submit() }),
                    modifier = Modifier.weight(1f).testTag("topic_input").semantics { contentDescription = "Any topic" },
                )
                FilledIconButton(
                    onClick = submit,
                    enabled = canStart,
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = primary,
                        disabledContainerColor = primary.copy(alpha = 0.08f),
                        disabledContentColor = primary.copy(alpha = 0.35f),
                    ),
                    modifier = Modifier.size(46.dp).testTag("start_quiz"),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Start a quiz", modifier = Modifier.size(20.dp))
                }
            }
        }
        if (!loading) {
            Text(
                if (aiEnabled) "AI writes 5 fresh questions · only your topic is shared" else "AI is off · quizzes use offline questions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
    }
}

@Composable
private fun TopicCard(topic: TopicSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val subject = SubjectArt.colors(topic.title)
    val progress = animatedProgress(topic.mastery.toFloat())
    PrimoriaCard(onClick = onClick, contentPadding = 16.dp, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(subject.container),
            ) { Icon(SubjectArt.icon(topic.title), null, tint = subject.content, modifier = Modifier.size(20.dp)) }
            Column(Modifier.weight(1f)) {
                Text(topic.title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${(topic.accuracy * 100).roundToInt()}% correct · ${topic.attempts} answers",
                    style = MaterialTheme.typography.bodySmall.tabular(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MasteryBadge(topic.masteryLevel)
            Box(
                Modifier
                    .weight(1f)
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
            Text("${(topic.mastery * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium.tabular(), color = PrimoriaTheme.colors.mastery)
        }
    }
}

@Composable
private fun LibraryTile(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val subject = SubjectArt.colors(title)
    PrimoriaCard(onClick = onClick, contentPadding = 16.dp, modifier = modifier.height(128.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(subject.container),
        ) { Icon(SubjectArt.icon(title), null, tint = subject.content, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.weight(1f))
        Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("Offline · hand-checked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
