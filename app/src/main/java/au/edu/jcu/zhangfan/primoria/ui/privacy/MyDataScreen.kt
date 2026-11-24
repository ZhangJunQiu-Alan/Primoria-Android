package au.edu.jcu.zhangfan.primoria.ui.privacy

import au.edu.jcu.zhangfan.primoria.ui.theme.tabular
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import au.edu.jcu.zhangfan.primoria.ui.components.PrimoriaCard
import au.edu.jcu.zhangfan.primoria.ui.components.SubjectArt
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.edu.jcu.zhangfan.primoria.domain.logic.DataRetention
import au.edu.jcu.zhangfan.primoria.domain.model.TopicSummary
import au.edu.jcu.zhangfan.primoria.ui.components.ContentColumn
import au.edu.jcu.zhangfan.primoria.ui.components.SectionHeader

@Composable
fun MyDataRoute(
    onBack: () -> Unit,
    viewModel: MyDataViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MyDataScreen(
        state = state,
        onBack = onBack,
        onSetRetention = viewModel::setRetention,
        onDeleteTopic = viewModel::deleteTopic,
        onDeleteEverything = viewModel::deleteEverything,
        onMessageShown = viewModel::messageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDataScreen(
    state: MyDataUiState,
    onBack: () -> Unit,
    onSetRetention: (Int) -> Unit,
    onDeleteTopic: (TopicSummary) -> Unit,
    onDeleteEverything: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var topicToDelete by remember { mutableStateOf<TopicSummary?>(null) }
    var confirmWipe by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { Text("My data", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        ContentColumn(Modifier.padding(padding)) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            ) {
                item {
                    PrimoriaCard(Modifier.fillMaxWidth().padding(top = 4.dp),
                        containerColor = PrimoriaTheme.colors.masteryContainer, border = false) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                            ) { Icon(Icons.Outlined.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Stored on this device", style = MaterialTheme.typography.titleLarge, color = PrimoriaTheme.colors.onMasteryContainer)
                                Text(
                                                                        "Your answers, XP and mastery are saved locally. If you create a quiz from a file, DeepSeek receives the excerpt you confirm.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimoriaTheme.colors.onMasteryContainer,
                                )
                            }
                        }
                        state.counts?.let { counts ->
                            HorizontalDivider(Modifier.padding(vertical = 14.dp), color = PrimoriaTheme.colors.hairline)
                            Column(Modifier.testTag("data_counts"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("Storage summary", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${counts.topics} topics · ${counts.questions} questions", style = MaterialTheme.typography.bodyMedium.tabular())
                                Text("${counts.attempts} answers · ${counts.reports} reports", style = MaterialTheme.typography.bodyMedium.tabular())
                            }
                        }
                    }
                }
                item { SectionHeader("Keep answers for", Modifier.padding(top = 12.dp)) }
                item {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        DataRetention.OPTIONS_DAYS.forEachIndexed { index, days ->
                            SegmentedButton(
                                selected = state.retentionDays == days,
                                onClick = { onSetRetention(days) },
                                shape = SegmentedButtonDefaults.itemShape(index, DataRetention.OPTIONS_DAYS.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    activeBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    inactiveBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                ),
                            ) { Text(if (days == 365) "1 year" else "$days days") }
                        }
                    }
                }
                item {
                    Text(
                        "Older answers are deleted automatically each time Primoria opens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item { SectionHeader("Saved topics", Modifier.padding(top = 12.dp)) }
                if (state.topics.isEmpty()) {
                    item { Text("No topics stored.", style = MaterialTheme.typography.bodyMedium) }
                }
                if (state.topics.isNotEmpty()) item {
                    PrimoriaCard(Modifier.fillMaxWidth(), contentPadding = 4.dp) {
                        state.topics.forEachIndexed { index, topic ->
                            if (index > 0) HorizontalDivider(color = PrimoriaTheme.colors.hairline, modifier = Modifier.padding(horizontal = 16.dp))
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = {
                                    val subject = SubjectArt.colors(topic.title)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(subject.container),
                                    ) { Icon(SubjectArt.icon(topic.title), null, tint = subject.content, modifier = Modifier.size(20.dp)) }
                                },
                                headlineContent = { Text(topic.title, style = MaterialTheme.typography.titleLarge) },
                                supportingContent = {
                                    Text(if (topic.attempts == 0) "No answers yet" else "${topic.attempts} answers")
                                },
                                trailingContent = {
                                    if (topic.attempts > 0 || topic.isStudyMaterial) {
                                        IconButton(onClick = { topicToDelete = topic }) {
                                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete ${topic.title}")
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = { confirmWipe = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .padding(vertical = 8.dp)
                            .testTag("delete_everything"),
                    ) { Text("Delete all my data") }
                }
            }
        }
    }

    topicToDelete?.let { topic ->
        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            title = { Text("Delete \"${topic.title}\"?") },
            text = { Text("This removes the topic, its questions, your answers and its mastery.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTopic(topic)
                    topicToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { topicToDelete = null }) { Text("Keep") } },
        )
    }

    if (confirmWipe) {
        AlertDialog(
            onDismissRequest = { confirmWipe = false },
            title = { Text("Delete all Primoria data?") },
            text = {
                Text(
                    "This permanently erases your answers, XP, mastery, reports, settings and any saved key, " +
                        "then restarts setup. It can't be undone.",
                )
            },
            // The safe choice is listed first and is the dismiss action.
            dismissButton = { TextButton(onClick = { confirmWipe = false }) { Text("Keep my data") } },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmWipe = false
                        onDeleteEverything()
                    },
                    modifier = Modifier.testTag("confirm_delete_everything"),
                ) { Text("Delete everything", color = MaterialTheme.colorScheme.error) }
            },
        )
    }
}
