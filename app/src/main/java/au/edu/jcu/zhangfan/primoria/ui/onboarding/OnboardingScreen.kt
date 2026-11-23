package au.edu.jcu.zhangfan.primoria.ui.onboarding

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
import androidx.compose.material.icons.outlined.AutoStories
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import au.edu.jcu.zhangfan.primoria.ui.components.DualRings
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.SegmentedProgress
import au.edu.jcu.zhangfan.primoria.ui.components.gradientBackground
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
    Box(Modifier.fillMaxSize().gradientBackground(PrimoriaTheme.colors.heroGradient)) {
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
                    if (state.step > 0) TextButton(onClick = onBack) { Text("Back") } else Spacer(Modifier)
                    Button(
                        onClick = if (state.isLastStep) onFinish else onNext,
                        enabled = state.canContinue,
                        modifier = Modifier.heightIn(min = 54.dp).testTag("onboarding_continue"),
                    ) {
                        Text(if (state.isLastStep) "Start learning" else "Continue", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    val colors = PrimoriaTheme.colors
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
        DualRings(
            outerProgress = 0.78f,
            outerColors = colors.masteryRing,
            innerProgress = 0.55f,
            innerColors = colors.effortRing,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            size = 120.dp,
            strokeWidth = 12.dp,
        )
        Icon(Icons.Outlined.AutoStories, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
    }
    Text("Welcome to Primoria", style = MaterialTheme.typography.displayMedium)
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
    PrimoriaCard(contentPadding = 16.dp, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            ) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp)) }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AgeStep(selected: AgeGroup?, onChoose: (AgeGroup) -> Unit) {
    Text("How old are you?", style = MaterialTheme.typography.displayMedium)
    Text(
        "This only changes guidance and defaults. It's stored on this device and never sent anywhere.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AgeOption(Icons.Outlined.School, "13–17", "Secondary school", selected == AgeGroup.TEEN) { onChoose(AgeGroup.TEEN) }
        AgeOption(Icons.Outlined.Work, "18 or older", "University or independent learner", selected == AgeGroup.ADULT) { onChoose(AgeGroup.ADULT) }
    }
}

@Composable
private fun AgeOption(icon: ImageVector, title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else PrimoriaTheme.colors.cardSurface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else PrimoriaTheme.colors.hairline),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
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
    Text("Your privacy, your choice", style = MaterialTheme.typography.displayMedium)
    PrimoriaCard(modifier = Modifier.fillMaxWidth()) {
        Text("What stays on this device", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        Text(
            "Your answers, scores, XP, mastery and settings. They're excluded from cloud backup and " +
                "answers older than your retention period (90 days by default) are deleted automatically.",
            style = MaterialTheme.typography.bodyMedium,
        )
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
                Text("Optional: AI-generated quizzes", style = MaterialTheme.typography.titleSmall)
                Text("Off unless you turn it on.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = state.aiConsent, onCheckedChange = null)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "If turned on, only the topic you type, the difficulty and your age band (13–17 or 18+) are sent " +
                "to DeepSeek, an AI service whose servers are in China, to write your questions. Your name, " +
                "scores and history are never sent. Leave it off to use the six built-in offline topics.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
