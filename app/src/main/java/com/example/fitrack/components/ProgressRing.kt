package com.example.fitrack.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fitrack.ui.theme.MintFit

@Composable
fun ProgressRing(
    value: Double,
    goal: Double,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp,
    strokeWidth: Dp = 14.dp,
    color: Color = MintFit,
    content: @Composable ColumnScope.() -> Unit
) {
    val target = if (goal > 0) (value / goal).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "progress_ring"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val radius = (this.size.width - strokePx) / 2f
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(radius * 2, radius * 2)

            drawArc(
                color = Color.White.copy(alpha = 0.06f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
