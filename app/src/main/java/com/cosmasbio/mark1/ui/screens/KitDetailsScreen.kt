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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R

private val KitDetailsTop = Color(0xFFEFF3F6)
private val KitDetailsBottom = Color(0xFFD5DDE4)
private val KitDetailsInk = Color(0xFF16181A)
private val KitDetailsSubText = Color(0xFF868D93)
private val KitDetailsDivider = Color(0xFFECEEF0)
private val KitDetailsChevron = Color(0xFFB4BABF)
private val KitDetailsLinkBlue = Color(0xFF2563EB)
private val KitDetailsDanger = Color(0xFFE53935)

@Composable
fun KitDetailsScreen(
    onBack: () -> Unit,
    onKitManual: () -> Unit = {},
    onBuyKit: () -> Unit = {},
    onDeleteKitHistory: () -> Unit = {},
    testDate: String = "Jan 1, 2025, 4:55 PM",
    kitModel: String = "COSMAS KIT 001",
    kitName: String = "Cosmas",
    detectedConditions: String = "COVID-19, Influenza A, Influenza B, RSV, and Flu Panel (5 Targets)",
    category: String = "Multiplex Rapid Infectious Disease Test",
    manufacturingDate: String = "Jan 1, 2025",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(KitDetailsTop, KitDetailsBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        KitDetailsHeader(onBack = onBack)

        Text(
            text = testDate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            color = KitDetailsSubText,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
            ) {
                KitDetailsCaptionRow(caption = stringResource(R.string.kit_details_caption_model), value = kitModel)
                KitDetailsDividerLine()
                KitDetailsCaptionRow(caption = stringResource(R.string.kit_details_caption_name), value = kitName)
                KitDetailsDividerLine()
                KitDetailsCaptionRow(
                    caption = stringResource(R.string.kit_details_caption_detected_conditions),
                    value = detectedConditions,
                )
                KitDetailsDividerLine()
                KitDetailsCaptionRow(caption = stringResource(R.string.kit_details_caption_category), value = category)
                KitDetailsDividerLine()
                KitDetailsCaptionRow(
                    caption = stringResource(R.string.kit_details_caption_manufacturing_date),
                    value = manufacturingDate,
                )
                KitDetailsDividerLine()
                KitDetailsPlainRow(title = stringResource(R.string.kit_details_row_kit_manual), onClick = onKitManual)
                KitDetailsDividerLine()
                KitDetailsLinkRow(title = stringResource(R.string.kit_details_row_buy_kit), onClick = onBuyKit)
                KitDetailsDividerLine()
                KitDetailsDangerRow(
                    title = stringResource(R.string.kit_details_row_delete_kit_history),
                    onClick = onDeleteKitHistory,
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun KitDetailsHeader(onBack: () -> Unit) {
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
                contentDescription = stringResource(R.string.kit_details_cd_back),
                tint = KitDetailsInk,
                modifier = Modifier.size(26.dp),
            )
        }

        Text(
            text = stringResource(R.string.kit_details_header_title),
            modifier = Modifier.align(Alignment.Center),
            color = KitDetailsInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun KitDetailsCaptionRow(caption: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(text = caption, color = KitDetailsSubText, fontSize = 13.sp)
        Spacer(Modifier.height(3.dp))
        Text(text = value, color = KitDetailsInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun KitDetailsPlainRow(title: String, onClick: () -> Unit) {
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
            color = KitDetailsInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = KitDetailsChevron,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun KitDetailsLinkRow(title: String, onClick: () -> Unit) {
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
            color = KitDetailsLinkBlue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = KitDetailsLinkBlue,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun KitDetailsDangerRow(title: String, onClick: () -> Unit) {
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
            color = KitDetailsDanger,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun KitDetailsDividerLine() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(KitDetailsDivider),
    )
}
