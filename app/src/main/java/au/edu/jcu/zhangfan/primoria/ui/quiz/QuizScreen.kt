package au.edu.jcu.zhangfan.primoria.ui.quiz

import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
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
import au.edu.jcu.zhangfan.primoria.ui.components.LearningMetric
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.SegmentedProgress
import au.edu.jcu.zhangfan.primoria.ui.components.SourceBadge
import au.edu.jcu.zhangfan.primoria.ui.components.rememberPressScale
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular
import kotlin.math.roundToInt

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
        onPrevious = viewModel::previous,
        onReview = viewModel::openReview,
        onReturnToSummary = viewModel::returnToSummary,
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
    onPrevious: () -> Unit = {},
    onReview: (Int?) -> Unit = {},
    onReturnToSummary: () -> Unit = onClose,
) {
    var confirmExit by rememberSaveable { mutableStateOf(false) }
    val active = state as? QuizUiState.Active
    val reviewing = active?.reviewSummary != null
    val askBeforeLeaving = active != null && !reviewing && (active.submitting || active.answers.isNotEmpty() || active.feedback != null)
    val close = {
        when {
            reviewing -> onReturnToSummary()
            askBeforeLeaving -> confirmExit = true
            else -> onClose()
        }
    }
    BackHandler(enabled = reviewing || askBeforeLeaving, onBack = close)

    val haptics = LocalHapticFeedback.current
    var acknowledgedAnswers by rememberSaveable { mutableStateOf(0) }
    var summaryAnimated by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(active?.answers?.size) {
        val count = active?.answers?.size ?: return@LaunchedEffect
        if (count > acknowledgedAnswers) {
            acknowledgedAnswers = count
            if (active.hapticsEnabled && !reviewing) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    if (state == QuizUiState.Loading) {
        LaunchedEffect(Unit) {
            acknowledgedAnswers = 0
            summaryAnimated = false
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ContentColumn(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            Column(Modifier.fillMaxSize()) {
                QuizTopBar(state, onClose = close)
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    when (state) {
                        QuizUiState.Loading -> LoadingQuiz()
                        is QuizUiState.Active -> ActiveQuiz(state, onSelect, onCheck, onNext, onPrevious, onReport)
                        is QuizUiState.Finished -> QuizSummary(
                            state, onDone = onClose, onPractiseAgain = { onPractiseAgain(state.topicTitle) },
                            onReview = onReview, animate = !summaryAnimated,
                            onAnimated = { summaryAnimated = true },
                        )
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
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(onClick = onRetry) { Text("Try again") }
                                    TextButton(onClick = onClose) { Text("Back to Home") }
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
        val reviewing = (state as? QuizUiState.Active)?.reviewSummary != null
        IconButton(onClick = onClose) {
            Icon(
                if (reviewing) Icons.AutoMirrored.Outlined.ArrowBack else Icons.Outlined.Close,
                contentDescription = if (reviewing) "Return to summary" else "Close quiz",
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val title = when (state) {
                is QuizUiState.Active -> "Question ${state.index + 1} of ${state.questions.size}"
                is QuizUiState.Finished -> "Quiz summary"
                else -> "Quiz"
            }
            val topic = (state as? QuizUiState.Active)?.topicTitle ?: (state as? QuizUiState.Finished)?.topicTitle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.labelLarge.tabular())
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
                    answered = maxOf(state.answers.size, state.index + if (state.feedback != null) 1 else 0),
                )
            }
        }
    }
}

@Composable
private fun LoadingQuiz() {
    Column(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(82.dp).padding(22.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        Text("Preparing your quiz", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text("Fresh AI questions can take up to half a minute.", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        LinearProgressIndicator(Modifier.width(160.dp), trackColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveQuiz(
    state: QuizUiState.Active,
    onSelect: (Int) -> Unit,
    onCheck: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onReport: (QuizQuestion, String) -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val reviewing = state.reviewSummary != null
    val reviewPosition = state.reviewIndices.indexOf(state.index)
    val canGoBack = if (reviewing) reviewPosition > 0 else state.index > 0

    key(state.index) {
        var showReportSheet by rememberSaveable { mutableStateOf(false) }
        val question = state.question
        val feedback = state.feedback
        val reported = question.id in state.reportedIds
        val scroll = rememberScrollState()
        LaunchedEffect(feedback != null) {
            if (feedback != null) {
                delay(250)
                scroll.animateScrollTo(scroll.maxValue)
            }
        }
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll)
                    .padding(horizontal = 20.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.notice?.takeIf { state.index == 0 && !reviewing }?.let { notice ->
                    Surface(shape = RoundedCornerShape(14.dp), color = PrimoriaTheme.colors.effortContainer,
                        contentColor = PrimoriaTheme.colors.onEffortContainer) {
                        Text(notice, Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SourceBadge(question.source)
                    if (state.fromStudyMaterial) Text("Study material", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (reviewing) Text("Answer review", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showReportSheet = true }, enabled = !reported) {
                        Icon(if (reported) Icons.Filled.Flag else Icons.Outlined.Flag,
                            contentDescription = if (reported) "Reported" else "Report this question",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(question.prompt, style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 6.dp).semantics { heading() })
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.forEachIndexed { index, option ->
                        OptionTile(
                            letter = 'A' + index,
                            text = option,
                            selected = state.selectedIndex == index,
                            result = feedback?.let {
                                when (index) {
                                    question.correctIndex -> OptionResult.CORRECT
                                    it.chosenIndex -> OptionResult.WRONG_CHOICE
                                    else -> OptionResult.OTHER
                                }
                            },
                            onClick = {
                                if (!state.submitting) {
                                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelect(index)
                                }
                            },
                        )
                    }
                }
                AnimatedVisibility(
                    visible = feedback != null,
                    enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 5 },
                    exit = fadeOut(tween(100)),
                ) {
                    if (feedback != null) FeedbackPanel(question, feedback, state.dailyCapReached)
                }
                state.submitError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            Surface(color = MaterialTheme.colorScheme.background) {
                Row(
                    Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (canGoBack) {
                        OutlinedButton(
                            onClick = onPrevious,
                            enabled = !state.submitting,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.heightIn(min = 52.dp),
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, Modifier.size(17.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Previous")
                        }
                    }
                    val label = when {
                        reviewing && reviewPosition == state.reviewIndices.lastIndex -> "Back to summary"
                        reviewing -> "Next review"
                        state.submitting -> "Checking…"
                        feedback == null -> "Check answer"
                        state.isLastQuestion -> "See summary"
                        else -> "Next question"
                    }
                    Button(
                        onClick = if (feedback == null) onCheck else onNext,
                        enabled = !state.submitting && (feedback != null || state.selectedIndex != null),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("quiz_primary_action"),
                    ) { Text(label, style = MaterialTheme.typography.titleSmall) }
                }
            }
        }

        if (showReportSheet) {
            ReportSheet(
                onDismiss = { showReportSheet = false },
                onSubmit = { reason ->
                    onReport(question, reason)
                    showReportSheet = false
                },
            )
        }
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
        OptionResult.CORRECT -> BorderStroke(1.5.dp, extended.mastery)
        OptionResult.WRONG_CHOICE -> BorderStroke(1.5.dp, colors.error)
        OptionResult.OTHER -> BorderStroke(1.dp, extended.hairline)
        null -> if (selected) BorderStroke(1.5.dp, colors.primary) else BorderStroke(1.dp, extended.hairline)
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
        else -> colors.surfaceContainer
    }
    val badgeContent = when {
        result == OptionResult.CORRECT -> extended.onMastery
        result == OptionResult.WRONG_CHOICE -> colors.onError
        selected && result == null -> colors.onPrimary
        else -> colors.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = container,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
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
                modifier = Modifier.size(30.dp).clip(CircleShape).background(badgeColor),
            ) {
                when (result) {
                    OptionResult.CORRECT -> Icon(Icons.Outlined.Check, null, tint = badgeContent, modifier = Modifier.size(18.dp))
                    OptionResult.WRONG_CHOICE -> Icon(Icons.Outlined.Close, null, tint = badgeContent, modifier = Modifier.size(18.dp))
                    else -> Text(letter.toString(), style = MaterialTheme.typography.labelMedium, color = badgeContent)
                }
            }
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = 14.dp))
        }
    }
}

@Composable
private fun FeedbackPanel(question: QuizQuestion, feedback: AnswerFeedback, capReached: Boolean) {
    val extended = PrimoriaTheme.colors
    val scheme = MaterialTheme.colorScheme
    val accent = if (feedback.correct) extended.mastery else scheme.error
    val xpLine = when {
        feedback.xpAwarded > 0 -> "+${feedback.xpAwarded} XP for a correct first answer"
        feedback.correct && capReached -> "Daily XP limit reached. This answer still counts toward mastery."
        feedback.correct -> "You've already answered this question, so it earns no XP."
        else -> null
    }
    Surface(
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(16.dp),
        color = if (feedback.correct) extended.masteryContainer else scheme.errorContainer,
        contentColor = if (feedback.correct) extended.onMasteryContainer else scheme.onErrorContainer,
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(if (feedback.correct) Icons.Outlined.CheckCircle else Icons.Outlined.Lightbulb, null,
                    Modifier.size(19.dp), tint = accent)
                Text(if (feedback.correct) "Correct." else "Not quite — here's why:",
                    style = MaterialTheme.typography.titleMedium)
            }
            Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
            xpLine?.let { Text(it, style = MaterialTheme.typography.labelMedium) }
            if (question.source == ContentSource.AI_GENERATED) {
                Text("AI-generated explanation", style = MaterialTheme.typography.labelMedium)
            }
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
private fun QuizSummary(
    state: QuizUiState.Finished,
    onDone: () -> Unit,
    onPractiseAgain: () -> Unit,
    onReview: (Int?) -> Unit,
    animate: Boolean,
    onAnimated: () -> Unit,
) {
    val extended = PrimoriaTheme.colors
    val score = remember { Animatable(if (animate) 0f else 1f) }
    val details = remember { Animatable(if (animate) 0f else 1f) }
    val session = state.session
    val missed = session?.answers?.count { !it.value.correct } ?: 0
    LaunchedEffect(Unit) {
        score.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        details.animateTo(1f, tween(250))
        onAnimated()
    }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = RoundedCornerShape(22.dp), color = Color(0xFF3835BB), contentColor = Color.White) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(state.topicTitle, style = MaterialTheme.typography.labelLarge, color = Color(0xFFE5E2FF))
                        Text("${state.correct} / ${state.total}", style = MaterialTheme.typography.displayLarge.tabular(),
                            modifier = Modifier.graphicsLayer {
                                scaleX = .80f + score.value * .20f
                                scaleY = .80f + score.value * .20f
                            }.semantics { heading() })
                        Text(if (state.correct == 1) "answer correct" else "answers correct", style = MaterialTheme.typography.titleMedium)
                        if (session != null) {
                            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                session.questions.indices.forEach { index ->
                                    val correct = session.answers[index]?.correct == true
                                    Box(Modifier.size(9.dp).background(
                                        if (correct) Color(0xFFDAFA82) else Color(0xFF9995F3), CircleShape,
                                    ))
                                }
                            }
                        }
                    }
                    Box(
                        Modifier.size(72.dp).graphicsLayer {
                            scaleX = .70f + score.value * .30f
                            scaleY = .70f + score.value * .30f
                            rotationZ = -17f + score.value * 17f
                        }.background(Color(0xFFDAFA82), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(39.dp), tint = Color(0xFF25229D))
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Max).graphicsLayer { alpha = .55f + details.value * .45f },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LearningMetric("Topic mastery", "${(state.mastery * 100).roundToInt()}%", state.masteryLevel.label,
                    extended.masteryContainer, extended.onMasteryContainer, Modifier.weight(1f).fillMaxHeight())
                LearningMetric("Effort earned", "+${state.xpEarned} XP",
                    if (state.dailyCapReached) "Daily XP limit reached" else "Correct first answers",
                    extended.effortContainer, extended.onEffortContainer, Modifier.weight(1f).fillMaxHeight())
            }
            if (session != null) {
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Answer review", style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).semantics { heading() })
                    Text(if (missed == 0) "All correct" else "$missed to revisit",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (missed == 0) extended.mastery else MaterialTheme.colorScheme.error)
                }
                session.questions.forEachIndexed { index, question ->
                    val answer = session.answers[index] ?: return@forEachIndexed
                    ReviewRow(index, question, answer.correct) { onReview(index) }
                }
                if (missed > 0) {
                    PrimoriaCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                        Text("Build on this quiz", style = MaterialTheme.typography.titleLarge)
                        Text(if (missed == 1) "Revisit the explanation for your missed answer."
                            else "Revisit the explanations for your $missed missed answers.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { onReview(null) }) { Text("Review missed answers") }
                    }
                }
            }
        }
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = onPractiseAgain, modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp)) { Text("Practise again") }
                Button(onClick = onDone, modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp)) { Text("Done") }
            }
        }
    }
}

@Composable
private fun ReviewRow(index: Int, question: QuizQuestion, correct: Boolean, onClick: () -> Unit) {
    val colors = PrimoriaTheme.colors
    val scheme = MaterialTheme.colorScheme
    PrimoriaCard(onClick = onClick, contentPadding = 10.dp, shape = RoundedCornerShape(13.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 42.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = if (correct) colors.masteryContainer else scheme.errorContainer,
                contentColor = if (correct) colors.onMasteryContainer else scheme.onErrorContainer,
            ) {
                Icon(if (correct) Icons.Outlined.Check else Icons.Outlined.Replay, null, Modifier.size(32.dp).padding(7.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(question.prompt, style = MaterialTheme.typography.titleSmall)
                Text("Question ${index + 1} · ${if (correct) "Correct" else "Revisit"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (correct) colors.mastery else scheme.error)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, Modifier.size(15.dp), tint = scheme.onSurfaceVariant)
        }
    }
}
