package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.material.icons.rounded.Check
import com.cosmasbio.mark1.R

private val ConnectTop = Color(0xFFE8F0F5)
private val ConnectBottom = Color(0xFFC5D1D9)
private val HelpTextColor = Color(0xFF858A8E)

@Composable
fun ConnectDeviceScreen(
    onClose: () -> Unit,
    onConnect: () -> Unit,
    isConnected: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ConnectTop,
                            ConnectBottom,
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,

                ) {
                ConnectTopBar(
                    onClose = onClose,
                )

                Spacer(modifier = Modifier.height(62.dp))

                Text(
                    text = if (isConnected) {
                        "Reader connected.\nStart diagnosis now?"
                    } else {
                        "Connecting the reader will start the diagnosis. Would you like to connect?"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF202124),
                    fontSize = 26.sp,
                    lineHeight = 33.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                )


            }

            Image(
                painter = painterResource(id = R.drawable.device),
                contentDescription = "COSMAS reader device",
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.65f)
                    .padding(top = 70.dp),
                contentScale = ContentScale.Fit,
            )

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 130.dp),
                visible = isConnected,
                enter = scaleIn(
                    initialScale = .25f,
                    animationSpec = spring(
                        dampingRatio = .48f,
                        stiffness = 420f,
                    ),
                ) + fadeIn(animationSpec = tween(240)),
                exit = fadeOut(),
            ) {
                ConnectionSuccessBadge(
                    modifier = Modifier.padding(bottom = 18.dp),
                )
            }

            Text(
                text = "Need help?",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp),
                color = HelpTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
            )

            ConnectButton(
                text = if (isConnected) "Continue" else "Connect",
                onClick = if (isConnected) onContinue else onConnect,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
            )
        }
    }
}

@Composable
private fun ConnectionSuccessBadge(
    modifier: Modifier = Modifier,
) {
    val pulseTransition = rememberInfiniteTransition(label = "connected_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_250),
            repeatMode = RepeatMode.Restart,
        ),
        label = "connected_pulse_scale",
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = .34f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_250),
            repeatMode = RepeatMode.Restart,
        ),
        label = "connected_pulse_alpha",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val touchScale by animateFloatAsState(
        targetValue = if (isPressed) .86f else 1f,
        animationSpec = spring(dampingRatio = .55f, stiffness = 650f),
        label = "connected_touch_scale",
    )

    Box(
        modifier = modifier.size(82.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(Color(0xFF6F8090).copy(alpha = pulseAlpha)),
        )

        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(touchScale)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF6F8090).copy(alpha = .30f),
                    spotColor = Color(0xFF6F8090).copy(alpha = .38f),
                )
                .clip(CircleShape)
                .background(Color(0xFF6F8090))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {},
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Reader connected",
                tint = Color.White,
                modifier = Modifier.size(35.dp),
            )
        }
    }
}

@Composable
private fun ConnectTopBar(
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.size(48.dp))

        Text(
            text = "Connect Device",
            modifier = Modifier.weight(1f),
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        CloseCircleButton(
            onClick = onClose,
        )
    }
}

@Composable
private fun CloseCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = CircleShape

    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(
                elevation = 8.dp,
                shape = shape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.15f),
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.82f),
                        Color.White.copy(alpha = 0.42f),
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
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "닫기",
            tint = Color.Black,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun ConnectButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.65f,
            stiffness = 550f,
        ),
        label = "connect_button_scale",
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 9.dp,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 600f,
        ),
        label = "connect_button_elevation",
    )

    Box(
        modifier = modifier
            .height(64.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(32.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.22f),
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (isPressed) {
                    Color(0xFF171717)
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
            text = text,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
