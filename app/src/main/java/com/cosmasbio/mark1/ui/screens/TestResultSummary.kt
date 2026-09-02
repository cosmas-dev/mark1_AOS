package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 검사 항목 한 줄. 아직 실제 분석 결과와 연결되지 않은 표시용 값이다. */
data class TestResultRow(
    val name: String,
    val value: String,
    val unit: String,
    val negative: Boolean,
)

val sampleTestResultRows = listOf(
    TestResultRow("마리화나", "0.3", "ng/mL", negative = true),
    TestResultRow("펜타닐", "21.5", "ng/mL", negative = false),
)

internal val ActionInk = Color(0xFF16181A)
internal val ActionSubText = Color(0xFF868D93)
internal val ActionDivider = Color(0xFFEDEFF1)
internal val ActionCardBg = Color(0xFFFFFFFF)
internal val NegativeBg = Color(0xFFE3FBEC)
internal val NegativeText = Color(0xFF22B455)
internal val PositiveBg = Color(0xFFFFE1E4)
internal val PositiveText = Color(0xFFFF4E59)

/** 검사 후 조치 / 저장 완료 화면에서 공통으로 쓰는 결과 요약 카드. */
@Composable
fun ResultSummaryCard(
    rows: List<TestResultRow>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ActionCardBg),
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ActionDivider),
                )
            }
            ResultSummaryRow(row)
        }
    }
}

@Composable
private fun ResultSummaryRow(row: TestResultRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = row.name,
            modifier = Modifier.weight(1f),
            color = ActionInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        ResultBadge(negative = row.negative)
        Spacer(Modifier.width(12.dp))
        Text(
            text = row.value,
            color = ActionInk,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = row.unit,
            color = ActionSubText,
            fontSize = 11.sp,
        )
    }
}

@Composable
internal fun ResultBadge(negative: Boolean, size: Int = 24) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(if (negative) NegativeBg else PositiveBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (negative) "N" else "P",
            color = if (negative) NegativeText else PositiveText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** 화면 상단 섹션 제목. */
@Composable
internal fun ActionSectionTitle(text: String) {
    Text(
        text = text,
        color = ActionInk,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
    )
}
