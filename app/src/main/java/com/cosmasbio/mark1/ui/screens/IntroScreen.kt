package com.cosmasbio.mark1.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R
import kotlinx.coroutines.delay

/** 로그인 버튼이 나타나기까지의 대기 시간. */
private const val LOGIN_REVEAL_DELAY_MS = 3_000L

/** intro.png 원본 비율(1646 x 2928). 전체 너비로 깔기 위해 사용한다. */
private const val INTRO_ASPECT_RATIO = 1646f / 2928f

/** 경찰청 + COSMAS 합본 로고 비율(732 x 900). */
private const val INTRO_LOGO_ASPECT_RATIO = 732f / 900f

/** 제품 사진을 아래로 내리는 양. 값을 키우면 로고와의 간격이 넓어진다. */
private val INTRO_DROP = 80.dp

@Composable
fun IntroScreen(
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLogin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(LOGIN_REVEAL_DELAY_MS)
        showLogin = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // 이미지 위쪽은 흰색에서 대리석으로 이어지는 그라데이션이다. 로고 아래에서 시작하도록
        // 잘라내면 경계선이 생기므로, 전체 너비 그대로 깔아 흰 여백이 로고 영역까지
        // 자연스럽게 이어지게 한다.
        Image(
            painter = painterResource(R.drawable.intro),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                // 로고와 제품 사진이 겹치지 않도록 아래로 내린다.
                // 원본 아래쪽 1/4 은 빈 여백이라 화면 밖으로 나가도 손실이 없다.
                .offset(y = INTRO_DROP)
                .fillMaxWidth()
                .aspectRatio(INTRO_ASPECT_RATIO),
            contentScale = ContentScale.FillWidth,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))
            // 경찰청 + COSMAS 가 하나로 합쳐진 로고 이미지.
            Image(
                painter = painterResource(R.drawable.intro_police_cosmas),
                contentDescription = stringResource(R.string.intro_police_cosmas_desc),
                modifier = Modifier
                    .width(176.dp)
                    .aspectRatio(INTRO_LOGO_ASPECT_RATIO),
                contentScale = ContentScale.Fit,
            )
        }

        AnimatedVisibility(
            visible = showLogin,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            enter = fadeIn(animationSpec = tween(450)) +
                slideInVertically(animationSpec = tween(450)) { it / 2 },
        ) {
            LoginButton(onClick = onLogin)
        }
    }
}

@Composable
private fun LoginButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.intro_login_button),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
