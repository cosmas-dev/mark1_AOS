package com.cosmasbio.mark1.ui.screens

import android.graphics.BitmapFactory
import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.VerifiedUser
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.cosmasbio.mark1.model.CaptureResult

private val ReportTop = Color(0xFFE8F0F5)
private val ReportBottom = Color(0xFFC5D1D9)

@Composable
fun ReportResultScreen(
    profileName: String,
    captureResult: CaptureResult?,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onSaveAndAct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var detailsSelected by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ReportTop, ReportBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            ReportHeader(onBack)
            // Text("Jan 23, 2025, 4:55 PM", color = Color(0xFF7D8589), fontSize = 12.sp)
            Text("2026월 9월 3일", color = Color(0xFF7D8589), fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
//            Text("COSMAS Report", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("고스마님의 보고서", color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            ReportTabs(detailsSelected) { detailsSelected = it }
            Spacer(Modifier.height(20.dp))
            if (detailsSelected) ReportDetails(profileName) else ReportSummary(captureResult, onSaveAndAct)
        }
        ReportBottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            onBack = onBack,
            onRestart = onRestart,
        )
    }
}

@Composable
private fun ReportHeader(onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(74.dp)) {
        CircleAction(Modifier.align(Alignment.CenterStart), onBack) {
            Icon(Icons.Rounded.ChevronLeft, "뒤로", modifier = Modifier.size(26.dp))
        }
        // Text("Result", Modifier.align(Alignment.Center), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("검사 결과", Modifier.align(Alignment.Center), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        CircleAction(Modifier.align(Alignment.CenterEnd), {}) {
            Icon(Icons.Rounded.IosShare, "내보내기", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ReportTabs(details: Boolean, onSelect: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(44.dp).clip(CircleShape).background(Color.White.copy(.52f)).padding(3.dp),
    ) {
//        listOf(false to "Summary", true to "Details").forEach { (value, title) ->
        listOf(false to "요약", true to "상세정보").forEach { (value, title) ->
            Box(
                Modifier.weight(1f).fillMaxSize().clip(CircleShape)
                    .background(if (details == value) Color.White else Color.Transparent)
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center,
            ) {
                Text(title, color = if (details == value) Color.Black else Color(0xFFB4BEC4), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReportSummary(captureResult: CaptureResult?, onSaveAndAct: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 92.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ResultCard("M", "arijuana", "0.3", "ng/mL", true)
        ResultCard("마리화나", "", "0.3", "ng/mL", true)
        // ResultCard("F", "entanyl", "10", "ng/mL", false)
        ResultCard("펜타닐", "", "21.5", "ng/mL", false)
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(Color(0xFFE64E4E), Color(0xFFFF747A))))
                .clickable(onClick = onSaveAndAct),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.VerifiedUser,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                "결과 저장 및 조치",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // CaptureDebugSection(captureResult)
    }
}

/** 값 확인용 임시 섹션. 촬영 이미지와 분석 원본값을 그대로 나열한다. */
@Composable
private fun CaptureDebugSection(captureResult: CaptureResult?) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("촬영 이미지 / 분석 원본값", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)

        if (captureResult == null) {
            Text("아직 촬영/분석 결과가 없습니다.", color = Color(0xFF7D8589), fontSize = 12.sp)
            return@Column
        }

        val bitmap = remember(captureResult.imagePath) {
            runCatching { BitmapFactory.decodeFile(captureResult.imagePath) }.getOrNull()
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "촬영 이미지",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
        } else {
            Text("이미지를 불러올 수 없습니다.", color = Color(0xFFE64E4E), fontSize = 12.sp)
        }

        DebugValue("imagePath", captureResult.imagePath)
        DebugValue("name", captureResult.name)
        DebugValue("type", captureResult.type)
        DebugValue("info", captureResult.info)

        val analysis = captureResult.analysis
        if (analysis == null) {
            Text("분석 결과 없음", color = Color(0xFFE64E4E), fontSize = 12.sp)
            return@Column
        }

        Spacer(Modifier.height(4.dp))
        DebugValue("tDetected", analysis.tDetected.toString())
        DebugValue("tWeak", analysis.tWeak.toString())
        DebugValue("cPosition", analysis.cPosition?.toString() ?: "null")
        DebugValue("cSnr", analysis.cSnr?.toString() ?: "null")
        DebugValue("tPosition", analysis.tPosition?.toString() ?: "null")
        DebugValue("tSnr", analysis.tSnr?.toString() ?: "null")
        DebugValue("noiseSigma", analysis.noiseSigma.toString())
        DebugValue("numPeaks", analysis.numPeaks.toString())
        DebugValue("peakSeparationPx", analysis.peakSeparationPx.toString())
        DebugValue("h1SplitValid", analysis.h1SplitValid.toString())
        DebugValue("channelName", analysis.channelName)
        DebugValue("image", "${analysis.imageWidth} x ${analysis.imageHeight}")
        DebugValue("roi", "x=${analysis.roiX} y=${analysis.roiY} w=${analysis.roiW} h=${analysis.roiH}")
        DebugValue("analysis.imagePath", analysis.imagePath)

        Spacer(Modifier.height(4.dp))
        Text("rawJson", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(analysis.rawJson, color = Color(0xFF25282A), fontSize = 10.sp)
    }
}

@Composable
private fun DebugValue(label: String, value: String) {
    Text("$label: $value", color = Color(0xFF25282A), fontSize = 12.sp)
}

@Composable
private fun ResultCard(initial: String, name: String, value: String, unit: String, negative: Boolean) {
    val fontScale = LocalDensity.current.fontScale
    val initialSize = (30f / fontScale).sp
    val nameSize = (18f / fontScale).sp
    val valueSize = (36f / fontScale).sp
    val unitSize = (9f / fontScale).sp
    val captionSize = (8f / fontScale).sp
    val badgeSize = (15f / fontScale).sp

    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = .66f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = .88f),
                shape = RoundedCornerShape(20.dp),
            ),
    ) {
        Row(
            Modifier.fillMaxSize().padding(start = 18.dp, end = 10.dp, top = 18.dp, bottom = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.width(105.dp).fillMaxSize()) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        initial,
                        modifier = Modifier.alignByBaseline(),
                        fontSize = initialSize,
                        lineHeight = (38f / fontScale).sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        name,
                        modifier = Modifier.alignByBaseline(),
                        fontSize = nameSize,
                        lineHeight = (20f / fontScale).sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        value,
                        modifier = Modifier.alignByBaseline(),
                        fontSize = valueSize,
                        lineHeight = (38f / fontScale).sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        unit,
                        modifier = Modifier.alignByBaseline(),
                        color = Color(0xFF737B80),
                        fontSize = unitSize,
                    )
                }
                Text(
                    "Cut-off Value ${if (negative) 2 else 1}",
                    color = Color(0xFF9CA4A8),
                    fontSize = captionSize,
                )
            }
            Spacer(Modifier.weight(1f))
            ResultDotPlot(negative)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 13.dp, end = 14.dp)
                .size(25.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(if (negative) Color(0xFFDFFFF0) else Color(0xFFFFDDDF)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (negative) "N" else "P",
                color = if (negative) Color(0xFF2ECF64) else Color(0xFFFF4E59),
                fontSize = badgeSize,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ResultDotPlot(negative: Boolean) {
    val reveal = remember(negative) { Animatable(0f) }
    LaunchedEffect(negative) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, animationSpec = tween(2800, easing = LinearEasing))
    }

    Canvas(Modifier.size(158.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension * .44f
        repeat(4) { i ->
            drawCircle(
                Color(0xFF9AA5AA).copy(.28f),
                maxRadius * (i + 1) / 4f,
                center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(1f),
            )
        }

        val labelPaint = Paint().apply {
            color = android.graphics.Color.argb(90, 90, 100, 105)
            textSize = 7.dp.toPx()
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        listOf("100", "10", "1", "0").forEachIndexed { index, label ->
            drawContext.canvas.nativeCanvas.drawText(
                label,
                center.x,
                center.y - maxRadius * (4 - index) / 4f + 8.dp.toPx(),
                labelPaint,
            )
        }

        // Wavefront sweeps outward from the center as `reveal` goes 0 -> 1; each dot
        // fades/grows in once the front passes its own distance, so the cluster looks
        // like it's spreading outward rather than popping in all at once.
        val frontHalfWidth = .16f
        val front = reveal.value * (1f + 2f * frontHalfWidth)

        if (negative) {
            for (row in -7..7) for (column in -7..7) {
                val distance = kotlin.math.sqrt((row * row + column * column).toFloat())
                if (distance <= 7.4f) {
                    val normDistance = distance / 7.4f
                    val dotReveal = ((front - normDistance) / (2f * frontHalfWidth)).coerceIn(0f, 1f)
                    if (dotReveal <= 0f) continue
                    val alpha = (1f - distance / 9f).coerceIn(.18f, .9f) * dotReveal
                    val radius = 1.55.dp.toPx() * (.35f + .65f * dotReveal)
                    drawCircle(
                        Color(0xFF34D066).copy(alpha),
                        radius,
                        Offset(center.x + column * 2.15.dp.toPx(), center.y + row * 2.15.dp.toPx()),
                    )
                }
            }
        } else {
            for (row in -18..18) for (column in -18..18) {
                val distance = kotlin.math.sqrt((row * row + column * column).toFloat())
                if (distance <= 18.3f) {
                    val normDistance = distance / 18.3f
                    val dotReveal = ((front - normDistance) / (2f * frontHalfWidth)).coerceIn(0f, 1f)
                    if (dotReveal <= 0f) continue
                    val alpha = (.42f + (1f - distance / 18.3f) * .48f).coerceIn(0f, 1f) * dotReveal
                    val radius = 1.25.dp.toPx() * (.35f + .65f * dotReveal)
                    drawCircle(
                        Color(0xFFFF5D68).copy(alpha),
                        radius,
                        Offset(center.x + column * 2.05.dp.toPx(), center.y + row * 2.05.dp.toPx()),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportDetails(profileName: String) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 92.dp).verticalScroll(rememberScrollState())
            .clip(RoundedCornerShape(20.dp)).background(Color.White.copy(.63f)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // DetailSection("Diagnostic Products")
        // DetailRow("Device", "COSMAS001  ›")
        // DetailRow("Kit", "COSMAS001 KIT  ›")
        // DetailRow("Location", "8502 Preston Rd. Inglewood, Maine 98380")
        // Spacer(Modifier.height(8.dp)); DetailSection("Profile")
        // DetailRow("Name", profileName.ifBlank { "kim" })
        // DetailRow("Age", "31")
        // DetailRow("Email", "cosmas@cosmas.com")
        // DetailRow("Phone", "+82-10-1234-5678")
        // DetailRow("Address", "8502 Preston Rd. Inglewood, Maine 98380")
        // Spacer(Modifier.height(8.dp)); DetailSection("Company")
        // DetailRow("Company Name", "cosmas")
        // DetailRow("Car Type", "Truck")
        // DetailRow("Plate Number", "ABC-1234")
        DetailSection("진단 제품 정보")
        DetailRow("모델명", "COSMAS001  ›")
        DetailRow("키트", "COSMAS001 KIT  ›")
        DetailRow("위치", "서울 관악구 봉천로")
        Spacer(Modifier.height(8.dp)); DetailSection("대상자 정보")
        DetailRow("이름", profileName.ifBlank { "오류" })
        DetailRow("나이", "31")
        DetailRow("이메일", "cosmas@cosmas.com")
        DetailRow("연락처", "+82-10-1234-5678")
        DetailRow("주소", "서울 관악구 봉천로")
        Spacer(Modifier.height(8.dp)); DetailSection("소속 정보")
        DetailRow("회사명", "고스마")
        DetailRow("차량 종류", "Truck")
        DetailRow("차량 번호", "123가 4567")
    }
}

@Composable private fun DetailSection(text: String) = Text(text, color = Color(0xFF4B5155), fontWeight = FontWeight.Bold)
@Composable private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(.38f), color = Color(0xFF7C858A))
        Text(value, Modifier.weight(.62f), textAlign = TextAlign.End, color = Color(0xFF25282A))
    }
}

@Composable
private fun ReportBottomActions(modifier: Modifier, onBack: () -> Unit, onRestart: () -> Unit) {
    Row(modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(.28f).height(58.dp).shadow(7.dp, CircleShape).clip(CircleShape).background(Color.White).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
            // Text("Cancel", fontWeight = FontWeight.Bold)
            Text("취소", fontWeight = FontWeight.Bold)
        }
        Box(Modifier.weight(.72f).height(58.dp).clip(CircleShape).background(Color.Black).clickable(onClick = onRestart), contentAlignment = Alignment.Center) {
            // Text("Restart", color = Color.White, fontWeight = FontWeight.Bold)
            Text("재검사", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun CircleAction(modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(modifier.size(38.dp).shadow(6.dp, CircleShape).clip(CircleShape).background(Color.White.copy(.7f)).border(1.dp, Color.White, CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) { content() }
}
