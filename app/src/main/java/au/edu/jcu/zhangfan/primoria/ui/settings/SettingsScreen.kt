package au.edu.jcu.zhangfan.primoria.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.data.remote.KeySource
import au.edu.jcu.zhangfan.primoria.domain.model.AgeGroup
import au.edu.jcu.zhangfan.primoria.domain.model.Difficulty
import au.edu.jcu.zhangfan.primoria.domain.model.ThemeMode
import au.edu.jcu.zhangfan.primoria.domain.model.UserSettings
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaSwitch
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    val use24HourTime = DateFormat.is24HourFormat(LocalContext.current)
    var confirmTeenAi by rememberSaveable { mutableStateOf(false) }
    var editingKey by rememberSaveable { mutableStateOf(false) }

    ContentColumn(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 18.dp),
        ) {
            Text("Settings", style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(bottom = 4.dp).semantics { heading() })

            SettingsGroup("Learning", Icons.Outlined.AutoStories, emphasized = true) {
                Text("Difficulty", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp, bottom = 3.dp))
                SettingSegments(Difficulty.entries, settings.difficulty, { it.label }, { level -> onUpdate { it.copy(difficulty = level) } })
                SettingsChoiceRow("Questions per quiz") { modifier ->
                    SettingSegments(QUIZ_LENGTHS, settings.questionsPerQuiz, { it.toString() }, { count ->
                        onUpdate { it.copy(questionsPerQuiz = count) }
                    }, modifier)
                }
                SettingsDivider()
                PreferenceRow("Weekly goal", "Active days per week") {
                    WeeklyGoalStepper(settings.weeklyGoalDays) { step ->
                        onUpdate { current -> current.copy(weeklyGoalDays = (current.weeklyGoalDays + step).coerceIn(1, 7)) }
                    }
                }
            }

            SettingsGroup("Feedback", Icons.Outlined.GraphicEq) {
                SwitchRow("Sound effects", "Play a tone after checking an answer.", settings.soundEffects) { on ->
                    onUpdate { it.copy(soundEffects = on) }
                }
                SettingsDivider()
                SwitchRow("Vibration", "Vibrate after checking an answer.", settings.haptics) { on ->
                    onUpdate { it.copy(haptics = on) }
                }
            }

            SettingsGroup("Reminders", Icons.Outlined.NotificationsNone) {
                SwitchRow(
                    title = "Study reminder",
                    subtitle = "At most once a day. Skips days you’ve already studied.",
                    checked = settings.reminderEnabled,
                    testTag = "reminder_toggle",
                    onCheckedChange = onReminderChange,
                )
                if (notificationsBlocked) {
                    Text(
                        "Notifications are blocked, so reminders stay off. You can allow them in system settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                if (settings.reminderEnabled) {
                    SettingsDivider()
                    PreferenceRow("Remind me at") {
                        HourPicker("Reminder time", settings.reminderHour, REMINDER_HOURS, use24HourTime, onReminderHourChange)
                    }
                }
                SettingsDivider()
                PreferenceRow("Quiet hours start", "Ends at ${hourLabel(settings.quietHoursEnd, use24HourTime)}.") {
                    HourPicker("Quiet hours start", settings.quietHoursStart, QUIET_STARTS, use24HourTime) { hour ->
                        onUpdate { it.copy(quietHoursStart = hour) }
                    }
                }
            }

            SettingsGroup("Appearance & accessibility", Icons.Outlined.Brightness6) {
                Text("Theme", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp, bottom = 3.dp))
                SettingSegments(
                    ThemeMode.entries, settings.themeMode, { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                    { mode -> onUpdate { it.copy(themeMode = mode) } },
                )
                SwitchRow("Larger text", "Increases text size by 15%.", settings.largeText) { on ->
                    onUpdate { it.copy(largeText = on) }
                }
            }

            SettingsGroup("AI & privacy", Icons.Outlined.Shield) {
                SwitchRow(
                    title = "AI-generated quizzes",
                    subtitle = "Optional. You can always use the offline library.",
                    checked = settings.aiConsent,
                    testTag = "settings_ai_toggle",
                ) { on ->
                    if (on && settings.ageGroup == AgeGroup.TEEN) confirmTeenAi = true
                    else onUpdate { it.copy(aiConsent = on) }
                }
                SettingsChoiceRow("Age band") { modifier ->
                    SettingSegments(AgeGroup.entries, settings.ageGroup, { it.label }, { group ->
                        // Moving to the teen band switches AI off until it is confirmed again.
                        onUpdate { it.copy(ageGroup = group, aiConsent = it.aiConsent && group == AgeGroup.ADULT) }
                    }, modifier)
                }
                SettingsDivider()
                PreferenceRow(
                    "My data", "Review or delete learning data.",
                    Modifier.clickable(role = Role.Button, onClick = onOpenMyData).testTag("open_my_data"),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary)
                }
                SettingsDivider()
                PreferenceRow(
                    "DeepSeek key",
                    when (state.keySource) {
                        KeySource.USER -> "Personal key encrypted on this device."
                        KeySource.BUILD -> "Using the developer key built into this copy."
                        KeySource.NONE -> "No personal key added."
                    },
                ) {
                    if (state.keySource == KeySource.USER) {
                        TextButton(onClick = onClearKey) { Text("Remove") }
                    } else {
                        TextButton(onClick = { editingKey = true }) { Text("Add") }
                    }
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
                    "Please check with a parent or guardian first. DeepSeek receives the topic and quiz preferences for AI practice. " +
                        "A material quiz sends the excerpt you confirm. Your scores and history stay on this device.",
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

@Composable
private fun SettingsGroup(
    title: String,
    icon: ImageVector,
    emphasized: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val group: @Composable ColumnScope.() -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        }
        content()
    }
    if (emphasized) {
        PrimoriaCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), contentPadding = 13.dp, content = group)
    } else {
        Column(Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 5.dp), content = group)
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = PrimoriaTheme.colors.hairline)
}

