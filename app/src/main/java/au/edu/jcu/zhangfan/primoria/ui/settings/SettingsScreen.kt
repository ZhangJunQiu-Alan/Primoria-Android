package au.edu.jcu.zhangfan.primoria.ui.settings

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
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
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard

@Composable
fun SettingsRoute(
    onOpenMyData: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var notificationsBlocked by rememberSaveable { mutableStateOf(false) }
    // Commitment 02 — just-in-time permission: asked only when the learner turns reminders on.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsBlocked = !granted
        if (granted) viewModel.setReminder(true)
    }
    val context = LocalContext.current
    SettingsScreen(
        state = state,
        notificationsBlocked = notificationsBlocked,
        onReminderChange = { enabled ->
            val needsPermission = enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            if (needsPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificationsBlocked = false
                viewModel.setReminder(enabled)
            }
        },
        onReminderHourChange = { hour -> viewModel.setReminder(true, hour) },
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
    notificationsBlocked: Boolean,
    onReminderChange: (Boolean) -> Unit,
    onReminderHourChange: (Int) -> Unit,
    onUpdate: ((UserSettings) -> UserSettings) -> Unit,
    onSaveKey: (String) -> Unit,
    onClearKey: () -> Unit,
    onOpenMyData: () -> Unit,
) {
    val settings = state.settings
    var confirmTeenAi by rememberSaveable { mutableStateOf(false) }
    var editingKey by rememberSaveable { mutableStateOf(false) }

    ContentColumn(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
            ) {
                Text("Settings", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(top = 16.dp))
                SettingsGroup("Learning") {
                FieldLabel("Difficulty")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Difficulty.entries.forEachIndexed { index, difficulty ->
                        SegmentedButton(
                            selected = settings.difficulty == difficulty,
                            onClick = { onUpdate { it.copy(difficulty = difficulty) } },
                            shape = SegmentedButtonDefaults.itemShape(index, Difficulty.entries.size),
                            colors = segmentedColors(),
                        ) { Text(difficulty.label) }
                    }
                }
                FieldLabel("Questions per quiz", Modifier.padding(top = 8.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    QUIZ_LENGTHS.forEachIndexed { index, count ->
                        SegmentedButton(
                            selected = settings.questionsPerQuiz == count,
                            onClick = { onUpdate { it.copy(questionsPerQuiz = count) } },
                            shape = SegmentedButtonDefaults.itemShape(index, QUIZ_LENGTHS.size),
                            colors = segmentedColors(),
                        ) { Text("$count") }
                    }
                }
                FieldLabel(
                    "Weekly goal: ${settings.weeklyGoalDays} active ${if (settings.weeklyGoalDays == 1) "day" else "days"}",
                    Modifier.padding(top = 8.dp),
                )
                PrimoriaSlider(
                    value = settings.weeklyGoalDays.toFloat(),
                    onValueChange = { value -> onUpdate { it.copy(weeklyGoalDays = value.toInt()) } },
                    valueRange = 1f..7f,
                    steps = 5,
                    description = "Weekly goal in active days",
                )
                Row(Modifier.fillMaxWidth()) {
                    Text("1 day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Text("7 days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "Choose a rhythm you can keep. Streak freezes cover busy weeks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                }
                SettingsGroup("Feedback") {
                SwitchRow("Sound effects", "A short tone after each answer.", settings.soundEffects) { on ->
                    onUpdate { it.copy(soundEffects = on) }
                }
                SwitchRow("Vibration", "A gentle buzz when an answer is checked.", settings.haptics) { on ->
                    onUpdate { it.copy(haptics = on) }
                }

                }
                SettingsGroup("Reminders") {
                SwitchRow(
                    title = "Study reminder",
                    subtitle = "Off by default. At most one a day, never in quiet hours, and none once you've studied.",
                    checked = settings.reminderEnabled,
                    testTag = "reminder_toggle",
                    onCheckedChange = onReminderChange,
                )
                if (notificationsBlocked) {
                    Text(
                        "Notifications are blocked, so reminders stay off. You can allow them in system settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (settings.reminderEnabled) {
                    FieldLabel("Remind me at ${hourLabel(settings.reminderHour)}")
                    PrimoriaSlider(
                        value = settings.reminderHour.toFloat(),
                        onValueChange = { onReminderHourChange(it.toInt()) },
                        valueRange = 7f..20f,
                        steps = 12,
                        description = "Reminder time",
                    )
                }
                FieldLabel("Quiet hours start")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    QUIET_STARTS.forEachIndexed { index, hour ->
                        SegmentedButton(
                            selected = settings.quietHoursStart == hour,
                            onClick = { onUpdate { it.copy(quietHoursStart = hour) } },
                            shape = SegmentedButtonDefaults.itemShape(index, QUIET_STARTS.size),
                            colors = segmentedColors(),
                        ) { Text(hourLabel(hour)) }
                    }
                }
                Text(
                    "Quiet from ${hourLabel(settings.quietHoursStart)} until ${hourLabel(settings.quietHoursEnd)}: Primoria never notifies you then.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                }
                SettingsGroup("Appearance & accessibility") {
                FieldLabel("Theme")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { onUpdate { it.copy(themeMode = mode) } },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                            colors = segmentedColors(),
                        ) { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase)) }
                    }
                }
                SwitchRow("Larger text", "Makes text 15% bigger on top of your system font size.", settings.largeText) { on ->
                    onUpdate { it.copy(largeText = on) }
                }

                }
                SettingsGroup("AI & privacy") {
                SwitchRow(
                    title = "AI-generated quizzes",
                    subtitle = "Sends only your topic, the difficulty and your age band to DeepSeek (servers in China).",
                    checked = settings.aiConsent,
                    testTag = "settings_ai_toggle",
                ) { on ->
                    if (on && settings.ageGroup == AgeGroup.TEEN) confirmTeenAi = true
                    else onUpdate { it.copy(aiConsent = on) }
                }
                FieldLabel("Age band")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    AgeGroup.entries.forEachIndexed { index, group ->
                        SegmentedButton(
                            selected = settings.ageGroup == group,
                            onClick = {
                                // Moving to the teen band switches AI off until it is confirmed again.
                                onUpdate { it.copy(ageGroup = group, aiConsent = it.aiConsent && group == AgeGroup.ADULT) }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, AgeGroup.entries.size),
                            colors = segmentedColors(),
                        ) { Text(group.label) }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = PrimoriaTheme.colors.hairline)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text("DeepSeek key", style = MaterialTheme.typography.titleLarge)
                        Text(
                            when (state.keySource) {
                                KeySource.USER -> "Using your own key, encrypted on this device."
                                KeySource.BUILD -> "Using the developer key built into this copy."
                                KeySource.NONE -> "No key set — quizzes use saved and offline questions."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.keySource == KeySource.USER) {
                        TextButton(onClick = onClearKey) { Text("Remove") }
                    } else {
                        TextButton(onClick = { editingKey = true }) { Text("Add") }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = PrimoriaTheme.colors.hairline)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .toggleable(value = false, onValueChange = { onOpenMyData() }, role = Role.Button)
                        .testTag("open_my_data"),
                ) {
                    Column(Modifier.weight(1f).padding(end = 12.dp)) {
                        Text("My data", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "See, expire or delete everything Primoria stores.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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

/** A quiet uppercase label above one card of related settings. */
@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp).semantics { heading() },
        )
        PrimoriaCard(Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
        }
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = modifier)
}

/** Round thumb and a light track, matching the rest of the rounded UI. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrimoriaSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    description: String,
) {
    val colors = SliderDefaults.colors(
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
        inactiveTickColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    )
    val interaction = remember { MutableInteractionSource() }
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        colors = colors,
        interactionSource = interaction,
        thumb = {
            Box(
                Modifier
                    .size(22.dp)
                    .shadow(2.dp, CircleShape)
                    .background(PrimoriaTheme.colors.cardSurface, CircleShape)
                    .border(6.dp, MaterialTheme.colorScheme.primary, CircleShape),
            )
        },
        track = { state -> SliderDefaults.Track(sliderState = state, colors = colors, thumbTrackGapSize = 0.dp, modifier = Modifier.height(6.dp)) },
        modifier = Modifier.semantics { contentDescription = description },
    )
}

private val QUIZ_LENGTHS = listOf(5, 10)
private val QUIET_STARTS = listOf(20, 21, 22)

private fun hourLabel(hour: Int): String = "%02d:00".format(hour)

/** Selected segments use the primary role; amber is reserved for effort and XP. */
@Composable
private fun segmentedColors() = SegmentedButtonDefaults.colors(
    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    activeBorderColor = MaterialTheme.colorScheme.outlineVariant,
    inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant,
)

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
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        PrimoriaSwitch(checked = checked)
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
