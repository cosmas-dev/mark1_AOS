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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
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
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.res.stringResource
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

private val HomeTop = Color(0xFFE8F0F5)
private val HomeBottom = Color(0xFFC5D1D9)
private val MutedText = Color(0xFF858A8E)

@Composable
fun HomeScreen(
    deviceStatus: DeviceStatus,
    onReconnect: () -> Unit,
    onCapture: () -> Unit,
    onToggleLight: () -> Unit,
    onReadTemperature: () -> Unit,
    onApplySetting: (String, String, String) -> Unit = { _, _, _ -> },
    onStartDiagnosis: () -> Unit,
    onNotificationClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onChecklistClick: () -> Unit,
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedSetting by remember { mutableStateOf("FOCUS") }
    var settingValue by remember { mutableStateOf("") }

    // 디자인(Figma) 기준 화면 크기는 360x800dp. 실제 화면 너비를 그 기준과
    // 비교한 비율만큼 주요 크기를 함께 조절해서, 기기 화면 크기가 달라져도
    // 디자인과 같은 비율로 보이게 한다.
    // (BoxWithConstraints는 내부적으로 SubcomposeLayout을 쓰는데, 이게 상단
    // 메뉴/알림 버튼의 클릭 가능 영역을 화면에 보이는 위치와 어긋나게 만드는
    // 문제가 있어서, 대신 LocalConfiguration으로 화면 너비를 구한다.)
    val scale = LocalConfiguration.current.screenWidthDp.dp / 360.dp

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
            // TopActions(메뉴/알림 버튼)는 스크롤 가능한 Column보다 나중에(Box의 더 위 레이어에)
            // 선언해야 한다. 먼저 선언하면 그 위에 겹쳐 그려지는 스크롤 영역이 같은 자리의
            // 터치를 먼저 가로채서 버튼이 눌리지 않는 문제가 있었다.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 화면이 작아서 아래 내용(마지막 업데이트 + 하단 내비게이션)이
                // 들어갈 공간이 부족해지면, 이 가운데 영역만 스크롤되고
                // 하단 내비게이션은 항상 화면에 보이도록 고정한다.
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Figma 실측값(360x800 기준): 인사말 텍스트 Top 135dp.
                    // 이 여백은 scale을 곱하지 않는다 — halo/버튼 크기만 정확히 비율대로
                    // 키우고, 여백까지 같이 키우면 화면이 좁고 높이가 빠듯한 기기에서
                    // 이 영역이 화면 안에 다 안 들어가 스크롤이 생겨버린다.
                    Spacer(Modifier.height(135.dp))

                    Text(
                        text = stringResource(R.string.home_greeting, "고스마"),
                        color = Color.Black,
                        fontSize = 34.sp,
                        lineHeight = 41.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )

                    // Figma 실측값: 인사말 텍스트 하단(135+86=221)에서 halo 상단(252)까지 31dp.
                    Spacer(Modifier.height(31.dp))

                    Box(
                        // Figma 실측값: halo(Vector) 360x360.
                        modifier = Modifier.size(360.dp * scale),
                        contentAlignment = Alignment.Center,
                    ) {
                        WhiteRingsHalo(modifier = Modifier.fillMaxSize())

                        StartButton(
                            onClick = onStartDiagnosis,
                            // Figma 실측값: START 버튼(Frame 2085668174) 200x200.
                            size = 200.dp * scale,
                        )
                    }
                }

                // Figma 실측값: halo 하단(252+360=612)에서 "Last updated" 영역 Top(622)까지 10dp.
                Spacer(Modifier.height(10.dp * scale))

                Text(
                    text = stringResource(R.string.home_last_updated),
                    color = MutedText,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Normal,
                )

                // Figma 실측값: "Last updated" 영역 하단(622+60=682)에서 하단 내비게이션 Top(695)까지 13dp.
                Spacer(Modifier.height(13.dp * scale))
                BottomNavigation(
                    onHomeClick = {},
                    onChecklistClick = onChecklistClick,
                    // 기존 촬영 동작은 길게 눌렀을 때만 실행되도록 남겨둔다.
                    onChecklistLongClick = onCapture,
                    onDocumentClick = onDocumentClick,
                    // 기존 온도 읽기 동작은 길게 눌렀을 때만 실행되도록 남겨둔다.
                    onDocumentLongClick = onReadTemperature,
                    onSettingsClick = onSettingsClick,
                    // 하드웨어 세팅값 다이얼로그는 길게 눌렀을 때만 열리도록 남겨둔다.
                    onSettingsLongClick = { showSettingsDialog = true },
                    selected = BottomNavKey.Home,
                    scale = scale,
                    // Figma 실측값: 하단 내비게이션(Frame 2085668332) 너비 314dp(좌우 여백 23dp씩).
                    modifier = Modifier.width(314.dp * scale),
                )
            }

            TopActions(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp * scale, vertical = 18.dp * scale),
                onMenuClick = onMenuClick,
                onNotificationClick = onNotificationClick,
                scale = scale,
            )
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
    size: androidx.compose.ui.unit.Dp = 190.dp,
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
            .size(size)
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
            text = stringResource(R.string.home_start_button),
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun TopActions(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    scale: Float = 1f,
) {
    // Row + SpaceBetween로 두 버튼을 배치하면(예전 방식) 클릭 가능 영역이 화면에
    // 보이는 버튼 위치와 어긋나는 문제가 있어서, 다른 화면들과 동일하게
    // Box + align으로 배치한다.
    Box(modifier = modifier) {
        GlassCircleIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onMenuClick,
            iconRes = R.drawable.ic_menu,
            contentDescription = stringResource(R.string.home_menu_content_description),
            size = 40.dp * scale,
        )
        GlassCircleIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = onNotificationClick,
            iconRes = R.drawable.ic_notification,
            contentDescription = stringResource(R.string.home_notification_content_description),
            size = 40.dp * scale,
        )
    }
}

