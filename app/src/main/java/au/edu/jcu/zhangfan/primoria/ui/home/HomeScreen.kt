package au.edu.jcu.zhangfan.primoria.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.domain.logic.StreakSummary
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.SubjectArt
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
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        if (canStart) onSubmitTopic()
    }
    val pick: (String) -> Unit = { title ->
        focusManager.clearFocus()
        onPickTopic(title)
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 18.dp), modifier = Modifier.fillMaxSize().imePadding()) {
            item {
                ContentColumn {
                    Row(
                        Modifier.windowInsetsPadding(WindowInsets.statusBars).fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(greeting, style = MaterialTheme.typography.displayLarge, modifier = Modifier.weight(1f).semantics { heading() })
                        if (!state.loading) XpBadge(state, onOpenStats)
                    }
                }
            }
            if (state.loading) {
                item { ContentColumn { LinearProgressIndicator(Modifier.fillMaxWidth().padding(20.dp)) } }
            } else {
                state.recentTopics.firstOrNull()?.let { topic ->
                    item {
                        ContentColumn {
                            ContinuePanel(topic, { pick(topic.title) }, Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp))
                        }
                    }
                }
                if (state.recentTopics.size > 1) {
                    item {
                        ContentColumn {
                            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("More recent topics", style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(horizontal = 20.dp).semantics { heading() })
                                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.recentTopics.drop(1), key = { it.id }) { topic ->
                                        LibraryTile(topic.title, { pick(topic.title) }, Modifier.width(176.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                if (state.aiEnabled) {
                    item {
                        ContentColumn {
                            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                TopicField(topicInput, onTopicInputChange, canStart, submit)
                                Text("AI quizzes share your topic and quiz preferences with DeepSeek.",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    ContentColumn {
                        OfflineLibrary(
                            state, topicInput, onTopicInputChange, canStart, submit, pick,
                            Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
                        )
                    }
                }
                state.streak?.let { streak ->
                    item {
                        ContentColumn {
                            WeeklyGoalSummary(streak, onOpenSettings, Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp))
                        }
                    }
                }
            }
        }
        // Once content scrolls under the status bar, give it a solid backdrop so icons stay legible.
        AnimatedVisibility(visible = scrolled, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).windowInsetsTopHeight(WindowInsets.statusBars))
        }
    }
}

private fun greetingFor(time: LocalTime): String = when (time.hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

@Composable
private fun XpBadge(state: HomeUiState, onOpenStats: () -> Unit) {
    val colors = PrimoriaTheme.colors
    Box(
        Modifier.heightIn(min = 48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onOpenStats)
            .semantics { contentDescription = "${state.xpToday} of ${state.dailyXpCap} XP today. Open progress" },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.background(colors.effortContainer, CircleShape).padding(horizontal = 9.dp, vertical = 5.dp),
        ) {
            Icon(Icons.Outlined.Bolt, null, tint = colors.effort, modifier = Modifier.size(13.dp))
            Text("${state.xpToday} XP", style = MaterialTheme.typography.labelMedium.tabular(), color = colors.onEffortContainer)
        }
    }
}

@Composable
private fun TopicField(
    value: String,
    onValueChange: (String) -> Unit,
    canStart: Boolean,
    onSubmit: (() -> Unit)? = null,
) {
    val aiEnabled = onSubmit != null
    val label = if (aiEnabled) "Any topic, e.g. black holes" else "Find an offline topic"
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(label) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            leadingIcon = { Icon(if (aiEnabled) Icons.Outlined.AutoAwesome else Icons.Outlined.Search, null, modifier = Modifier.size(18.dp)) },
            trailingIcon = if (value.isNotEmpty()) {
                { IconButton(onClick = { onValueChange("") }) { Icon(Icons.Outlined.Clear, "Clear topic") } }
            } else null,
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PrimoriaTheme.colors.cardSurface,
                unfocusedContainerColor = PrimoriaTheme.colors.cardSurface,
                unfocusedBorderColor = PrimoriaTheme.colors.hairline,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = if (aiEnabled) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
                imeAction = if (aiEnabled) ImeAction.Go else ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onGo = { onSubmit?.invoke() }),
            modifier = Modifier.weight(1f).testTag("topic_input").semantics { contentDescription = label },
        )
        if (onSubmit != null) {
            FilledIconButton(onSubmit, Modifier.size(48.dp).testTag("start_quiz"), enabled = canStart, shape = RoundedCornerShape(13.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Start a quiz")
            }
        }
    }
}

