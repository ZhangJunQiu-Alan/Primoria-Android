package au.edu.jcu.zhangfan.primoria.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun MaterialDialog(
    state: MaterialUiState,
    aiEnabled: Boolean,
    onChoose: () -> Unit,
    onGenerate: () -> Unit,
    onSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (state == MaterialUiState.Closed || state is MaterialUiState.Ready) return
    val preview = state as? MaterialUiState.Preview
    val working = state == MaterialUiState.Reading || state is MaterialUiState.Generating
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create from study material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (state) {
                    MaterialUiState.Choose -> Text("Choose a PDF, TXT or Markdown file. You can review its text before generating the quiz.")
                    MaterialUiState.Reading -> Text("Reading the selected file on your device…")
                    is MaterialUiState.Generating -> Text("Generating questions from ${state.title}…")
                    is MaterialUiState.Failed -> Text(state.message, color = MaterialTheme.colorScheme.error)
                    is MaterialUiState.Preview -> {
                        Text(state.source.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.source.limited) "Only the excerpt below will be used. The file has more text."
                            else "Review the extracted text below.",
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Text(
                                state.source.excerpt,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 148.dp)
                                    .verticalScroll(rememberScrollState()).padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(
                            "Generate quiz sends this text, your quiz preferences and age range to DeepSeek. Your original file stays on your device. Check the excerpt for private information.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (!aiEnabled) Text("AI is off. You can enable it in Settings.")
                        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                    else -> Unit
                }
                if (working) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            when {
                working -> Unit
                preview == null -> Button(onClick = onChoose) { Text("Choose file") }
                aiEnabled -> Button(onClick = onGenerate) { Text("Generate quiz") }
                else -> Button(onClick = onSettings) { Text("Open Settings") }
            }
        },
        dismissButton = {
            if (preview != null) TextButton(onClick = onChoose) { Text("Change file") }
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
