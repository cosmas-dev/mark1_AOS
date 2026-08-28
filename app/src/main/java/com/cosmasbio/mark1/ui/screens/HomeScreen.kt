package com.cosmasbio.mark1.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.cosmasbio.mark1.R
import com.cosmasbio.mark1.model.DeviceStatus
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.scale

private val HomeTop = Color(0xFFE8F0F5)
private val HomeBottom = Color(0xFFC5D1D9)
private val MutedText = Color(0xFF858A8E)
private val SelectedNav = Color(0xFFB2C1CB)

@Composable
fun HomeScreen(
    deviceStatus: DeviceStatus,
    onReconnect: () -> Unit,
    onCapture: () -> Unit,
    onToggleLight: () -> Unit,
    onReadTemperature: () -> Unit,
    onApplySetting: (String, String, String) -> Unit = { _, _, _ -> },
    onStartDiagnosis: () -> Unit,
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedSetting by remember { mutableStateOf("FOCUS") }
    var settingValue by remember { mutableStateOf("") }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(HomeTop, HomeBottom)
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            TopActions(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                onMenuClick = { showSettingsDialog = true },
                onNotificationClick = onReconnect,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(137.dp))

                Text(
                    text = "Hello,\nMr Cosmas",
                    color = Color.Black,
                    fontSize = 34.sp,
                    lineHeight = 41.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(30.dp))

                Box(
                    modifier = Modifier .size(310.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    WhiteRingsHalo(modifier = Modifier.fillMaxSize())

                    StartButton(
                        onClick = onStartDiagnosis,
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = "Last updated\n23 min ago",
                    color = MutedText,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal,
                )

                Spacer(Modifier.height(36.dp))
                BottomNavigation(
                    onHomeClick = {},
                    onChecklistClick = onCapture,
                    onDocumentClick = onReadTemperature,
                    onSettingsClick = { showSettingsDialog = true },
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showSettingsDialog) {
        SettingValueDialog(
            selectedSetting = selectedSetting,
            value = settingValue,
            onDismiss = { showSettingsDialog = false },
            onSettingChange = { selectedSetting = it },
            onValueChange = { settingValue = it },
            onConfirm = {
                onApplySetting("A001", selectedSetting, settingValue)
                showSettingsDialog = false
            },
        )
    }
}

@Composable
private fun StartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.55f,
            stiffness = 500f,
        ),
        label = "start_button_scale",
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 600f,
        ),
        label = "start_button_elevation",
    )

    Box(
        modifier = modifier
            .size(190.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                clip = false,
                ambientColor = Color.Black.copy(
                    alpha = if (isPressed) 0.12f else 0.22f
                ),
                spotColor = Color.Black.copy(
                    alpha = if (isPressed) 0.12f else 0.22f
                ),
            )
            .clip(CircleShape)
            .background(
                color = if (isPressed) {
                    Color(0xFF151515)
                } else {
                    Color.Black
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "START",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TopActions(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassCircleIconButton(
            onClick = onMenuClick,
            icon = R.drawable.ic_menu,
            contentDescription = "메뉴"
        )
        GlassCircleIconButton(
            onClick = onNotificationClick,
            icon = R.drawable.ic_notification,
            contentDescription = "알림"
        )
    }
}

@Composable
private fun GlassCircleIconButton(
    onClick: () -> Unit,
    icon: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape

    Box(
        modifier = modifier
            .size(40.dp)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.14f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color(0xFFE8F0F5),
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.85f),
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
        )
    }
}

