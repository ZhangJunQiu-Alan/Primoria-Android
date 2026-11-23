package au.edu.jcu.zhangfan.primoria.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FiberNew
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import au.edu.jcu.zhangfan.primoria.domain.model.ContentSource
import au.edu.jcu.zhangfan.primoria.domain.model.MasteryLevel
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme

/** Small tonal pill with an icon; content source and mastery share this look. */
@Composable
fun TonalBadge(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(color = containerColor, contentColor = contentColor, shape = RoundedCornerShape(10.dp), modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** Commitment 03: AI content is always labelled as such. */
@Composable
fun SourceBadge(source: ContentSource, modifier: Modifier = Modifier) {
    when (source) {
        ContentSource.AI_GENERATED -> TonalBadge(
            text = "AI-generated",
            icon = Icons.Outlined.AutoAwesome,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = modifier,
        )
        ContentSource.OFFLINE_LIBRARY -> TonalBadge(
            text = "Offline library",
            icon = Icons.Outlined.OfflinePin,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
    }
}

/** Mastery always uses the cool teal role, with a distinct icon per level. */
@Composable
fun MasteryBadge(level: MasteryLevel, modifier: Modifier = Modifier) {
    val icon = when (level) {
        MasteryLevel.NEW -> Icons.Outlined.FiberNew
        MasteryLevel.DEVELOPING -> Icons.Outlined.TrendingUp
        MasteryLevel.SECURE -> Icons.Outlined.CheckCircle
        MasteryLevel.MASTERED -> Icons.Outlined.Verified
    }
    val colors = PrimoriaTheme.colors
    TonalBadge(
        text = level.label,
        icon = icon,
        containerColor = if (level == MasteryLevel.NEW) MaterialTheme.colorScheme.surfaceContainerHigh else colors.masteryContainer,
        contentColor = if (level == MasteryLevel.NEW) MaterialTheme.colorScheme.onSurfaceVariant else colors.onMasteryContainer,
        modifier = modifier,
    )
}
