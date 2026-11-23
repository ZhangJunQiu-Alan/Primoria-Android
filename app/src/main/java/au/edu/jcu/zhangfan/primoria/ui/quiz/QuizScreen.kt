package au.edu.jcu.zhangfan.primoria.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.QuizQuestion
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.EmptyState
import au.edu.jcu.zhangfan.primoria.ui.components.MasteryBadge
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.ProgressRing
import au.edu.jcu.zhangfan.primoria.ui.components.SegmentedProgress
import au.edu.jcu.zhangfan.primoria.ui.components.SourceBadge
import au.edu.jcu.zhangfan.primoria.ui.components.gradientBackground
import au.edu.jcu.zhangfan.primoria.ui.components.rememberPressScale
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular
import kotlin.math.roundToInt

private val SheetEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

@Composable
fun QuizRoute(
    onClose: () -> Unit,
    onTryTopic: (String) -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    QuizScreen(
        state = state,
        onSelect = viewModel::select,
        onCheck = viewModel::check,
        onNext = viewModel::next,
        onRetry = viewModel::load,
        onReport = viewModel::report,
        onClose = onClose,
        onPractiseAgain = onTryTopic,
    )
}

@Composable
fun QuizScreen(
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onReport: (QuizQuestion, String) -> Unit,
    onClose: () -> Unit,
    onPractiseAgain: (String) -> Unit,
) {
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val active = state as? QuizUiState.Active
    // Leaving mid-quiz asks first, but honestly: answers already checked are kept.
    val askBeforeLeaving = active != null && (active.index > 0 || active.feedback != null)
    BackHandler(enabled = askBeforeLeaving) { confirmExit = true }

    Box(
        Modifier
            .fillMaxSize()
            .gradientBackground(PrimoriaTheme.colors.quizBackdrop),
    ) {
        ContentColumn(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            Column(Modifier.fillMaxSize()) {
                QuizTopBar(state, onClose = { if (askBeforeLeaving) confirmExit = true else onClose() })
                Box(Modifier.weight(1f)) {
                    when (state) {
                        QuizUiState.Loading -> LoadingQuiz()
                        is QuizUiState.Active -> ActiveQuiz(state, onSelect, onCheck, onNext, onReport)
                        is QuizUiState.Finished -> QuizSummary(state, onDone = onClose, onPractiseAgain = { onPractiseAgain(state.topicTitle) })
                        is QuizUiState.Refused -> EmptyState(
                            icon = Icons.Outlined.SentimentSatisfied,
                            title = "Let's pick a different topic",
                            body = state.reason,
                            action = { Button(onClick = onClose) { Text("Choose another topic") } },
                        )
                        is QuizUiState.Unavailable -> EmptyState(
                            icon = Icons.Outlined.CloudOff,
                            title = "No questions yet",
                            body = state.message,
                            action = {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedButton(onClick = onClose) { Text("Back to Home") }
                                    Button(onClick = onRetry) { Text("Try again") }
                                }
                            },
                        )
                        QuizUiState.InvalidTopic -> EmptyState(
                            icon = Icons.Outlined.Info,
                            title = "That topic is empty",
                            body = "Type a subject with at least one letter or number, like \"Fractions\".",
                            action = { Button(onClick = onClose) { Text("Back to Home") } },
                        )
                    }
                }
            }
        }
    }

    if (confirmExit) {
        AlertDialog(
            onDismissRequest = { confirmExit = false },
            title = { Text("Leave this quiz?") },
            text = { Text("Answers you've already checked are saved to your progress.") },
            confirmButton = { TextButton(onClick = { confirmExit = false; onClose() }) { Text("Leave") } },
            dismissButton = { TextButton(onClick = { confirmExit = false }) { Text("Keep going") } },
        )
    }
}

@Composable
private fun QuizTopBar(state: QuizUiState, onClose: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
    ) {
        FilledTonalIconButton(onClick = onClose) { Icon(Icons.Outlined.Close, contentDescription = "Close quiz") }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val title = when (state) {
                is QuizUiState.Active -> "Question ${state.index + 1} of ${state.questions.size}"
                is QuizUiState.Finished -> "Quiz summary"
                else -> "Quiz"
            }
            val topic = (state as? QuizUiState.Active)?.topicTitle ?: (state as? QuizUiState.Finished)?.topicTitle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall.tabular())
                if (topic != null) {
                    Text(
                        "  ·  $topic",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (state is QuizUiState.Active) {
                SegmentedProgress(
                    total = state.questions.size,
                    current = state.index,
                    answered = state.index + if (state.feedback != null) 1 else 0,
                )
            }
        }
    }
}

