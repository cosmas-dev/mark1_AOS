package com.cosmasbio.mark1.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import com.cosmasbio.mark1.model.CaptureUiState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.math.sin

private val AnalysisTop = Color(0xFFE8F0F5)
private val AnalysisBottom = Color(0xFFC5D1D9)
private val AnalysisInk = Color(0xFF202326)
private val AnalysisBlue = Color(0xFF647B8A)

/** 리더기 촬영/분석 진행 단계. 값이 클수록 뒤쪽 단계이며 되돌아가지 않는다. */
private const val STAGE_PREPARING = 0
private const val STAGE_CAPTURING = 1
private const val STAGE_ANALYZING = 2
private const val STAGE_DONE = 3

@Composable
fun AnalysisProgressScreen(
    captureUiState: CaptureUiState,
    analysisDone: Boolean,
    onStartCapture: () -> Unit,
    onDismissError: () -> Unit,
    onClose: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestOnComplete by rememberUpdatedState(onComplete)
    val latestOnStartCapture by rememberUpdatedState(onStartCapture)

    // 재시도할 때마다 진행 상태를 처음부터 다시 시작한다.
    var attempt by remember { mutableIntStateOf(0) }
    val progress = remember(attempt) { Animatable(0f) }

    LaunchedEffect(attempt) { latestOnStartCapture() }

    val failed = captureUiState.error != null

    // ViewModel은 분석이 끝나면 상태를 idle로 되돌리므로, 도달한 최대 단계를 따로 기억해
    // 진행률이 뒤로 밀리지 않게 한다.
    val rawStage = when {
        analysisDone -> STAGE_DONE
        captureUiState.analyzing -> STAGE_ANALYZING
        captureUiState.capturing && captureUiState.remainingSeconds == 0 -> STAGE_CAPTURING
        else -> STAGE_PREPARING
    }
    var stage by remember(attempt) { mutableIntStateOf(STAGE_PREPARING) }
    LaunchedEffect(rawStage, attempt) {
        if (rawStage > stage) stage = rawStage
    }

    // 각 단계마다 목표 진행률까지 천천히 채우고, 다음 단계로 넘어가면 이어서 진행한다.
    LaunchedEffect(stage, failed, attempt) {
        if (failed) return@LaunchedEffect
        when (stage) {
            STAGE_PREPARING -> progress.animateTo(.08f, tween(1_200, easing = LinearEasing))
            STAGE_CAPTURING -> progress.animateTo(.55f, tween(9_000, easing = LinearEasing))
            STAGE_ANALYZING -> progress.animateTo(.92f, tween(12_000, easing = LinearEasing))
            else -> {
                progress.animateTo(1f, tween(500, easing = LinearEasing))
                delay(400)
                latestOnComplete()
            }
        }
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
                text = when {
                    failed -> "분석 실패\n다시 시도해 주세요."
                    stage == STAGE_DONE -> "분석 완료\n검사 결과를 확인해 주세요."
                    else -> "분석 중..."
                },
                color = AnalysisInk,
                fontSize = 25.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = when {
                    failed -> captureUiState.error.orEmpty()
                    captureUiState.remainingSeconds > 0 -> "촬영까지 ${captureUiState.remainingSeconds}초"
                    else -> "최대 10분 정도 소요될 수 있습니다."
                },
                color = AnalysisInk,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))
            CircularAnalysisProgress(progress.value, percent)
            Spacer(Modifier.weight(1f))

            Text(
                text = when {
                    failed -> "분석 실패"
                    stage == STAGE_DONE -> "분석 완료"
                    stage == STAGE_ANALYZING -> "이미지 분석 중..."
                    stage == STAGE_CAPTURING -> "촬영 중..."
                    else -> "촬영 준비 중..."
                },
                color = Color(0xFF81898D),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(62.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (failed) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable {
                                onDismissError()
                                attempt++
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("다시 시도", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = .92f))
                        .clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("취소", color = AnalysisInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
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
            // "Diagnose",
            "진단",
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
