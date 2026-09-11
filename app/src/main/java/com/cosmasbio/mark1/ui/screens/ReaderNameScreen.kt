package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R

private val ReaderNameInk = Color(0xFF16181A)
private val ReaderNameSubText = Color(0xFF868D93)
private val ReaderNameFieldBorder = Color(0xFFE3E7EA)
private const val READER_NAME_MAX_BYTES = 200

@Composable
fun ReaderNameScreen(
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    initialName: String = "Cosma",
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf(initialName) }
    val byteCount = name.toByteArray(Charsets.UTF_8).size
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            // 입력창 바깥을 누르면 포커스를 해제해 키보드를 닫는다.
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        ReaderNameHeader(onBack = onBack)

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.reader_name_prompt),
                modifier = Modifier.fillMaxWidth(),
                color = ReaderNameInk,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
            )

            Spacer(Modifier.height(28.dp))

            Text(text = stringResource(R.string.reader_name_label), color = ReaderNameSubText, fontSize = 13.sp)
            Spacer(Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, ReaderNameFieldBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = { candidate ->
                        if (candidate.toByteArray(Charsets.UTF_8).size <= READER_NAME_MAX_BYTES) {
                            name = candidate
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(color = ReaderNameInk, fontSize = 16.sp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    cursorBrush = SolidColor(ReaderNameInk),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            innerTextField()
                        }
                    },
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.reader_name_byte_count, byteCount, READER_NAME_MAX_BYTES),
                modifier = Modifier.fillMaxWidth(),
                color = ReaderNameSubText,
                fontSize = 12.sp,
                textAlign = TextAlign.End,
            )

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable { onSave(name) },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = stringResource(R.string.reader_name_save), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ReaderNameHeader(onBack: () -> Unit) {
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
                contentDescription = stringResource(R.string.reader_name_back),
                tint = ReaderNameInk,
                modifier = Modifier.size(26.dp),
            )
        }

        Text(
            text = stringResource(R.string.reader_name_header_title),
            modifier = Modifier.align(Alignment.Center),
            color = ReaderNameInk,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
