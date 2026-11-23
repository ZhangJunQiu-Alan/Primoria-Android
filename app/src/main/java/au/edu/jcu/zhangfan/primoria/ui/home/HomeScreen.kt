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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.outlined.CloudOff
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
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
import au.edu.jcu.zhangfan.primoria.ui.components.gradientBackground
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
    val scrolledPastHero by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    Box(Modifier.fillMaxSize()) {
    ContentColumn(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                HeroHeader(state, greeting, onOpenStats, onOpenSettings)
            }
            item {
                PromptBar(
                    topicInput = topicInput,
                    canStart = canStart,
                    aiEnabled = state.aiEnabled,
                    loading = state.loading,
                    onTopicInputChange = onTopicInputChange,
                    onSubmit = onSubmitTopic,
                    modifier = Modifier.padding(horizontal = 16.dp).offset(y = (-40).dp),
                )
            }
            if (state.loading) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) }
            } else {
                item {
                    Column(Modifier.offset(y = (-40).dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Continue learning", Modifier.padding(horizontal = 20.dp))
                        if (state.recentTopics.isEmpty()) {
                            PrimoriaCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), border = false, containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                                Text(
                                    "Topics you practise will appear here, with your mastery for each.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                            ) {
                                val single = state.recentTopics.size == 1
                                items(state.recentTopics, key = { it.id }) { topic ->
                                    TopicCard(
                                        topic,
                                        onClick = { onPickTopic(topic.title) },
                                        modifier = if (single) Modifier.fillParentMaxWidth() else Modifier.width(220.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Column(Modifier.offset(y = (-40).dp).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader("Offline library", Modifier.padding(horizontal = 4.dp))
                        Text(
                            "Hand-checked questions that work without internet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
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
        // Once the hero has scrolled away, give the status bar a solid backdrop so icons stay legible.
        AnimatedVisibility(visible = scrolledPastHero, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.96f))
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
private fun HeroHeader(state: HomeUiState, greeting: String, onOpenStats: () -> Unit, onOpenSettings: () -> Unit) {
    val colors = PrimoriaTheme.colors
    val fade = MaterialTheme.colorScheme.background
    Box(
        Modifier
            .fillMaxWidth()
            .gradientBackground(colors.heroGradient)
            // Fade the wash into the page so the prompt bar floats over a soft edge, not a seam.
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.verticalGradient(listOf(Color.Transparent, fade), startY = size.height - 110.dp.toPx(), endY = size.height),
                )
            }
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 64.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Primoria", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Outlined.Bolt, null, tint = colors.effort, modifier = Modifier.size(18.dp))
                        Text(
                            "${state.xpToday} / ${state.dailyXpCap} XP today",
                            style = MaterialTheme.typography.labelLarge.tabular(),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(greeting, style = MaterialTheme.typography.displaySmall)
                Text(
                    "What would you like to understand today?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.streak?.let { streak ->
                Surface(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = if (colors.isDark) 0.35f else 0.7f),
                ) {
                    WeeklyGoalRow(streak, Modifier.padding(16.dp))
                }
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
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PrimoriaCard(shape = RoundedCornerShape(22.dp), contentPadding = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = topicInput,
                    onValueChange = onTopicInputChange,
                    placeholder = { Text("Any topic, e.g. black holes", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium,
                    leadingIcon = { Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (topicInput.isNotEmpty()) {
                            IconButton(onClick = { onTopicInputChange("") }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear topic")
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
                    shape = RoundedCornerShape(16.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(52.dp).testTag("start_quiz"),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "Start a quiz")
                }
            }
        }
        if (!loading) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(
                    if (aiEnabled) Icons.Outlined.AutoAwesome else Icons.Outlined.CloudOff,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    if (aiEnabled) {
                        "AI writes 5 fresh questions. Only your topic is sent to DeepSeek."
                    } else {
                        "AI is off — quizzes use saved and offline questions. Turn it on in Settings."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun TopicCard(topic: TopicSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val subject = SubjectArt.colors(topic.title)
    val progress = animatedProgress(topic.mastery.toFloat())
    PrimoriaCard(onClick = onClick, shape = RoundedCornerShape(22.dp), contentPadding = 16.dp, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(subject.container),
            ) { Icon(SubjectArt.icon(topic.title), null, tint = subject.content, modifier = Modifier.size(22.dp)) }
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
            Text("${(topic.mastery * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge.tabular(), color = PrimoriaTheme.colors.mastery)
        }
    }
}

@Composable
private fun LibraryTile(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val subject = SubjectArt.colors(title)
    PrimoriaCard(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        containerColor = subject.container,
        contentColor = subject.content,
        border = false,
        contentPadding = 16.dp,
        modifier = modifier.height(116.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimoriaTheme.colors.cardSurface.copy(alpha = 0.75f)),
        ) { Icon(SubjectArt.icon(title), null, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.weight(1f))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
