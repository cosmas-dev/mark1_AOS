package com.cosmasbio.mark1.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val AnalysisTop = Color(0xFFE8F0F5)
private val AnalysisBottom = Color(0xFFC5D1D9)
private val AnalysisInk = Color(0xFF202326)
private val AnalysisBlue = Color(0xFF647B8A)

@Composable
fun AnalysisProgressScreen(
    onClose: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = remember { Animatable(0f) }
    val latestOnComplete by rememberUpdatedState(onComplete)

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 6_500, easing = LinearEasing),
        )
        delay(450)
        latestOnComplete()
    }

    val percent = (progress.value * 100f).roundToInt().coerceIn(0, 100)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AnalysisTop, AnalysisBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnalysisHeader(onClose)
            Spacer(Modifier.height(54.dp))
            Text(
                text = if (percent == 100) {
                    "Analysis complete\nYour results are ready"
                } else {
                    "Analyzing your test\nPlease wait"
                },
                color = AnalysisInk,
                fontSize = 25.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "This may take up to 15 minutes",
                color = AnalysisInk,
                fontSize = 16.sp,
            )

            Spacer(Modifier.weight(1f))
            CircularAnalysisProgress(progress.value, percent)
            Spacer(Modifier.weight(1f))

            Text(
                text = if (percent == 100) "Analysis complete" else "Analyzing…",
                color = Color(0xFF81898D),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(62.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = .92f))
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center,
            ) {
                Text("Cancel", color = AnalysisInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun CircularAnalysisProgress(progress: Float, percent: Int) {
    Box(modifier = Modifier.size(310.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension * .49f
            val innerRadius = size.minDimension * .31f
            val spacing = 12.dp.toPx()
            var y = -maxRadius
            while (y <= maxRadius) {
                var x = -maxRadius
                while (x <= maxRadius) {
                    val distance = kotlin.math.sqrt(x * x + y * y)
                    if (distance in innerRadius..maxRadius) {
                        val fade = (1f - (distance - innerRadius) / (maxRadius - innerRadius))
                        val wave = .55f + .45f * sin(distance / 18f - progress * 12f)
                        drawCircle(
                            color = AnalysisBlue.copy(alpha = (.13f + fade * .25f) * wave),
                            radius = 2.2.dp.toPx(),
                            center = Offset(center.x + x, center.y + y),
                        )
                    }
                    x += spacing
                }
                y += spacing
            }

            val stroke = 11.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = .82f),
                radius = innerRadius,
                style = Stroke(stroke),
            )
            drawArc(
                color = AnalysisBlue,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                size = androidx.compose.ui.geometry.Size(innerRadius * 2f, innerRadius * 2f),
                style = Stroke(stroke, cap = StrokeCap.Butt),
            )
        }
        Text(
            text = "$percent%",
            color = AnalysisBlue,
            fontSize = 43.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AnalysisHeader(onClose: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(72.dp)) {
        Text(
            "Diagnose",
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(46.dp)
                .shadow(7.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = .76f))
                .border(1.dp, Color.White.copy(alpha = .9f), CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Close, "Close", tint = Color.Black, modifier = Modifier.size(27.dp))
        }
    }
}
