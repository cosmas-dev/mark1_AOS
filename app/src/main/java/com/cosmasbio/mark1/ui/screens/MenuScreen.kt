package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MenuTop = Color(0xFFEFF3F6)
private val MenuBottom = Color(0xFFD5DDE4)
private val MenuInk = Color(0xFF16181A)
private val MenuSectionLabel = Color(0xFF868D93)
private val MenuDivider = Color(0xFFECEEF0)
private val MenuChevron = Color(0xFFB4BABF)
private val MenuLinkBlue = Color(0xFF2563EB)
private val MenuToggleOn = Color(0xFF2F6BE8)

@Composable
fun MenuScreen(
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 아직 저장소와 연결되지 않은 화면 전용 상태.
    var saveHistory by rememberSaveable { mutableStateOf(true) }

    val openAdminConsole = rememberOpenAdminConsole()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MenuTop, MenuBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        MenuHeader(onBack = onBack, onNotifications = onNotifications)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            MenuSectionTitle("앱")
            MenuCard {
                MenuToggleRow(
                    title = "진단 이력 저장",
                    checked = saveHistory,
                    onCheckedChange = { saveHistory = it },
                )
                MenuRowDivider()
                MenuLinkRow("알림 설정", onClick = onNotifications)
                MenuRowDivider()
                MenuLinkRow("언어 변경") { /* TODO: 언어 선택 */ }
            }

            Spacer(Modifier.height(22.dp))
            MenuSectionTitle("도움")
            MenuCard {
                MenuLinkRow("앱 정보") { /* TODO: 앱 정보 */ }
                MenuRowDivider()
                MenuLinkRow("고객지원") { /* TODO: 고객지원 */ }
                MenuRowDivider()
                MenuLinkRow("서비스 안내") { /* TODO: 서비스 안내 */ }
            }

            Spacer(Modifier.height(40.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 26.dp)
                .clickable(onClick = openAdminConsole),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "관리자 시스템에서 보기",
                color = MenuLinkBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                tint = MenuLinkBlue,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun MenuHeader(onBack: () -> Unit, onNotifications: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 12.dp),
    ) {
        MenuCircleButton(Modifier.align(Alignment.CenterStart), onBack) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "뒤로",
                tint = MenuInk,
                modifier = Modifier.size(26.dp),
            )
        }

        Text(
            text = "메뉴",
            modifier = Modifier.align(Alignment.Center),
            color = MenuInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )

        MenuCircleButton(Modifier.align(Alignment.CenterEnd), onNotifications) {
            Icon(
                imageVector = Icons.Rounded.NotificationsNone,
                contentDescription = "알림",
                tint = MenuInk,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
private fun MenuCircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .shadow(5.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun MenuSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, bottom = 7.dp),
        color = MenuSectionLabel,
        fontSize = 13.sp,
    )
}

@Composable
private fun MenuCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White),
    ) {
        content()
    }
}

@Composable
private fun MenuRowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 18.dp)
            .height(1.dp)
            .background(MenuDivider),
    )
}

@Composable
private fun MenuLinkRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = MenuInk,
            fontSize = 16.sp,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MenuChevron,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun MenuToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = MenuInk,
            fontSize = 16.sp,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MenuToggleOn,
                checkedBorderColor = MenuToggleOn,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD6DBE0),
                uncheckedBorderColor = Color(0xFFD6DBE0),
            ),
        )
    }
}
