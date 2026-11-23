package au.edu.jcu.zhangfan.primoria.ui.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import au.edu.jcu.zhangfan.primoria.ui.components.SourceBadge
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
        onRetry = viewModel::load,
        onReport = viewModel::report,
        onClose = onClose,
        onPractiseAgain = onTryTopic,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { if (askBeforeLeaving) confirmExit = true else onClose() }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close quiz")
                    }
                },
                title = {
                    Column {
                        Text(
                            text = when (state) {
                                is QuizUiState.Active -> "Question ${state.index + 1} of ${state.questions.size}"
                                is QuizUiState.Finished -> "Quiz summary"
                                else -> "Quiz"
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        val topic = (state as? QuizUiState.Active)?.topicTitle ?: (state as? QuizUiState.Finished)?.topicTitle
                        if (topic != null) {
                            Text(topic, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
            )
        },
    ) { padding ->
        ContentColumn(Modifier.padding(padding)) {
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
private fun LoadingQuiz() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Preparing your questions…", style = MaterialTheme.typography.titleMedium)
        Text(
            "New AI questions can take up to half a minute.",
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
    val progress by animateFloatAsState(state.progress, label = "quizProgress")

    LaunchedEffect(state.feedback) {
        if (state.feedback != null && state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = { progress },
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.fillMaxWidth(),
        )
        AnimatedContent(
            targetState = state.index,
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 12 }) togetherWith fadeOut()
            },
            label = "question",
            modifier = Modifier.weight(1f),
        ) { index ->
            val question = state.questions[index]
            val scrollState = rememberScrollState()
            // Bring the explanation into view once an answer is checked.
            LaunchedEffect(state.feedback != null) {
                if (state.feedback != null) scrollState.animateScrollTo(scrollState.maxValue)
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            ) {
                state.notice?.takeIf { index == 0 }?.let { notice ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Outlined.CloudOff, null)
                            Text(notice, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SourceBadge(question.source)
                    Spacer(Modifier.weight(1f))
                    val reported = question.id in state.reportedIds
                    TextButton(onClick = { showReportSheet = true }, enabled = !reported) {
                        Icon(Icons.Outlined.Flag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(if (reported) "Reported" else "Report", Modifier.padding(start = 6.dp))
                    }
                }
                Text(question.prompt, style = MaterialTheme.typography.titleLarge)
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    question.options.forEachIndexed { optionIndex, option ->
                        OptionCard(
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
                            onClick = { onSelect(optionIndex) },
                        )
                    }
                }
                AnimatedVisibility(visible = state.feedback != null) {
                    state.feedback?.let { FeedbackPanel(question, it, state.dailyCapReached) }
                }
            }
        }
        Surface(tonalElevation = 2.dp) {
            Button(
                onClick = if (state.feedback == null) onCheck else onNext,
                enabled = (state.selectedIndex != null && !state.submitting) || state.feedback != null,
                modifier = Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 52.dp).testTag("quiz_primary_action"),
            ) {
                Text(
                    when {
                        state.feedback == null -> "Check answer"
                        state.isLastQuestion -> "See summary"
                        else -> "Next question"
                    },
                )
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
private fun OptionCard(
    letter: Char,
    text: String,
    selected: Boolean,
    result: OptionResult?,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val (border, container) = when (result) {
        OptionResult.CORRECT -> BorderStroke(2.dp, colors.tertiary) to colors.tertiaryContainer
        OptionResult.WRONG_CHOICE -> BorderStroke(2.dp, colors.error) to colors.errorContainer
        OptionResult.OTHER -> BorderStroke(1.dp, colors.outlineVariant) to colors.surface
        null -> if (selected) {
            BorderStroke(2.dp, colors.primary) to colors.primaryContainer
        } else {
            BorderStroke(1.dp, colors.outlineVariant) to colors.surface
        }
    }
    // Result is spoken as words so feedback never depends on colour alone.
    val resultLabel = when (result) {
        OptionResult.CORRECT -> "Correct answer"
        OptionResult.WRONG_CHOICE -> "Your answer, incorrect"
        else -> null
    }
    OutlinedCard(
        border = border,
        colors = CardDefaults.outlinedCardColors(containerColor = container),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = selected, enabled = result == null, role = Role.RadioButton, onClick = onClick)
            .semantics { resultLabel?.let { stateDescription = it } }
            .testTag("option_$letter"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Surface(shape = CircleShape, color = colors.surfaceContainerHighest, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(letter.toString(), style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
            when (result) {
                OptionResult.CORRECT -> Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = colors.tertiary)
                OptionResult.WRONG_CHOICE -> Icon(Icons.Outlined.Cancel, contentDescription = null, tint = colors.error)
                else -> if (result == null) RadioButton(selected = selected, onClick = null)
            }
        }
    }
}

@Composable
private fun FeedbackPanel(question: QuizQuestion, feedback: AnswerFeedback, capReached: Boolean) {
    val xpLine = when {
        feedback.xpAwarded > 0 -> "+${feedback.xpAwarded} XP for a correct first answer"
        feedback.correct && capReached -> "Today's XP limit is reached — your mastery still grows."
        feedback.correct -> "No XP this time: you've answered this question before."
        else -> null
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (feedback.correct) "Correct." else "Not quite — here's why:",
                style = MaterialTheme.typography.titleMedium,
            )
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
                    Icon(Icons.Outlined.Bolt, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                }
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
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Report this question", style = MaterialTheme.typography.titleLarge)
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
                            .heightIn(min = 48.dp)
                            .selectable(selected = reason == option, role = Role.RadioButton, onClick = { reason = option }),
                    ) {
                        RadioButton(selected = reason == option, onClick = null)
                        Text(option, Modifier.padding(start = 12.dp))
                    }
                }
            }
            Button(onClick = { reason?.let(onSubmit) }, enabled = reason != null, modifier = Modifier.fillMaxWidth()) {
                Text("Submit report")
            }
        }
    }
}

@Composable
private fun QuizSummary(state: QuizUiState.Finished, onDone: () -> Unit, onPractiseAgain: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Icon(Icons.Outlined.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        Text("${state.correct} of ${state.total} correct", style = MaterialTheme.typography.headlineMedium)
        Text(
            if (state.correct == state.total) "Every answer right — great work." else "Every question you check helps the next one stick.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // Effort and knowledge are shown as two separate cards on purpose (commitment 05).
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Effort", style = MaterialTheme.typography.labelLarge)
                Text("+${state.xpEarned} XP", style = MaterialTheme.typography.headlineSmall)
                Text(
                    if (state.dailyCapReached) "You've reached today's XP limit. Learning still counts towards mastery." else "XP rewards correct first answers.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mastery of ${state.topicTitle}", style = MaterialTheme.typography.labelLarge)
                Text("${(state.mastery * 100).roundToInt()}%", style = MaterialTheme.typography.headlineSmall)
                MasteryBadge(state.masteryLevel)
                Text("Mastery is an estimate of what you know, separate from XP.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Button(onClick = onPractiseAgain, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Practise this topic again") }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Done") }
    }
}
