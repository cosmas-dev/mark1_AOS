package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R

data class DiagnosisProfile(
    val date: String,
    val name: String,
    val diagnosisType: String,
    val diagnosisItems: String,
    val isNew: Boolean = true,
    // 서버(persons 테이블)로 함께 전송되는 대상자 정보.
    // 비워두면 이름만 저장되므로 목록에서 선택한 검사도 반드시 채워서 넘긴다.
    val dateOfBirth: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val organization: String = "",
)

private val DiagnoseTop = Color(0xFFE8F0F5)
private val DiagnoseBottom = Color(0xFFC5D1D9)
private val DiagnoseText = Color(0xFF202326)
private val DiagnoseMutedText = Color(0xFF555A5E)

private val sampleProfiles = listOf(
//    DiagnosisProfile("2025-01-23", "Arlene", "Multi-Panel Drug Test", "THC, FYL"),
//    DiagnosisProfile("2025-01-23", "Darrell", "Multi-Panel Drug Test", "THC, FYL"),
//    DiagnosisProfile("2025-01-23", "Eduardo", "Multi-Panel Drug Test", "THC, FYL"),
//    DiagnosisProfile("2025-01-23", "Arthur", "Multi-Panel Drug Test", "THC, FYL"),
//    DiagnosisProfile("2025-01-23", "Marlene", "Multi-Panel Drug Test", "THC, FYL"),
    DiagnosisProfile(
        "2026-08-23", "박경찰", "Multi-Panel Drug Test", "THC, FYL",
        dateOfBirth = "1988-03-14", email = "park@police.go.kr",
        phoneNumber = "010-2841-7702", organization = "경찰청 마약수사대",
    ),
    DiagnosisProfile(
        "2026-08-23", "김경감", "Multi-Panel Drug Test", "THC, FYL",
        dateOfBirth = "1979-11-02", email = "kim@police.go.kr",
        phoneNumber = "010-3315-8890", organization = "경찰청 강력계",
    ),
    DiagnosisProfile(
        "2026-08-23", "고길동", "Multi-Panel Drug Test", "THC, FYL",
        dateOfBirth = "1995-06-27", email = "ko@example.com",
        phoneNumber = "010-7742-1163", organization = "서울지방경찰청",
    ),
    DiagnosisProfile(
        "2026-08-23", "김철수", "Multi-Panel Drug Test", "THC, FYL",
        dateOfBirth = "2001-01-09", email = "kimcs@example.com",
        phoneNumber = "010-9026-4417", organization = "서울지방경찰청",
    ),
    DiagnosisProfile(
        "2026-08-23", "유재석", "Multi-Panel Drug Test", "THC, FYL",
        dateOfBirth = "1992-08-30", email = "yoo@example.com",
        phoneNumber = "010-5518-2274", organization = "경기남부경찰청",
    ),
)

@Composable
fun DiagnoseScreen(
    onClose: () -> Unit,
    onProfileClick: (DiagnosisProfile) -> Unit,
    onAddClick: () -> Unit,
    onMoreClick: (DiagnosisProfile) -> Unit,
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
                .background(Brush.verticalGradient(listOf(DiagnoseTop, DiagnoseBottom)))
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 15.dp,
                    end = 15.dp,
                    bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item {
                    DiagnoseHeader(onClose = onClose)
                    Spacer(Modifier.height(51.dp))
                    Text(
                        text = stringResource(R.string.diagnose_title),
                        modifier = Modifier.fillMaxWidth(),
                        color = DiagnoseText,
                        fontSize = 28.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.diagnose_subtitle),
                        modifier = Modifier.fillMaxWidth(),
                        color = DiagnoseText,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(23.dp))
                }

                items(sampleProfiles, key = { it.name }) { profile ->
                    DiagnosisProfileCard(
                        profile = profile,
                        onClick = { onProfileClick(profile) },
                        onMoreClick = { onMoreClick(profile) },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 15.dp, bottom = 15.dp)
                    .size(70.dp)
                    .shadow(9.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.diagnose_add_content_description),
                    tint = Color.White,
                    modifier = Modifier.size(39.dp),
                )
            }
        }
    }
}

@Composable
private fun DiagnoseHeader(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
    ) {
        Text(
            text = stringResource(R.string.diagnose_header_title),
            modifier = Modifier.align(Alignment.Center),
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        GlassCircleIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = onClose,
            icon = Icons.Rounded.Close,
            contentDescription = stringResource(R.string.diagnose_close_content_description),
        )
    }
}

@Composable
private fun DiagnosisProfileCard(
    profile: DiagnosisProfile,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
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
                    Text(profile.date, color = DiagnoseMutedText, fontSize = 15.sp)
                    if (profile.isNew) {
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
                    text = profile.name,
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
                    contentDescription = stringResource(R.string.diagnose_more_options_content_description),
                    tint = Color.Black,
                    modifier = Modifier.size(27.dp),
                )
            }
        }

        Spacer(Modifier.height(13.dp))
        ProfileDetailRow(stringResource(R.string.diagnose_type_label), profile.diagnosisType)
        Spacer(Modifier.height(8.dp))
        ProfileDetailRow(stringResource(R.string.diagnose_items_label), profile.diagnosisItems)
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = DiagnoseText, fontSize = 17.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = DiagnoseMutedText,
            fontSize = 17.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
        )
    }
}