@Composable
private fun LoadingQuiz() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        CircularProgressIndicator(strokeWidth = 5.dp, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(24.dp))
        Text("Preparing your questions…", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            "Fresh AI questions can take up to half a minute.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveQuiz(
    state: QuizUiState.Active,
    onSelect: (Int) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onReport: (QuizQuestion, String) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    var showReportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.feedback) {
        if (state.feedback != null && state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = state.index,
            transitionSpec = {
                (slideInHorizontally(tween(300)) { it / 8 } + fadeIn(tween(300))) togetherWith
                    (slideOutHorizontally(tween(200)) { -it / 8 } + fadeOut(tween(200)))
            },
            label = "question",
            modifier = Modifier.weight(1f),
        ) { index ->
            val question = state.questions[index]
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                state.notice?.takeIf { index == 0 }?.let { notice ->
                    Surface(shape = RoundedCornerShape(16.dp), color = PrimoriaTheme.colors.effortContainer, contentColor = PrimoriaTheme.colors.onEffortContainer) {
                        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Outlined.CloudOff, null, modifier = Modifier.size(20.dp))
                            Text(notice, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                PrimoriaCard(shape = RoundedCornerShape(28.dp), contentPadding = 22.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SourceBadge(question.source)
                        Spacer(Modifier.weight(1f))
                        val reported = question.id in state.reportedIds
                        TextButton(onClick = { showReportSheet = true }, enabled = !reported) {
                            Icon(Icons.Outlined.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(if (reported) "Reported" else "Report", Modifier.padding(start = 6.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(question.prompt, style = MaterialTheme.typography.headlineMedium)
                }
                Spacer(Modifier.height(4.dp))
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    question.options.forEachIndexed { optionIndex, option ->
                        OptionTile(
                            letter = 'A' + optionIndex,
                            text = option,
                            selected = state.selectedIndex == optionIndex,
                            result = state.feedback?.let {
                                when (optionIndex) {
                                    question.correctIndex -> OptionResult.CORRECT
                                    it.chosenIndex -> OptionResult.WRONG_CHOICE
                                    else -> OptionResult.OTHER
                                }
                            },
                            onClick = {
                                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(optionIndex)
                            },
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }

        // Check button and feedback panel share the bottom slot; the panel slides up over it.
        Box(Modifier.fillMaxWidth()) {
            if (state.feedback == null) {
                Button(
                    onClick = onCheck,
                    enabled = state.selectedIndex != null && !state.submitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(16.dp)
                        .heightIn(min = 54.dp)
                        .testTag("quiz_primary_action"),
                ) { Text("Check answer", style = MaterialTheme.typography.titleMedium) }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = state.feedback != null,
                enter = slideInVertically(tween(280, easing = SheetEasing)) { it } + fadeIn(tween(180)),
                exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(120)),
            ) {
                state.feedback?.let { feedback ->
                    FeedbackPanel(
                        question = state.question,
                        feedback = feedback,
                        capReached = state.dailyCapReached,
                        actionLabel = if (state.isLastQuestion) "See summary" else "Next question",
                        onNext = onNext,
                    )
                }
            }
        }
    }

    if (showReportSheet) {
        ReportSheet(
            onDismiss = { showReportSheet = false },
            onSubmit = { reason ->
                onReport(state.question, reason)
                showReportSheet = false
            },
        )
    }
}

private enum class OptionResult { CORRECT, WRONG_CHOICE, OTHER }

@Composable
private fun OptionTile(
    letter: Char,
    text: String,
    selected: Boolean,
    result: OptionResult?,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val extended = PrimoriaTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction)
    val container by animateColorAsState(
        when (result) {
            OptionResult.CORRECT -> extended.masteryContainer
            OptionResult.WRONG_CHOICE -> colors.errorContainer
            OptionResult.OTHER -> extended.cardSurface
            null -> if (selected) colors.primaryContainer else extended.cardSurface
        },
        label = "optionContainer",
    )
    val border = when (result) {
        OptionResult.CORRECT -> BorderStroke(2.dp, extended.mastery)
        OptionResult.WRONG_CHOICE -> BorderStroke(2.dp, colors.error)
        OptionResult.OTHER -> BorderStroke(1.dp, extended.hairline)
        null -> if (selected) BorderStroke(2.dp, colors.primary) else BorderStroke(1.dp, extended.hairline)
    }
    // Result is spoken as words so feedback never depends on colour alone.
    val resultLabel = when (result) {
        OptionResult.CORRECT -> "Correct answer"
        OptionResult.WRONG_CHOICE -> "Your answer, incorrect"
        else -> null
    }
    val badgeColor = when {
        result == OptionResult.CORRECT -> extended.mastery
        result == OptionResult.WRONG_CHOICE -> colors.error
        selected && result == null -> colors.primary
        else -> colors.surfaceContainerHigh
    }
    val badgeContent = when {
        result == OptionResult.CORRECT -> extended.onMastery
        result == OptionResult.WRONG_CHOICE -> colors.onError
        selected && result == null -> colors.onPrimary
        else -> colors.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = container,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(if (result == OptionResult.OTHER) 0.6f else 1f)
            .selectable(
                selected = selected,
                enabled = result == null,
                role = Role.RadioButton,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .semantics { resultLabel?.let { stateDescription = it } }
            .testTag("option_$letter"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(34.dp).clip(CircleShape).background(badgeColor),
            ) {
                when (result) {
                    OptionResult.CORRECT -> Icon(Icons.Outlined.CheckCircle, null, tint = badgeContent, modifier = Modifier.size(20.dp))
                    OptionResult.WRONG_CHOICE -> Icon(Icons.Outlined.Cancel, null, tint = badgeContent, modifier = Modifier.size(20.dp))
                    else -> Text(letter.toString(), style = MaterialTheme.typography.labelLarge, color = badgeContent)
                }
            }
            Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(start = 14.dp))
        }
    }
}

@Composable
private fun FeedbackPanel(
    question: QuizQuestion,
    feedback: AnswerFeedback,
    capReached: Boolean,
    actionLabel: String,
    onNext: () -> Unit,
) {
    val extended = PrimoriaTheme.colors
    val accent = if (feedback.correct) extended.mastery else MaterialTheme.colorScheme.error
    val xpLine = when {
        feedback.xpAwarded > 0 -> "+${feedback.xpAwarded} XP for a correct first answer"
        feedback.correct && capReached -> "Today's XP limit is reached — your mastery still grows."
        feedback.correct -> "No XP this time: you've answered this question before."
        else -> null
    }
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = extended.cardSurface,
        border = BorderStroke(1.dp, extended.hairline),
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .heightIn(max = 380.dp)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    if (feedback.correct) Icons.Outlined.CheckCircle else Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp),
                )
                Text(
                    if (feedback.correct) "Correct." else "Not quite — here's why:",
                    style = MaterialTheme.typography.headlineSmall,
                    color = accent,
                )
            }
            Text(question.explanation, style = MaterialTheme.typography.bodyLarge)
            if (question.source == ContentSource.AI_GENERATED) {
                Text(
                    "Explanation written by AI from the topic alone. If something looks wrong, tap Report.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            xpLine?.let {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Bolt, null, tint = extended.effort, modifier = Modifier.size(18.dp))
                    Text(it, style = MaterialTheme.typography.labelLarge, color = extended.effort)
                }
            }
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = 54.dp).testTag("quiz_primary_action"),
            ) { Text(actionLabel, style = MaterialTheme.typography.titleMedium) }
        }
    }
}

