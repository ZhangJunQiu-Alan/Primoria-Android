package au.edu.jcu.zhangfan.primoria.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.data.remote.KeySource
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import au.edu.jcu.zhangfan.primoria.domain.model.ThemeMode
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.SectionHeader

@Composable
fun SettingsRoute(
    onOpenMyData: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onUpdate = viewModel::update,
        onSaveKey = viewModel::saveApiKey,
        onClearKey = viewModel::clearApiKey,
        onOpenMyData = onOpenMyData,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onUpdate: ((UserSettings) -> UserSettings) -> Unit,
    onSaveKey: (String) -> Unit,
    onClearKey: () -> Unit,
    onOpenMyData: () -> Unit,
) {
    val settings = state.settings
    var confirmTeenAi by rememberSaveable { mutableStateOf(false) }
    var editingKey by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        ContentColumn(Modifier.padding(padding)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            ) {
                SectionHeader("Learning")
                Text("Difficulty", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Difficulty.entries.forEachIndexed { index, difficulty ->
                        SegmentedButton(
                            selected = settings.difficulty == difficulty,
                            onClick = { onUpdate { it.copy(difficulty = difficulty) } },
                            shape = SegmentedButtonDefaults.itemShape(index, Difficulty.entries.size),
                        ) { Text(difficulty.label) }
                    }
                }
                Text("Questions per quiz", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    QUIZ_LENGTHS.forEachIndexed { index, count ->
                        SegmentedButton(
                            selected = settings.questionsPerQuiz == count,
                            onClick = { onUpdate { it.copy(questionsPerQuiz = count) } },
                            shape = SegmentedButtonDefaults.itemShape(index, QUIZ_LENGTHS.size),
                        ) { Text("$count") }
                    }
                }
                Text(
                    "Weekly goal: ${settings.weeklyGoalDays} active ${if (settings.weeklyGoalDays == 1) "day" else "days"}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Slider(
                    value = settings.weeklyGoalDays.toFloat(),
                    onValueChange = { value -> onUpdate { it.copy(weeklyGoalDays = value.toInt()) } },
                    valueRange = 1f..7f,
                    steps = 5,
                    modifier = Modifier.semantics { contentDescription = "Weekly goal in active days" },
                )
                Text(
                    "Choose a rhythm you can keep. Streak freezes cover busy weeks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                SectionHeader("Feedback")
                SwitchRow("Sound effects", "A short tone after each answer.", settings.soundEffects) { on ->
                    onUpdate { it.copy(soundEffects = on) }
                }
                SwitchRow("Vibration", "A gentle buzz when an answer is checked.", settings.haptics) { on ->
                    onUpdate { it.copy(haptics = on) }
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                SectionHeader("Appearance & accessibility")
                Text("Theme", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { onUpdate { it.copy(themeMode = mode) } },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                        ) { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase)) }
                    }
                }
                SwitchRow("Larger text", "Makes text 15% bigger on top of your system font size.", settings.largeText) { on ->
                    onUpdate { it.copy(largeText = on) }
                }

                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                SectionHeader("AI & privacy")
                SwitchRow(
                    title = "AI-generated quizzes",
                    subtitle = "Sends only your topic, the difficulty and your age band to DeepSeek (servers in China).",
                    checked = settings.aiConsent,
                    testTag = "settings_ai_toggle",
                ) { on ->
                    if (on && settings.ageGroup == AgeGroup.TEEN) confirmTeenAi = true
                    else onUpdate { it.copy(aiConsent = on) }
                }
                Text("Age band", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    AgeGroup.entries.forEachIndexed { index, group ->
                        SegmentedButton(
                            selected = settings.ageGroup == group,
                            onClick = {
                                // Moving to the teen band switches AI off until it is confirmed again.
                                onUpdate { it.copy(ageGroup = group, aiConsent = it.aiConsent && group == AgeGroup.ADULT) }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, AgeGroup.entries.size),
                        ) { Text(group.label) }
                    }
                }
                ListItem(
                    headlineContent = { Text("DeepSeek key") },
                    supportingContent = {
                        Text(
                            when (state.keySource) {
                                KeySource.USER -> "Using your own key, encrypted on this device."
                                KeySource.BUILD -> "Using the developer key built into this copy."
                                KeySource.NONE -> "No key set — quizzes use saved and offline questions."
                            },
                        )
                    },
                    trailingContent = {
                        if (state.keySource == KeySource.USER) {
                            TextButton(onClick = onClearKey) { Text("Remove") }
                        } else {
                            TextButton(onClick = { editingKey = true }) { Text("Add") }
                        }
                    },
                )
                ListItem(
                    headlineContent = { Text("My data") },
                    supportingContent = { Text("See, expire or delete everything Primoria stores.") },
                    leadingContent = { Icon(Icons.Outlined.Shield, null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null) },
                    modifier = Modifier
                        .toggleable(value = false, onValueChange = { onOpenMyData() }, role = Role.Button)
                        .testTag("open_my_data"),
                )
            }
        }
    }

    if (confirmTeenAi) {
        AlertDialog(
            onDismissRequest = { confirmTeenAi = false },
            title = { Text("Turn on AI quizzes?") },
            text = {
                Text(
                    "Please check with a parent or guardian first. Only your topic, the difficulty and your age band " +
                        "are sent to DeepSeek — never your name, scores or history.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmTeenAi = false
                    onUpdate { it.copy(aiConsent = true) }
                }) { Text("They know — turn on") }
            },
            dismissButton = { TextButton(onClick = { confirmTeenAi = false }) { Text("Not now") } },
        )
    }

    if (editingKey) {
        ApiKeyDialog(
            onDismiss = { editingKey = false },
            onSave = { key ->
                onSaveKey(key)
                editingKey = false
            },
        )
    }
}

private val QUIZ_LENGTHS = listOf(5, 10)

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String = title,
    onCheckedChange: (Boolean) -> Unit,
) {
    // The whole row is one toggle target so TalkBack reads title, subtitle and state together.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch)
            .padding(vertical = 8.dp)
            .testTag(testTag),
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun ApiKeyDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var key by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Use your own DeepSeek key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("The key is encrypted with the Android Keystore and never leaves this device except to call DeepSeek.")
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it.trim() },
                    label = { Text("API key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(key) }, enabled = key.length >= 10) { Text("Save") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
