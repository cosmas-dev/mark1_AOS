package com.cosmasbio.mark1.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class PermissionItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val permissionItems = listOf(
    PermissionItem(Icons.Outlined.NotificationsNone, "알림", "알림 메시지 발송"),
    PermissionItem(Icons.Outlined.PhotoCamera, "카메라", "키트, QR코드 촬영"),
    PermissionItem(Icons.Outlined.Image, "사진", "진단키트 이미지 분석"),
    PermissionItem(Icons.Outlined.LocationOn, "위치", "리더기, 진단 위치 확인"),
    PermissionItem(Icons.Outlined.Face, "Face ID", "로그인 및 인증서비스 제공"),
)

@Composable
fun PermissionScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var requesting by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        requesting = false
        // 선택 권한을 거부해도 앱의 기본 기능은 계속 사용할 수 있다.
        onComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.weight(.7f))
            Text(
                text = "앱 사용을 위해\n접근 권한을 허용해주세요",
                color = Color.Black,
                fontSize = 30.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.weight(1.1f))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                permissionItems.forEach { item -> PermissionRow(item) }
            }

            Spacer(Modifier.weight(.5f))
            Text(
                text = "선택 권한의 경우 허용하지 않아도 서비스를 사용할 수 있으나\n일부 서비스 이용이 제한될 수 있습니다.",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF9E9E9E),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.weight(1.5f))
            Button(
                onClick = {
                    if (requesting) return@Button
                    val permissions = runtimePermissions()
                    if (permissions.isEmpty()) {
                        onComplete()
                    } else {
                        requesting = true
                        permissionLauncher.launch(permissions)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp),
                enabled = !requesting,
                shape = RoundedCornerShape(37.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF333333),
                    disabledContentColor = Color.White,
                ),
            ) {
                Text(
                    text = if (requesting) "권한 요청 중..." else "동의하고 시작",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionRow(item: PermissionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = Color(0xFF555A5E),
        )
        Spacer(Modifier.width(24.dp))
        Text(
            text = item.title,
            modifier = Modifier.width(96.dp),
            color = Color.Black,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = item.description,
            modifier = Modifier.weight(1f),
            color = Color(0xFF9E9E9E),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

private fun runtimePermissions(): Array<String> = buildList {
    add(Manifest.permission.CAMERA)
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
        add(Manifest.permission.READ_MEDIA_IMAGES)
        add(Manifest.permission.NEARBY_WIFI_DEVICES)
    } else {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}.distinct().toTypedArray()
