package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 검사 후 조치 유형. */
enum class PostTestAction(val title: String, val description: String) {
    FieldCheck("현장 확인", "현장에서 결과를 확인하고 안내"),
    Retest("추가 검사", "재검 또는 추가 항목 검사"),
    Handover("인계 준비", "인계서 작성 및 대상자 이송 준비"),
}

private const val MEMO_MAX_LENGTH = 200

private val ActionTop = Color(0xFFF1F3F5)
private val ActionBottom = Color(0xFFDCE2E7)
private val SaveBlueStart = Color(0xFF1B4FC4)
private val SaveBlueEnd = Color(0xFF2E74E8)

@Composable
fun PostTestActionScreen(
    onBack: () -> Unit,
    onSave: (action: PostTestAction, memo: String) -> Unit,
    onCancel: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedAction by rememberSaveable { mutableStateOf(PostTestAction.FieldCheck) }
    var memo by rememberSaveable { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ActionTop, ActionBottom)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            },
    ) {
        ActionHeader(title = "검사 후 조치", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ActionSectionTitle("검사 결과 요약")
            ResultSummaryCard(rows = sampleTestResultRows)

            Spacer(Modifier.height(6.dp))
            ActionSectionTitle("조치 유형")
            ActionTypeCard(
                selected = selectedAction,
                onSelect = { selectedAction = it },
            )

            Spacer(Modifier.height(6.dp))
            ActionSectionTitle("현장 메모")
            MemoCard(
                memo = memo,
                onMemoChange = { if (it.length <= MEMO_MAX_LENGTH) memo = it },
            )

            Spacer(Modifier.height(10.dp))
            SaveRecordButton(onClick = { onSave(selectedAction, memo) })
            Spacer(Modifier.height(10.dp))
        }

        ActionBottomBar(onCancel = onCancel, onRestart = onRestart)
    }
}

@Composable
internal fun ActionHeader(
    title: String,
    onBack: () -> Unit,
    showShare: Boolean = true,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 12.dp),
    ) {
        HeaderCircleButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBack,
        ) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "뒤로",
                tint = ActionInk,
                modifier = Modifier.size(26.dp),
            )
        }

        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            color = ActionInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )

        if (showShare) {
            HeaderCircleButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { /* TODO: 결과 공유 */ },
            ) {
                Icon(
                    imageVector = Icons.Rounded.IosShare,
                    contentDescription = "내보내기",
                    tint = ActionInk,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun HeaderCircleButton(
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
private fun ActionTypeCard(
    selected: PostTestAction,
    onSelect: (PostTestAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ActionCardBg),
    ) {
        PostTestAction.entries.forEachIndexed { index, action ->
            if (index > 0) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ActionDivider),
                )
            }
            ActionTypeRow(
                action = action,
                selected = action == selected,
                onClick = { onSelect(action) },
            )
        }
    }
}

@Composable
private fun ActionTypeRow(
    action: PostTestAction,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Color.White else Color(0xFFF7F8F9))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action.title,
                color = if (selected) ActionInk else Color(0xFF5C6369),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = action.description,
                color = ActionSubText,
                fontSize = 12.sp,
            )
        }
        RadioDot(selected = selected)
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = if (selected) ActionInk else Color(0xFFD3D8DC),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(ActionInk),
            )
        }
    }
}

@Composable
private fun MemoCard(memo: String, onMemoChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ActionCardBg)
            .padding(16.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (memo.isEmpty()) {
                Text(
                    text = "현장 상황과 대상자 진술을 입력하세요.",
                    color = Color(0xFFB0B6BB),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                )
            }
            BasicTextField(
                value = memo,
                onValueChange = onMemoChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 66.dp),
                textStyle = TextStyle(
                    color = ActionInk,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                ),
                cursorBrush = SolidColor(ActionInk),
            )
        }

        Text(
            text = "${memo.length}/$MEMO_MAX_LENGTH",
            modifier = Modifier.align(Alignment.End),
            color = ActionSubText,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun SaveRecordButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(Brush.horizontalGradient(listOf(SaveBlueStart, SaveBlueEnd)))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.SaveAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(19.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "검사 기록 저장",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun ActionBottomBar(onCancel: () -> Unit, onRestart: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(.3f)
                .height(56.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Text("취소", color = ActionInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Box(
            modifier = Modifier
                .weight(.7f)
                .height(56.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable(onClick = onRestart),
            contentAlignment = Alignment.Center,
        ) {
            Text("다시 검사", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
