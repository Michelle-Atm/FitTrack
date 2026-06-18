package com.example.fitrack.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.VioletFit
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun CelebrationOverlay(
    visible: Boolean,
    message: String,
    onDismiss: () -> Unit
) {
    val particleProgress = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            particleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
            )
            delay(1500)
            onDismiss()
        } else {
            particleProgress.snapTo(0f)
        }
    }

    val particles = remember {
        List(30) {
            Triple(
                Random.nextFloat() * 360f,
                Random.nextFloat() * 200f + 80f,
                listOf(AmberFit, MintFit, VioletFit, Color.White)[Random.nextInt(4)]
            )
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300)),
        exit = fadeOut(tween(400)) + scaleOut(tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .drawBehind {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val progress = particleProgress.value
                        particles.forEach { (angleDeg, dist, color) ->
                            val angle = Math.toRadians(angleDeg.toDouble())
                            val x = cx + cos(angle).toFloat() * dist * progress * 3f
                            val y = cy + sin(angle).toFloat() * dist * progress * 3f
                            val alpha = (1f - progress).coerceIn(0f, 1f)
                            drawCircle(
                                color = color.copy(alpha = alpha),
                                radius = 6f,
                                center = Offset(x, y)
                            )
                        }
                    }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardBG)
                        .padding(horizontal = 40.dp, vertical = 32.dp)
                ) {
                    Text(text = "🏆", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Appuie pour continuer",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
