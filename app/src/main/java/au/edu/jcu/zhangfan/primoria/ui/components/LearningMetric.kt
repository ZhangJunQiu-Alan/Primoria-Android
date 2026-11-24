package au.edu.jcu.zhangfan.primoria.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import au.edu.jcu.zhangfan.primoria.ui.theme.tabular

@Composable
internal fun LearningMetric(
    label: String,
    value: String,
    detail: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Surface(modifier, shape = RoundedCornerShape(17.dp), color = container, contentColor = content) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineLarge.tabular())
            Text(detail, style = MaterialTheme.typography.bodySmall)
        }
    }
}
