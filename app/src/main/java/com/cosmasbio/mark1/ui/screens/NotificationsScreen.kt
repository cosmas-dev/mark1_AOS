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
import androidx.compose.ui.res.painterResource
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        NotificationsHeader(onBack = onBack)

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
                    text = "아직 알림이 없습니다",
                    color = NotificationsEmptyText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun NotificationsHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(38.dp)
                .shadow(5.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "뒤로",
                tint = NotificationsInk,
                modifier = Modifier.size(26.dp),
            )
        }

        Text(
            text = "알림",
            modifier = Modifier.align(Alignment.Center),
            color = NotificationsInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