// 좌에서 우로 퍼져나가는 애니메이션
//@Composable
//private fun DotHalo(
//    modifier: Modifier = Modifier,
//) {
//    val infiniteTransition = rememberInfiniteTransition(label = "dot_halo_animation")
//
//    val waveProgress by infiniteTransition.animateFloat(
//        initialValue = -1f,
//        targetValue = 1f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(
//                durationMillis = 2200,
//                easing = LinearEasing,
//            )
//        ),
//        label = "dot_wave_progress",
//    )
//
//    Canvas(modifier = modifier) {
//        val center = Offset(
//            x = size.width / 2f,
//            y = size.height / 2f,
//        )
//
//        // dot이 촘촘할 수록 숫자 낮아짐
//        val spacing = 18.dp.toPx()
//
//        // 점이 퍼지는 전체 범위
//        val maxDistance = size.minDimension * 0.50f
//
//        // 중앙 START 버튼에 가려지는 영역
//        val innerHiddenRadius = 88.dp.toPx()
//
//        val columns = (size.width / spacing).toInt() + 2
//        val rows = (size.height / spacing).toInt() + 2
//
//        for (row in -rows..rows) {
//            for (column in -columns..columns) {
//                val x = center.x + column * spacing
//                val y = center.y + row * spacing
//
//                val dx = x - center.x
//                val dy = y - center.y
//
//                val distance = sqrt(dx * dx + dy * dy)
//
//                if (distance in innerHiddenRadius..maxDistance) {
//                    val normalizedDistance =
//                        ((distance - innerHiddenRadius) /
//                                (maxDistance - innerHiddenRadius))
//                            .coerceIn(0f, 1f)
//
//                    // 중앙에서 바깥으로 갈수록 점이 작아짐
//                    val baseRadius =
//                        7.2.dp.toPx() * (1f - normalizedDistance * 0.82f)
//
//                    // 가로 방향으로 지나가는 빛의 띠
//                    val normalizedX =
//                        ((x - center.x) / maxDistance)
//                            .coerceIn(-1f, 1f)
//
//                    val waveDistance = abs(normalizedX - waveProgress)
//
//                    val waveStrength =
//                        (1f - waveDistance / 0.42f)
//                            .coerceIn(0f, 1f)
//
//                    // 옆으로 퍼지는 듯한 미세한 파동
//                    val ripple =
//                        0.5f +
//                                0.5f * sin(
//                            normalizedDistance * 18f -
//                                    waveProgress * 6f
//                        )
//
//                    val radius =
//                        (
//                                baseRadius *
//                                        (0.92f + waveStrength * 0.30f) *
//                                        (0.95f + ripple * 0.08f)
//                                ).coerceAtLeast(1.dp.toPx())
//
//                    val edgeFade =
//                        (1f - normalizedDistance)
//                            .coerceIn(0f, 1f)
//
//                    val alpha =
//                        (
//                                0.12f +
//                                        edgeFade * 0.46f +
//                                        waveStrength * 0.32f
//                                ).coerceIn(0.06f, 0.82f)
//
//                    drawCircle(
//                        color = DotColor.copy(alpha = alpha),
//                        radius = radius,
//                        center = Offset(x, y),
//                    )
//                }
//            }
//        }
//    }
//}

// white_rings_preview.html의 동심원 점 애니메이션을 Compose Canvas로 구현
@Composable
private fun WhiteRingsHalo(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "white_rings_animation"
    )

    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3_000,
                easing = LinearEasing,
            )
        ),
        label = "white_rings_pulse",
    )

    // HTML과 동일하게 각 링은 7, 10, 13 ... 34초의 서로 다른 속도로 회전한다.
    val ringRotations = List(10) { ringIndex ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (ringIndex * 3 + 7) * 1_000,
                    easing = LinearEasing,
                )
            ),
            label = "white_ring_rotation_$ringIndex",
        )
    }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val densityScale = size.minDimension / 400f

        repeat(10) { ringIndex ->
            val pointCount = 18 + ringIndex * 6
            val ringRadius = (40f + ringIndex * 12f) * densityScale
            val delayedPhase = (
                pulseProgress - ringIndex * .10f + 1f
            ) % 1f

            // CSS pulse: 0 → 1 → 0 scale/opacity.
            val pulse = if (delayedPhase < .5f) {
                delayedPhase * 2f
            } else {
                (1f - delayedPhase) * 2f
            }
            val dotRadius = 4f * densityScale * pulse
            val alpha = pulse.coerceIn(0f, 1f)
            val rotation = ringRotations[ringIndex].value

            repeat(pointCount) { pointIndex ->
                val degrees = rotation + pointIndex * (360f / pointCount)
                val radians = Math.toRadians(degrees.toDouble())
                val point = Offset(
                    x = center.x + kotlin.math.sin(radians).toFloat() * ringRadius,
                    y = center.y - kotlin.math.cos(radians).toFloat() * ringRadius,
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = dotRadius,
                    center = point,
                )
            }
        }
    }
}

@Composable
private fun BottomNavigation(
    onHomeClick: () -> Unit,
    onChecklistClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
             .height(72.dp)
            .shadow(10.dp, RoundedCornerShape(54.dp))
            .clip(RoundedCornerShape(54.dp))
            .background(Color.White.copy(alpha = 0.39f))
             .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem(Icons.Outlined.Home, "홈", selected = true, onClick = onHomeClick)
        BottomNavItem(Icons.Outlined.Check, "목록", onClick = onChecklistClick)
        BottomNavItem(Icons.Outlined.DateRange, "문서", onClick = onDocumentClick)
        BottomNavItem(Icons.Outlined.Settings, "설정", onClick = onSettingsClick)
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
             .width(76.dp)
             .height(60.dp)
            .clip(RoundedCornerShape(44.dp))
            .background(if (selected) SelectedNav else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.Black,
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun SettingValueDialog(
    selectedSetting: String,
    value: String,
    onDismiss: () -> Unit,
    onSettingChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
) {
    val options = listOf("FOCUS", "EXPOSURE", "D_GAIN", "A_GAIN", "BL_DUTY", "ISO", "SHUTTER")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("세팅값 설정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    TextButton(
                        onClick = { onSettingChange(option) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (selectedSetting == option) "✓ $option" else option)
                    }
                }
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("$selectedSetting 값") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("확인") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
