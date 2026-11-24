package au.edu.jcu.zhangfan.primoria.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme

/** Keeps reading width comfortable on tablets and in landscape. */
@Composable
fun ContentColumn(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 680.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = maxWidth).fillMaxWidth(), content = content)
    }
}

/** Diagonal brand wash used behind heroes and the quiz. */
fun Modifier.gradientBackground(colors: List<Color>, shape: Shape? = null): Modifier {
    val brush = Brush.linearGradient(colors, start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY))
    return if (shape != null) background(brush, shape) else background(brush)
}

/** A gentle 0.97 press compression with a soft spring, shared by every tappable card. */
@Composable
fun rememberPressScale(interactionSource: MutableInteractionSource): Float {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "pressScale",
    )
    return scale
}

/**
 * The standard Primoria card: white on paper with a single hairline edge and no shadow, so
 * hierarchy comes from spacing and type rather than stacked borders and elevation.
 */
@Composable
fun PrimoriaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(18.dp),
    containerColor: Color = PrimoriaTheme.colors.cardSurface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: Boolean = true,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PrimoriaTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val scale = if (onClick != null) rememberPressScale(interaction) else 1f
    val inner: @Composable () -> Unit = { Column(Modifier.padding(contentPadding), content = content) }
    val outer = modifier.graphicsLayer { scaleX = scale; scaleY = scale }
    val stroke = if (border) BorderStroke(1.dp, colors.hairline) else null
    if (onClick != null) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            border = stroke,
            interactionSource = interaction,
            modifier = outer,
        ) { inner() }
    } else {
        Surface(shape = shape, color = containerColor, contentColor = contentColor, border = stroke, modifier = outer) { inner() }
    }
}

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    Box(modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterStart).semantics { heading() },
        )
        if (trailing != null) Box(Modifier.align(Alignment.CenterEnd)) { trailing() }
    }
}
