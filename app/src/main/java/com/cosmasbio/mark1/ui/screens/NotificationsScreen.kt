package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R

private val NotificationsInk = Color(0xFF16181A)
private val NotificationsEmptyText = Color(0xFFAEB4B9)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 홈 화면과 같은 비율로 상단 버튼 크기·여백을 맞추기 위한 값 (디자인 기준 360dp).
    val scale = LocalConfiguration.current.screenWidthDp.dp / 360.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        NotificationsHeader(onBack = onBack, scale = scale)

        // 아직 알림 데이터 소스가 없으므로 항상 빈 상태를 보여준다.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.alarm),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = stringResource(R.string.notifications_empty_state),
                    color = NotificationsEmptyText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun NotificationsHeader(onBack: () -> Unit, scale: Float = 1f) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 다른 화면들과 동일하게 고정 높이 대신 여백으로만 크기를 정해서
            // 버튼의 화면상 위치·크기가 똑같아지게 한다.
            .padding(horizontal = 18.dp * scale, vertical = 18.dp * scale),
    ) {
        // 디자인은 기존 그대로(흰색 배경 + 그림자만 있는 단순한 원형 버튼) 유지하고,
        // 다른 화면과 같은 위치·크기가 되도록 여백/사이즈만 맞춘다.
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp * scale)
                .shadow(5.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = stringResource(R.string.notifications_back_content_description),
                tint = NotificationsInk,
                modifier = Modifier.size(26.dp),
            )
        }

        Text(
            text = stringResource(R.string.notifications_title),
            modifier = Modifier.align(Alignment.Center),
            color = NotificationsInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