@Composable
private fun OfflineLibrary(
    state: HomeUiState,
    topicInput: String,
    onTopicInputChange: (String) -> Unit,
    canStart: Boolean,
    onSearchSaved: () -> Unit,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titles = remember(state.libraryTopics, state.aiEnabled, topicInput) {
        if (state.aiEnabled) state.libraryTopics else state.libraryTopics.filter { it.contains(topicInput.trim(), ignoreCase = true) }
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Offline library", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f).semantics { heading() })
                if (!state.aiEnabled) Text("AI off", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("Hand-checked quizzes you can use offline.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!state.aiEnabled) {
            TopicField(topicInput, onTopicInputChange, canStart)
            if (titles.isEmpty() && topicInput.isNotBlank()) {
                Text("No offline topics match your search.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        BoxWithConstraints {
            val columns = if (maxWidth < 260.dp || LocalDensity.current.fontScale >= 1.5f) 1 else 2
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                titles.chunked(columns).forEach { group ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        group.forEach { title -> LibraryTile(title, { onPick(title) }, Modifier.weight(1f)) }
                        if (group.size < columns) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        if (!state.aiEnabled && canStart && titles.none { it.equals(topicInput.trim(), ignoreCase = true) }) {
            TextButton(onClick = onSearchSaved) { Text("Look for saved questions") }
        }
    }
}

@Composable
private fun ContinuePanel(topic: TopicSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(
            Brush.linearGradient(listOf(Color(0xFF302044), Color(0xFF483467), Color(0xFF5B437C))),
        ),
    ) {
        StudyArtwork(SubjectArt.icon(topic.title), Modifier.align(Alignment.BottomEnd).size(142.dp))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Continue learning", color = Color(0xFFEADBF4), style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.semantics { heading() })
            Text(topic.title, color = Color(0xFFFFF9FF),
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(5.dp).background(Color(0xFFB9DBB4), CircleShape))
                Text("${topic.masteryLevel.label} · ${(topic.mastery * 100).roundToInt()}% mastery",
                    color = Color(0xFFD5EBD1), style = MaterialTheme.typography.labelMedium.tabular())
            }
            Button(
                onClick = onClick,
                modifier = Modifier.padding(top = 5.dp).heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4EBFB), contentColor = Color(0xFF392851)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text("Continue quiz")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun StudyArtwork(icon: ImageVector, modifier: Modifier) {
    val botanical = icon == Icons.Outlined.Eco
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * .69f, size.height * .64f)
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF9B86BC).copy(alpha = .46f), Color(0xFF705691).copy(alpha = .17f)),
                    center = center, radius = size.width * .46f),
                radius = size.width * .46f,
                center = center,
            )
            rotate(38f) {
                drawOval(Color(0xFFDFCBF3).copy(alpha = .27f), Offset(size.width * .33f, size.height * .12f),
                    Size(size.width * .58f, size.height * .82f), style = Stroke(1.dp.toPx()))
            }
            if (botanical) {
                val w = size.width
                val h = size.height
                val stem = Path().apply {
                    moveTo(w * .46f, h * .94f)
                    cubicTo(w * .49f, h * .73f, w * .62f, h * .49f, w * .86f, h * .29f)
                }
                drawPath(stem, Color(0xFFD9E8B8), style = Stroke(1.dp.toPx()))
                val leaves = Path().apply {
                    moveTo(w * .65f, h * .54f)
                    cubicTo(w * .48f, h * .44f, w * .47f, h * .25f, w * .56f, h * .15f)
                    cubicTo(w * .7f, h * .24f, w * .78f, h * .43f, w * .65f, h * .54f)
                    close()
                    moveTo(w * .66f, h * .51f)
                    cubicTo(w * .74f, h * .36f, w * .87f, h * .22f, w * 1.02f, h * .25f)
                    cubicTo(w * .95f, h * .44f, w * .82f, h * .51f, w * .66f, h * .51f)
                    close()
                    moveTo(w * .52f, h * .78f)
                    cubicTo(w * .56f, h * .64f, w * .69f, h * .58f, w * .78f, h * .61f)
                    cubicTo(w * .74f, h * .72f, w * .61f, h * .78f, w * .52f, h * .78f)
                    close()
                }
                drawPath(leaves, Color(0xFFBDD7A1).copy(alpha = .86f))
            }
        }
        if (!botanical) {
            Icon(icon, null, Modifier.padding(start = 30.dp).size(46.dp), tint = Color(0xFFD9CBF1).copy(alpha = .8f))
        }
    }
}

@Composable
private fun LibraryTile(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val subject = SubjectArt.colors(title)
    PrimoriaCard(
        modifier = modifier.heightIn(min = 62.dp),
        onClick = onClick,
        shape = RoundedCornerShape(13.dp),
        containerColor = lerp(PrimoriaTheme.colors.cardSurface, subject.container, .68f),
        border = false,
        contentPadding = 8.dp,
    ) {
        Row(Modifier.heightIn(min = 46.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Surface(shape = RoundedCornerShape(9.dp), color = subject.container) {
                Icon(SubjectArt.icon(title), null, Modifier.size(29.dp).padding(6.dp), tint = subject.content)
            }
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun WeeklyGoalSummary(streak: StreakSummary, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val done = streak.activeDaysThisWeek.coerceAtMost(streak.weeklyGoal)
    val palette = PrimoriaTheme.colors
    Column(modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpenSettings)) {
        HorizontalDivider(color = palette.hairline)
        Column(Modifier.padding(top = 10.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Weekly goal", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).semantics { heading() })
                Text("$done of ${streak.weeklyGoal} days", style = MaterialTheme.typography.labelMedium.tabular())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(streak.weeklyGoal) { day ->
                    Box(Modifier.weight(1f).height(4.dp).background(if (day < done) palette.effort else palette.hairline, CircleShape))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Outlined.AcUnit, null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                Text("${streak.freezesLeft} ${if (streak.freezesLeft == 1) "freeze" else "freezes"} available",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
