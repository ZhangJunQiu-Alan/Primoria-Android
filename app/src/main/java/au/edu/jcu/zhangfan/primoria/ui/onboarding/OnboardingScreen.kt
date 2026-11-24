package au.edu.jcu.zhangfan.primoria.ui.onboarding

import androidx.compose.material.icons.outlined.Send
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.CircleShape
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.SegmentedProgress
import au.edu.jcu.zhangfan.primoria.ui.home.StudyArtwork
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(enabled = state.step > 0) { viewModel.back() }
    OnboardingScreen(
        state = state,
        onNext = viewModel::next,
        onBack = viewModel::back,
        onChooseAge = viewModel::chooseAge,
        onAiConsentChange = viewModel::setAiConsent,
        onGuardianAwareChange = viewModel::setGuardianAware,
        onFinish = {
            viewModel.finish()
            onFinished()
        },
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onChooseAge: (AgeGroup) -> Unit,
    onAiConsentChange: (Boolean) -> Unit,
    onGuardianAwareChange: (Boolean) -> Unit,
    onFinish: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        ContentColumn(Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
            Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(16.dp))
                SegmentedProgress(total = 3, current = state.step, answered = state.step)
                Text(
                    text = "Step ${state.step + 1} of 3",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp).semantics { liveRegion = LiveRegionMode.Polite },
                )
                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = { (slideInHorizontally(tween(300)) { it / 10 } + fadeIn(tween(300))) togetherWith fadeOut(tween(150)) },
                    label = "onboardingStep",
                    modifier = Modifier.weight(1f),
                ) { step ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 24.dp),
                    ) {
                        when (step) {
                            0 -> WelcomeStep()
                            1 -> AgeStep(state.ageGroup, onChooseAge)
                            else -> ConsentStep(state, onAiConsentChange, onGuardianAwareChange)
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                ) {
                    if (state.step > 0) {
                        TextButton(onClick = onBack, modifier = Modifier.padding(end = 10.dp)) { Text("Back") }
                    }
                    Button(
                        onClick = if (state.isLastStep) onFinish else onNext,
                        enabled = state.canContinue,
                        modifier = Modifier.weight(1f).heightIn(min = 54.dp).testTag("onboarding_continue"),
                        shape = RoundedCornerShape(15.dp),
                    ) {
                        Text(if (state.isLastStep) "Start learning" else "Continue", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.WelcomeStep() {
    Box(
        Modifier.fillMaxWidth().height(172.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF3835BB)),
        contentAlignment = Alignment.Center,
    ) {
        StudyArtwork(Icons.Outlined.Eco, Modifier.size(198.dp))
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Small quizzes. Big ideas.", style = MaterialTheme.typography.displayMedium)
        Text("Learn one concept at your own pace with Primoria.", style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PromiseRow(Icons.Outlined.TipsAndUpdates, "Practise a topic", "Short quizzes with an explanation after every answer.")
        PromiseRow(Icons.Outlined.Spa, "Build a weekly rhythm", "Pick a goal you can keep and take rest days.")
        PromiseRow(Icons.Outlined.Lock, "Keep history on your device", "No account or ads. AI practice is optional.")
    }
}

@Composable
private fun PromiseRow(icon: ImageVector, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        ) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ColumnScope.AgeStep(selected: AgeGroup?, onChoose: (AgeGroup) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Choose your age range", style = MaterialTheme.typography.displaySmall)
        Text(
            "Your age range helps us choose suitable quiz guidance. We don’t ask for your birth date.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Column(Modifier.selectableGroup().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AgeOption(Icons.Outlined.School, "13–17", "Secondary school", selected == AgeGroup.TEEN) { onChoose(AgeGroup.TEEN) }
        AgeOption(Icons.Outlined.Work, "18 or older", "University or independent learner", selected == AgeGroup.ADULT) { onChoose(AgeGroup.ADULT) }
    }
    PrimoriaCard(Modifier.fillMaxWidth(), contentPadding = 13.dp,
        containerColor = MaterialTheme.colorScheme.primaryContainer, border = false) {
        ConsentPoint(Icons.Outlined.Lock, "If you enable AI, DeepSeek uses your age range to choose appropriate questions.")
    }
}

@Composable
private fun AgeOption(icon: ImageVector, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else PrimoriaTheme.colors.cardSurface,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else PrimoriaTheme.colors.hairline),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(
                    if (selected) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.primaryContainer,
                ),
            ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            RadioButton(selected = selected, onClick = null)
        }
    }
}

@Composable
private fun ConsentStep(
    state: OnboardingUiState,
    onAiConsentChange: (Boolean) -> Unit,
    onGuardianAwareChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Your privacy, your choice", style = MaterialTheme.typography.displaySmall)
        Text(
            "Choose whether Primoria can use DeepSeek to generate quizzes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    PrimoriaCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = PrimoriaTheme.colors.masteryContainer,
        border = false,
    ) {
        Text("Stored on this device", style = MaterialTheme.typography.titleLarge, color = PrimoriaTheme.colors.onMasteryContainer)
        Text("Your answers, XP, mastery and quiz history. Older answers are deleted after 90 days by default.",
            style = MaterialTheme.typography.bodyMedium, color = PrimoriaTheme.colors.onMasteryContainer)
    }
    PrimoriaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = state.aiConsent, onValueChange = onAiConsentChange, role = Role.Switch)
                .testTag("ai_consent_toggle"),
        ) {
            Column(Modifier.weight(1f)) {
                Text("Optional: AI-generated quizzes", style = MaterialTheme.typography.titleLarge)
                Text("Off unless you turn it on.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PrimoriaSwitch(checked = state.aiConsent)
        }
        Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ConsentPoint(Icons.Outlined.Send, "AI practice sends the topic, quiz preferences and age range to DeepSeek in China.")
            ConsentPoint(Icons.Outlined.Lock, "A quiz from study material sends the text you preview and confirm.")
        }
    }
    if (state.ageGroup == AgeGroup.TEEN) {
        PrimoriaCard(modifier = Modifier.fillMaxWidth(), contentPadding = 8.dp) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = state.guardianAware, onValueChange = onGuardianAwareChange, role = Role.Checkbox)
                    .padding(8.dp)
                    .testTag("guardian_checkbox")
                    .semantics { contentDescription = "A parent or guardian knows I'm using Primoria and what the AI option does." },
            ) {
                Checkbox(checked = state.guardianAware, onCheckedChange = null)
                Text(
                    "A parent or guardian knows I'm using Primoria and what the AI option does.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
    Text(
        "You can change any of this later in Settings, and delete everything under Settings › My data.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ConsentPoint(icon: ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
