package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R
import com.cosmasbio.mark1.model.ExamHistoryRow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DiagnosisMgmtTop = Color(0xFFEFF3F6)
private val DiagnosisMgmtBottom = Color(0xFFD5DDE4)
private val DiagnosisMgmtInk = Color(0xFF16181A)
private val DiagnosisMgmtSubText = Color(0xFF868D93)

@Composable
fun DiagnosisManagementScreen(
    examHistory: List<ExamHistoryRow>,
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onCreateClick: () -> Unit,
    onItemClick: (ExamHistoryRow) -> Unit,
    onMoreClick: (ExamHistoryRow) -> Unit,
    onHomeClick: () -> Unit,
    onDocumentClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DiagnosisMgmtTop, DiagnosisMgmtBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiagnosisManagementHeader(onMenuClick = onMenuClick, onNotificationClick = onNotificationClick)

        if (examHistory.isEmpty()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.diag_mgmt_empty_message),
                    textAlign = TextAlign.Center,
                    color = DiagnosisMgmtInk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                )

                Spacer(Modifier.height(28.dp))
                Image(
                    painter = painterResource(R.drawable.empty_graphic),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp),
                )
                Spacer(Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .clickable(onClick = onCreateClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.diag_mgmt_create_button),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(Modifier.height(18.dp))
                    examHistory.forEach { row ->
                        DiagnosisManagementCard(row = row, onClick = { onItemClick(row) }, onMoreClick = { onMoreClick(row) })
                        Spacer(Modifier.height(14.dp))
                    }
                    // 우측 하단 + 버튼에 마지막 카드가 가리지 않도록 여백을 남긴다.
                    Spacer(Modifier.height(70.dp))
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp)
                        .size(70.dp)
                        .shadow(9.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .clickable(onClick = onCreateClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.diag_mgmt_add_content_description),
                        tint = Color.White,
                        modifier = Modifier.size(39.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Box(Modifier.padding(horizontal = 18.dp)) {
            BottomNavigation(
                onHomeClick = onHomeClick,
                onChecklistClick = {},
                onDocumentClick = onDocumentClick,
                onSettingsClick = onSettingsClick,
                selected = BottomNavKey.Checklist,
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun DiagnosisManagementHeader(onMenuClick: () -> Unit, onNotificationClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // HomeScreen과 완전히 같은 버튼 크기·간격을 쓰기 위해 TopActions를 그대로 재사용한다.
        TopActions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            onMenuClick = onMenuClick,
            onNotificationClick = onNotificationClick,
        )

        Text(
            text = stringResource(R.string.diag_mgmt_header_title),
            modifier = Modifier.align(Alignment.Center),
            color = DiagnosisMgmtInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DiagnosisManagementCard(row: ExamHistoryRow, onClick: () -> Unit, onMoreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = .13f),
                spotColor = Color.Black.copy(alpha = .18f),
            )
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = .65f))
            .border(1.dp, Color.White.copy(alpha = .88f), RoundedCornerShape(30.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 25.dp, vertical = 23.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = formatCardDate(row.capturedAt), color = DiagnosisMgmtSubText, fontSize = 15.sp)
                    if (row.localDate() == LocalDate.now()) {
                        Spacer(Modifier.size(7.dp))
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5B65)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "N",
                                color = Color.White,
                                fontSize = 9.sp,
                                lineHeight = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                                    lineHeightStyle = LineHeightStyle(
                                        alignment = LineHeightStyle.Alignment.Center,
                                        trim = LineHeightStyle.Trim.Both,
                                    ),
                                ),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(9.dp))
                Text(
                    text = row.personName.ifBlank { stringResource(R.string.diag_mgmt_no_name) },
                    color = Color(0xFF151719),
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = .92f))
                    .clickable(onClick = onMoreClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = stringResource(R.string.diag_mgmt_more_content_description),
                    tint = Color.Black,
                    modifier = Modifier.size(27.dp),
                )
            }
        }

        Spacer(Modifier.height(13.dp))
        DiagnosisManagementDetailRow(label = stringResource(R.string.diag_mgmt_type_label), value = row.diagnosisType.ifBlank { "-" })
        Spacer(Modifier.height(8.dp))
        DiagnosisManagementDetailRow(label = stringResource(R.string.diag_mgmt_items_label), value = row.diagnosisItems.ifBlank { "-" })
    }
}

@Composable
private fun DiagnosisManagementDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = DiagnosisMgmtInk, fontSize = 17.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = DiagnosisMgmtSubText,
            fontSize = 17.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
        )
    }
}

private fun ExamHistoryRow.localDate(): LocalDate =
    Instant.ofEpochMilli(capturedAt).atZone(ZoneId.systemDefault()).toLocalDate()

private fun formatCardDate(capturedAt: Long): String =
    Instant.ofEpochMilli(capturedAt).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH))