@Composable
internal fun GlassCircleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconRes: Int? = null,
    icon: ImageVector? = null,
    iconTint: Color = Color.Black,
    size: androidx.compose.ui.unit.Dp = 40.dp,
) {
    val shape = CircleShape

    Box(
        modifier = modifier
            .size(size)
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
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp),
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
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

internal enum class BottomNavKey { Home, Checklist, Document, Settings }

@Composable
internal fun BottomNavigation(
    onHomeClick: () -> Unit,
    onChecklistClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onChecklistLongClick: (() -> Unit)? = null,
    onDocumentLongClick: (() -> Unit)? = null,
    onSettingsLongClick: (() -> Unit)? = null,
    selected: BottomNavKey = BottomNavKey.Home,
    scale: Float = 1f,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {
    Row(
        modifier = modifier
             .height(72.dp * scale)
            .shadow(10.dp, RoundedCornerShape(54.dp * scale))
            .clip(RoundedCornerShape(54.dp * scale))
            .background(Color.White.copy(alpha = 0.39f))
             .padding(horizontal = 8.dp * scale, vertical = 6.dp * scale),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavItem(
            icon = Icons.Outlined.Home,
            contentDescription = stringResource(R.string.home_nav_home_content_description),
            selected = selected == BottomNavKey.Home,
            onClick = onHomeClick,
            scale = scale,
        )
        BottomNavItem(
            iconRes = R.drawable.checklist,
            contentDescription = stringResource(R.string.home_nav_checklist_content_description),
            selected = selected == BottomNavKey.Checklist,
            onClick = onChecklistClick,
            onLongClick = onChecklistLongClick,
            scale = scale,
        )
        BottomNavItem(
            iconRes = R.drawable.report,
            contentDescription = stringResource(R.string.home_nav_document_content_description),
            selected = selected == BottomNavKey.Document,
            onClick = onDocumentClick,
            onLongClick = onDocumentLongClick,
            scale = scale,
        )
        BottomNavItem(
            iconRes = R.drawable.settings,
            contentDescription = stringResource(R.string.home_nav_settings_content_description),
            selected = selected == BottomNavKey.Settings,
            onClick = onSettingsClick,
            onLongClick = onSettingsLongClick,
            scale = scale,
        )
    }
}

@Composable
internal fun BottomNavItem(
    icon: ImageVector? = null,
    iconRes: Int? = null,
    contentDescription: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    scale: Float = 1f,
) {
    Box(
        modifier = Modifier
             .width(76.dp * scale)
             .height(60.dp * scale)
            .clip(RoundedCornerShape(44.dp * scale))
            .then(
                if (onLongClick != null) {
                    Modifier.pointerInput(onClick, onLongClick) {
                        detectTapGestures(
                            onTap = { onClick() },
                            onLongPress = { onLongClick() },
                        )
                    }
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(28.dp * scale),
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.Black,
                modifier = Modifier.size(30.dp * scale),
            )
        }
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
        title = { Text(stringResource(R.string.home_dialog_title)) },
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
                    label = { Text(stringResource(R.string.home_setting_value_label, selectedSetting)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.home_dialog_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.home_dialog_cancel)) } },
    )
}
