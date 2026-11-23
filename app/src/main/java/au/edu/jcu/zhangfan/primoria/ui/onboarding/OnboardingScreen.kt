package au.edu.jcu.zhangfan.primoria.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn

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
    Scaffold(
        bottomBar = {
            ContentColumn {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    if (state.step > 0) TextButton(onClick = onBack) { Text("Back") } else Spacer(Modifier)
                    Button(
                        onClick = if (state.isLastStep) onFinish else onNext,
                        enabled = state.canContinue,
                        modifier = Modifier.testTag("onboarding_continue"),
                    ) {
                        Text(if (state.isLastStep) "Start learning" else "Continue")
                    }
                }
            }
        },
    ) { padding ->
        ContentColumn(Modifier.padding(padding)) {
            Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { (state.step + 1) / 3f },
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Step ${state.step + 1} of 3",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
                AnimatedContent(
                    targetState = state.step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboardingStep",
                ) { step ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 24.dp),
                    ) {
                        when (step) {
                            0 -> WelcomeStep()
                            1 -> AgeStep(state.ageGroup, onChooseAge)
                            else -> ConsentStep(state, onAiConsentChange, onGuardianAwareChange)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Icon(
        Icons.Outlined.AutoStories,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(56.dp),
    )
    Text("Welcome to Primoria", style = MaterialTheme.typography.headlineMedium)
    Text(
        "Learn any topic at your own pace — no timers, no pressure.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    PromiseRow(Icons.Outlined.TipsAndUpdates, "Practise anything", "Type a topic and get a short quiz with clear explanations.")
    PromiseRow(Icons.Outlined.Spa, "Progress without pressure", "Goals are weekly, rest days are fine, and XP has a daily limit.")
    PromiseRow(Icons.Outlined.Lock, "Your data stays here", "No account, no ads. Your history lives on this device and you can delete it any time.")
}

@Composable
private fun PromiseRow(icon: ImageVector, title: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AgeStep(selected: AgeGroup?, onChoose: (AgeGroup) -> Unit) {
    Text("How old are you?", style = MaterialTheme.typography.headlineMedium)
    Text(
        "This only changes guidance and defaults. It's stored on this device and never sent anywhere.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AgeOption("13–17", "Secondary school", selected == AgeGroup.TEEN) { onChoose(AgeGroup.TEEN) }
        AgeOption("18 or older", "University or independent learner", selected == AgeGroup.ADULT) { onChoose(AgeGroup.ADULT) }
    }
}

@Composable
private fun AgeOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedCard(
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            RadioButton(selected = selected, onClick = null)
            Column(Modifier.padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ConsentStep(
    state: OnboardingUiState,
    onAiConsentChange: (Boolean) -> Unit,
    onGuardianAwareChange: (Boolean) -> Unit,
) {
    Text("Your privacy, your choice", style = MaterialTheme.typography.headlineMedium)
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("What stays on this device", style = MaterialTheme.typography.titleSmall)
            Text(
                "Your answers, scores, XP, mastery and settings. They're excluded from cloud backup and " +
                    "answers older than your retention period (90 days by default) are deleted automatically.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = state.aiConsent, onValueChange = onAiConsentChange, role = Role.Switch)
                    .testTag("ai_consent_toggle"),
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Optional: AI-generated quizzes", style = MaterialTheme.typography.titleSmall)
                    Text("Off unless you turn it on.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = state.aiConsent, onCheckedChange = null)
            }
            Text(
                "If turned on, only the topic you type, the difficulty and your age band (13–17 or 18+) are sent " +
                    "to DeepSeek, an AI service whose servers are in China, to write your questions. Your name, " +
                    "scores and history are never sent. Leave it off to use the six built-in offline topics.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (state.ageGroup == AgeGroup.TEEN) {
        OutlinedCard(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(value = state.guardianAware, onValueChange = onGuardianAwareChange, role = Role.Checkbox)
                    .padding(16.dp)
                    .testTag("guardian_checkbox"),
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
