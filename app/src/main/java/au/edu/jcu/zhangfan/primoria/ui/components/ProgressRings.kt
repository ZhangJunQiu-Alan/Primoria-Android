package au.edu.jcu.zhangfan.primoria.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val RingEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** Animates from the previous value rather than restarting at zero on every update. */
@Composable
fun animatedProgress(target: Float): Float {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animatable.animateTo(target.coerceIn(0f, 1f), tween(durationMillis = 650, easing = RingEasing))
    }
    return animatable.value
}

private fun DrawScope.ring(progress: Float, colors: List<Color>, track: Color, stroke: Float, inset: Float) {
    val diameter = size.minDimension - stroke - inset * 2
    val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
    val arcSize = Size(diameter, diameter)
    drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
    if (progress <= 0f) return
    rotate(-90f) {
        drawArc(
            brush = Brush.sweepGradient(colors + colors.first()),
            startAngle = 0f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
    }
}

/** A single gradient ring with optional centre content. */
@Composable
fun ProgressRing(
    progress: Float,
    colors: List<Color>,
    trackColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated = animatedProgress(progress)
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) { ring(animated, colors, trackColor, strokeWidth.toPx(), 0f) }
        content()
    }
}

/** Two concentric rings: mastery outside, effort inside — the Progress screen's signature. */
@Composable
fun DualRings(
    outerProgress: Float,
    outerColors: List<Color>,
    innerProgress: Float,
    innerColors: List<Color>,
    trackColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 132.dp,
    strokeWidth: Dp = 14.dp,
) {
    val outer = animatedProgress(outerProgress)
    val inner = animatedProgress(innerProgress)
    Canvas(modifier.size(size)) {
        val stroke = strokeWidth.toPx()
        ring(outer, outerColors, trackColor, stroke, 0f)
        ring(inner, innerColors, trackColor, stroke, stroke + 4.dp.toPx())
    }
}
