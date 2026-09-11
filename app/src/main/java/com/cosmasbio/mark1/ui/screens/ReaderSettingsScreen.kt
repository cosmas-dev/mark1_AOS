package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R

private val ReaderSettingsTop = Color(0xFFEFF3F6)
private val ReaderSettingsBottom = Color(0xFFD5DDE4)
private val ReaderSettingsInk = Color(0xFF16181A)
private val ReaderSettingsSubText = Color(0xFF868D93)
private val ReaderSettingsDivider = Color(0xFFECEEF0)
private val ReaderSettingsChevron = Color(0xFFB4BABF)
private val ReaderSettingsDanger = Color(0xFFE53935)

@Composable
fun ReaderSettingsScreen(
    onBack: () -> Unit,
    onOpenReaderName: () -> Unit,
    onOpenReaderInfo: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    readerName: String = "Cosma",
    modifier: Modifier = Modifier,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(ReaderSettingsTop, ReaderSettingsBottom)))
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            ReaderSettingsHeader(onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White),
                ) {
                    ReaderSettingsCaptionRow(
                        caption = stringResource(R.string.reader_settings_owner_reader_caption),
                        title = readerName,
                        onClick = onOpenReaderName,
                    )
                    ReaderSettingsDividerLine()
                    ReaderSettingsCaptionRow(
                        caption = stringResource(R.string.reader_settings_change_network_caption),
                        title = "cosmas001",
                        onClick = { /* TODO: 네트워크 변경 */ },
                    )
                    ReaderSettingsDividerLine()
                    ReaderSettingsPlainRow(
                        title = stringResource(R.string.reader_settings_reader_info_title),
                        onClick = onOpenReaderInfo,
                    )
                    ReaderSettingsDividerLine()
                    ReaderSettingsDangerRow(
                        title = stringResource(R.string.reader_settings_delete_device_title),
                        onClick = { showDeleteConfirm = true },
                    )
                }

                Spacer(Modifier.height(40.dp))
            }
        }

        if (showDeleteConfirm) {
            DeleteDeviceDialog(
                onCancel = { showDeleteConfirm = false },
                onConfirm = {
                    showDeleteConfirm = false
                    onDeleteConfirmed()
                },
            )
        }
    }
}

@Composable
private fun DeleteDeviceDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onCancel,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .padding(top = 210.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {},
                )
                .padding(20.dp),
        ) {
            Text(text = "COSMAS", color = ReaderSettingsInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.reader_settings_delete_confirm_message),
                color = ReaderSettingsSubText,
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFEDEFF2))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                ) {
                    Text(text = stringResource(R.string.reader_settings_cancel), color = ReaderSettingsInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(ReaderSettingsDanger)
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                ) {
                    Text(text = stringResource(R.string.reader_settings_delete), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReaderSettingsHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 12.dp),
    ) {
        ReaderSettingsCircleButton(Modifier.align(Alignment.CenterStart), onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = stringResource(R.string.reader_settings_back),
                tint = ReaderSettingsInk,
                modifier = Modifier.size(26.dp),
            )
        }

        Text(
            text = stringResource(R.string.reader_settings_header_title),
            modifier = Modifier.align(Alignment.Center),
            color = ReaderSettingsInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ReaderSettingsCircleButton(
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
private fun ReaderSettingsCaptionRow(caption: String, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = caption, color = ReaderSettingsSubText, fontSize = 13.sp)
            Spacer(Modifier.height(3.dp))
            Text(text = title, color = ReaderSettingsInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = ReaderSettingsChevron,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ReaderSettingsPlainRow(title: String, onClick: () -> Unit) {
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
            color = ReaderSettingsInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = ReaderSettingsChevron,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ReaderSettingsDangerRow(title: String, onClick: () -> Unit) {
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
            color = ReaderSettingsDanger,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ReaderSettingsDividerLine() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ReaderSettingsDivider),
    )
}
