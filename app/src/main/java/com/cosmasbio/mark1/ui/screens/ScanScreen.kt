package com.cosmasbio.mark1.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R
import kotlinx.coroutines.delay

private val ScanTop = Color(0xFFE8F0F5)
private val ScanBottom = Color(0xFFC5D1D9)
private val ScanInk = Color(0xFF202326)
private val ScanBlue = Color(0xFF91A3C3)

@Composable
fun ScanScreen(
    onClose: () -> Unit,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scanComplete by remember { mutableStateOf(false) }
    var interactionBoost by remember { mutableStateOf(false) }

    // UI preview timing. Replace this with the real device completion state later.
    LaunchedEffect(Unit) {
        delay(7_000)
        scanComplete = true
    }
    LaunchedEffect(interactionBoost) {
        if (interactionBoost) {
            delay(1_200)
            interactionBoost = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ScanTop, ScanBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScanHeader(onClose = onClose)
            Spacer(Modifier.height(40.dp))

            Text(
                text = if (scanComplete) {
                    // "Scan complete Your\nanalysis is ready to begin"
                    "스캔 완료\n분석할 준비가 되었습니다."
                } else {
                    // "Please wait\nwhile we scan your test"
                    "키트를 스캔하고 있습니다."
                },
                color = ScanInk,
                fontSize = 24.sp,
                lineHeight = 35.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (scanComplete) "" else "최대 1분 정도 소요될 수 있습니다.", // "This may take up to 1 minutes",
                color = ScanInk,
                fontSize = 18.sp,
            )

            Spacer(Modifier.weight(1f))
            AnimatedKit(
                scanComplete = scanComplete,
                interactionBoost = interactionBoost,
                onTap = {
                    if (!scanComplete) interactionBoost = true
                },
            )
            Spacer(Modifier.height(30.dp))

            Text(
                text = if (scanComplete) "스캔 완료" else "스캔 중...",
                color = Color(0xFF7D8589),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))

            ScanActions(
                enabled = scanComplete,
                onCancel = onCancel,
                onContinue = onContinue,
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AnimatedKit(
    scanComplete: Boolean,
    interactionBoost: Boolean,
    onTap: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "kit_scan")
    val lightSweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (interactionBoost) 3_100 else 3_100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "kit_light_sweep",
    )
    val pulse by transition.animateFloat(
        initialValue = .25f,
        targetValue = .68f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_300),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scan_pulse",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
//        Canvas(Modifier.fillMaxSize()) {
//            val center = Offset(size.width / 2f, size.height / 2f)
//            val ringColor = ScanBlue.copy(alpha = if (scanComplete) .25f else .16f + pulse * .16f)
//
//            listOf(88f, 132f, 176f).forEach { radius ->
//                drawOval(
//                    color = ringColor,
//                    topLeft = Offset(center.x - radius, center.y - radius * .35f),
//                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * .70f),
//                    style = Stroke(width = 3f, cap = StrokeCap.Round),
//                )
//            }
//
//        }

        Image(
            painter = painterResource(R.drawable.kit),
            contentDescription = "Diagnostic test kit",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(325.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    if (!scanComplete) {
                        val bandHeight = 150.dp.toPx()
                        val offsetY = -bandHeight + lightSweep * (size.height + bandHeight)
                        translate(top = offsetY) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.5f to Color.White.copy(alpha = .9f),
                                    1f to Color.Transparent,
                                    startY = 0f,
                                    endY = bandHeight,
                                ),
                                size = androidx.compose.ui.geometry.Size(size.width, bandHeight),
                                blendMode = BlendMode.SrcAtop,
                            )
                        }
                    }
                },
        )
    }
}

@Composable
private fun ScanHeader(onClose: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(72.dp)) {
        Text(
            // text = "Diagnose",
            text = "진단",
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(48.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = .72f))
                .border(1.dp, Color.White.copy(alpha = .88f), CircleShape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Close, "Close", Modifier.size(28.dp), Color.Black)
        }
    }
}

@Composable
private fun ScanActions(
    enabled: Boolean,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
) {
    val continueColor by animateColorAsState(
        targetValue = if (enabled) Color.Black else Color(0xFFA5A5A5),
        label = "continue_color",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .height(64.dp)
                .weight(.34f)
                .shadow(8.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = .82f))
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Text("취소", color = ScanInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .height(64.dp)
                .weight(.66f)
                .clip(RoundedCornerShape(32.dp))
                .background(continueColor)
                .clickable(enabled = enabled, onClick = onContinue),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                // "Continue",
                "분석 시작",
                color = if (enabled) Color.White else Color.White.copy(alpha = .38f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
