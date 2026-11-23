package au.edu.jcu.zhangfan.primoria.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
        topBar = {
            TopAppBar(
                title = { Text("My data") },
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
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            ) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Your learning history stays on this device", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "No account, no ads, no analytics, and no cloud backup. DeepSeek only ever receives " +
                                    "the topic text, difficulty and age band of a quiz you start.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
                item {
                    state.counts?.let { counts ->
                        OutlinedCard(Modifier.fillMaxWidth().testTag("data_counts")) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Stored right now", style = MaterialTheme.typography.titleSmall)
                                Text("${counts.topics} topics · ${counts.questions} questions", style = MaterialTheme.typography.bodyMedium)
                                Text("${counts.attempts} answers · ${counts.reports} reports", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item { SectionHeader("Keep answers for") }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DataRetention.OPTIONS_DAYS.forEach { days ->
                            FilterChip(
                                selected = state.retentionDays == days,
                                onClick = { onSetRetention(days) },
                                label = { Text(if (days == 365) "1 year" else "$days days") },
                            )
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
                item { SectionHeader("Topics") }
                if (state.topics.isEmpty()) {
                    item { Text("No topics stored.", style = MaterialTheme.typography.bodyMedium) }
                }
                items(state.topics, key = { it.id }) { topic ->
                    ListItem(
                        headlineContent = { Text(topic.title) },
                        supportingContent = { Text("${topic.attempts} answers") },
                        trailingContent = {
                            IconButton(onClick = { topicToDelete = topic }) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete ${topic.title}")
                            }
                        },
                    )
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
