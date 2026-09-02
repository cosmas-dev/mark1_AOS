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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CompleteTop = Color(0xFFF1F3F5)
private val CompleteBottom = Color(0xFFDCE2E7)
private val CompleteCheckBg = Color(0xFF6F8090)
private val CompleteLinkBlue = Color(0xFF2563EB)

@Composable
fun SaveCompleteScreen(
    examId: String,
    onBack: () -> Unit,
    onStartNewExam: () -> Unit,
    onViewRecords: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CompleteTop, CompleteBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ActionHeader(title = "저장 완료", onBack = onBack, showShare = false)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(CompleteCheckBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp),
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "검사 기록이 저장되었습니다",
                color = ActionInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "관리자 시스템에 동기화 되었습니다",
                color = ActionSubText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(26.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ActionSectionTitle("검사 ID")
                ExamIdCard(
                    examId = examId,
                    onCopy = { clipboard.setText(AnnotatedString(examId)) },
                )

                Spacer(Modifier.height(6.dp))
                ActionSectionTitle("검사 결과 요약")
                ResultSummaryCard(rows = sampleTestResultRows)
            }

            Spacer(Modifier.height(20.dp))
        }

        CompleteBottomActions(
            onStartNewExam = onStartNewExam,
            onViewRecords = onViewRecords,
        )
    }
}

@Composable
private fun ExamIdCard(examId: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ActionCardBg)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = examId,
            modifier = Modifier.weight(1f),
            color = ActionInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Rounded.ContentCopy,
            contentDescription = "검사 ID 복사",
            tint = Color(0xFF6B7278),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onCopy),
        )
    }
}

@Composable
private fun CompleteBottomActions(
    onStartNewExam: () -> Unit,
    onViewRecords: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable(onClick = onStartNewExam),
            contentAlignment = Alignment.Center,
        ) {
            Text("새 검사 시작", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE7EBEE))
                .clickable(onClick = onViewRecords),
            contentAlignment = Alignment.Center,
        ) {
            Text("검사 기록 보기", color = ActionInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.clickable(onClick = rememberOpenAdminConsole()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "관리자 시스템에서 보기",
                color = CompleteLinkBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(5.dp))
            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                tint = CompleteLinkBlue,
                modifier = Modifier.size(15.dp),
            )
        }
    }
}