private val REPORT_REASONS = listOf(
    "The answer is wrong",
    "The question is unclear",
    "It's inappropriate or unsafe",
    "Something else",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportSheet(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var reason by rememberSaveable { mutableStateOf<String?>(null) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = PrimoriaTheme.colors.cardSurface) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Report this question", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Reports are saved on this device and the question won't be shown again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(Modifier.selectableGroup()) {
                REPORT_REASONS.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .selectable(selected = reason == option, role = Role.RadioButton, onClick = { reason = option })
                            .padding(horizontal = 4.dp),
                    ) {
                        RadioButton(selected = reason == option, onClick = null)
                        Text(option, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
            Button(onClick = { reason?.let(onSubmit) }, enabled = reason != null, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text("Submit report")
            }
        }
    }
}

@Composable
private fun QuizSummary(state: QuizUiState.Finished, onDone: () -> Unit, onPractiseAgain: () -> Unit) {
    val extended = PrimoriaTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(16.dp),
    ) {
        Column(Modifier.padding(horizontal = 4.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${state.correct} of ${state.total} correct", style = MaterialTheme.typography.displayMedium.tabular())
            Text(
                if (state.correct == state.total) "Every answer right — great work." else "Every question you check helps the next one stick.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // Effort and knowledge are shown side by side but never merged (commitment 05).
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Max)) {
            PrimoriaCard(containerColor = extended.masteryContainer, contentColor = extended.onMasteryContainer, border = false, modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("Mastery", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(12.dp))
                ProgressRing(
                    progress = state.mastery.toFloat(),
                    colors = extended.masteryRing,
                    trackColor = extended.onMasteryContainer.copy(alpha = 0.12f),
                    size = 84.dp,
                    strokeWidth = 9.dp,
                ) { Text("${(state.mastery * 100).roundToInt()}%", style = MaterialTheme.typography.titleLarge.tabular()) }
                Spacer(Modifier.height(12.dp))
                MasteryBadge(state.masteryLevel)
            }
            PrimoriaCard(containerColor = extended.effortContainer, contentColor = extended.onEffortContainer, border = false, modifier = Modifier.weight(1f).fillMaxHeight()) {
                Text("Effort", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Bolt, null, tint = extended.effort, modifier = Modifier.size(28.dp))
                    Text("+${state.xpEarned} XP", style = MaterialTheme.typography.headlineLarge.tabular())
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (state.dailyCapReached) "Today's XP limit is reached. Learning still builds mastery." else "XP rewards correct first answers.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Text(
            "Mastery estimates what you know about ${state.topicTitle}; XP only measures effort.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Spacer(Modifier.height(4.dp))
        Button(onClick = onPractiseAgain, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("Practise this topic again") }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text("Done") }
    }
}
