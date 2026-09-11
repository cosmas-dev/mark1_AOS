package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R

private val ReaderInfoInk = Color(0xFF16181A)
private val ReaderInfoSubText = Color(0xFF868D93)
private val ReaderInfoDivider = Color(0xFFECEEF0)
private val ReaderInfoChevron = Color(0xFFB4BABF)
private val ReaderInfoLinkBlue = Color(0xFF2563EB)
private val ReaderInfoBannerBg = Color(0xFFEFF4FC)

@Composable
fun ReaderInfoScreen(
    onBack: () -> Unit,
    onUserManual: () -> Unit = {},
    onBuyReader: () -> Unit = {},
    registeredAt: String = "Jan 1, 2025, 4:55 PM",
    model: String = "COSMAS LEDER 001",
    type: String = "Gen 1",
    manufactureDate: String = "Jan 1, 2025",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ReaderInfoHeader(onBack = onBack)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ReaderInfoBannerBg)
                .padding(vertical = 10.dp),
        ) {
            Text(
                text = registeredAt,
                modifier = Modifier.fillMaxWidth(),
                color = ReaderInfoSubText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }

        ReaderInfoCaptionRow(caption = stringResource(R.string.reader_info_model_label), value = model)
        ReaderInfoDividerLine()
        ReaderInfoCaptionRow(caption = stringResource(R.string.reader_info_type_label), value = type)
        ReaderInfoDividerLine()
        ReaderInfoCaptionRow(caption = stringResource(R.string.reader_info_manufacture_date_label), value = manufactureDate)
        ReaderInfoDividerLine()
        ReaderInfoPlainRow(title = stringResource(R.string.reader_info_user_manual), onClick = onUserManual)
        ReaderInfoDividerLine()
        ReaderInfoLinkRow(title = stringResource(R.string.reader_info_buy_reader), onClick = onBuyReader)
    }
}

@Composable
private fun ReaderInfoHeader(onBack: () -> Unit) {
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
                contentDescription = stringResource(R.string.reader_info_back),
                tint = ReaderInfoInk,
                modifier = Modifier.size(26.dp),
            )
        }

        Text(
            text = stringResource(R.string.reader_info_header_title),
            modifier = Modifier.align(Alignment.Center),
            color = ReaderInfoInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ReaderInfoCaptionRow(caption: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Text(text = caption, color = ReaderInfoSubText, fontSize = 13.sp)
        Spacer(Modifier.height(3.dp))
        Text(text = value, color = ReaderInfoInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ReaderInfoPlainRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = ReaderInfoInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = ReaderInfoChevron,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ReaderInfoLinkRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = ReaderInfoLinkBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = ReaderInfoLinkBlue,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ReaderInfoDividerLine() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ReaderInfoDivider),
    )
}