@Composable
private fun SettingsChoiceRow(
    title: String,
    control: @Composable (Modifier) -> Unit,
) {
    if (LocalDensity.current.fontScale >= 1.5f) {
        Column(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            control(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            control(Modifier.width(140.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingSegments(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    val colors = MaterialTheme.colorScheme
    SingleChoiceSegmentedButtonRow(
        modifier.background(colors.surfaceContainerLow, RoundedCornerShape(11.dp)).padding(3.dp),
        space = 0.dp,
    ) {
        options.forEach { option ->
            SegmentedButton(
                selected = selected == option,
                onClick = { onSelect(option) },
                shape = RoundedCornerShape(8.dp),
                icon = {},
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.primaryContainer,
                    activeContentColor = colors.onPrimaryContainer,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = colors.onSurfaceVariant,
                    activeBorderColor = Color.Transparent,
                    inactiveBorderColor = Color.Transparent,
                ),
            ) {
                Text(label(option), style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected == option) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun WeeklyGoalStepper(days: Int, onStep: (Int) -> Unit) {
    val colors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        FilledTonalIconButton(
            onClick = { onStep(-1) }, enabled = days > 1, modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(11.dp), colors = colors,
        ) { Icon(Icons.Outlined.Remove, "Decrease weekly goal", modifier = Modifier.size(18.dp)) }
        Text(
            "$days ${if (days == 1) "day" else "days"}",
            style = MaterialTheme.typography.labelMedium.tabular(),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.testTag("weekly_goal_days"),
        )
        FilledTonalIconButton(
            onClick = { onStep(1) }, enabled = days < 7, modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(11.dp), colors = colors,
        ) { Icon(Icons.Outlined.Add, "Increase weekly goal", modifier = Modifier.size(18.dp)) }
    }
}

private val QUIZ_LENGTHS = listOf(5, 10)
private val QUIET_STARTS = listOf(20, 21, 22)
private val REMINDER_HOURS = (7..20).toList()

private fun hourLabel(hour: Int, use24HourTime: Boolean): String =
    LocalTime.of(hour, 0).format(DateTimeFormatter.ofPattern(if (use24HourTime) "HH:mm" else "h:mm a"))

@Composable
private fun HourPicker(
    label: String,
    selected: Int,
    options: List<Int>,
    use24HourTime: Boolean,
    onSelect: (Int) -> Unit,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { open = true },
            shape = RoundedCornerShape(9.dp),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 7.dp),
            modifier = Modifier.heightIn(min = 48.dp),
        ) {
            Text(hourLabel(selected, use24HourTime), style = MaterialTheme.typography.labelMedium.tabular())
            Icon(Icons.Outlined.KeyboardArrowDown, label, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { hour ->
                DropdownMenuItem(text = { Text(hourLabel(hour, use24HourTime)) }, onClick = {
                    onSelect(hour)
                    open = false
                })
            }
        }
    }
}

@Composable
private fun PreferenceRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        modifier.fillMaxWidth().heightIn(min = 52.dp).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String = title,
    onCheckedChange: (Boolean) -> Unit,
) {
    // The whole row is one toggle target so TalkBack reads title, subtitle and state together.
    PreferenceRow(
        title, subtitle,
        Modifier.toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch).testTag(testTag),
    ) { PrimoriaSwitch(checked = checked) }
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
