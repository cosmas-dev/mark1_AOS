package com.cosmasbio.mark1.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosmasbio.mark1.R

private val ProductsTop = Color(0xFFEFF3F6)
private val ProductsBottom = Color(0xFFD5DDE4)
private val ProductsInk = Color(0xFF16181A)
private val ProductsSubText = Color(0xFF868D93)
private val ProductsDivider = Color(0xFFECEEF0)
private val ProductsChevron = Color(0xFFB4BABF)
private val ProductsTabTrack = Color(0xFFE3E7EB)
private val ProductsLinkBlue = Color(0xFF2563EB)

private enum class ProductsTab { Reader, Kits }

@Composable
fun ProductsScreen(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onBackToHome: () -> Unit,
    onOpenReaderSettings: () -> Unit,
    onAddNewReader: () -> Unit,
    onOpenKitDetails: () -> Unit,
    onOpenDiagnosisReport: () -> Unit,
    onOpenDiagnosisManagement: () -> Unit,
    readerName: String,
    isReaderRegistered: Boolean,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(ProductsTab.Reader) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ProductsTop, ProductsBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ProductsHeader(onMenuClick = onMenuClick, onNotificationClick = onNotificationClick)

        Spacer(Modifier.height(10.dp))

        ProductsTabSwitcher(
            selected = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(18.dp))

            when (selectedTab) {
                ProductsTab.Reader -> {
                    if (isReaderRegistered) {
                        ReaderCard(name = readerName, onOpenReaderSettings = onOpenReaderSettings)
                    } else {
                        RegisterReaderCard(onAddNew = onAddNewReader)
                    }
                    Spacer(Modifier.height(18.dp))
                    ReaderLinksCard()
                }
                ProductsTab.Kits -> {
                    KitCard(onClick = onOpenKitDetails)
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.products_kit_usage_history_title),
                        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
                        color = ProductsInk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    KitUsageHistoryCard()
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        Spacer(Modifier.height(14.dp))
        Box(Modifier.padding(horizontal = 18.dp)) {
            BottomNavigation(
                onHomeClick = onBackToHome,
                onChecklistClick = onOpenDiagnosisManagement,
                onDocumentClick = onOpenDiagnosisReport,
                onSettingsClick = {},
                selected = BottomNavKey.Settings,
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun ProductsHeader(onMenuClick: () -> Unit, onNotificationClick: () -> Unit) {
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
            text = stringResource(R.string.products_header_title),
            modifier = Modifier.align(Alignment.Center),
            color = ProductsInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ProductsCircleButton(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
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
private fun ProductsTabSwitcher(
    selected: ProductsTab,
    onSelect: (ProductsTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(ProductsTabTrack)
            .padding(4.dp),
    ) {
        ProductsTabItem(
            title = stringResource(R.string.products_tab_reader),
            selected = selected == ProductsTab.Reader,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(ProductsTab.Reader) },
        )
        ProductsTabItem(
            title = stringResource(R.string.products_tab_kits),
            selected = selected == ProductsTab.Kits,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(ProductsTab.Kits) },
        )
    }
}

@Composable
private fun ProductsTabItem(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = if (selected) ProductsInk else ProductsSubText,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ReaderCard(name: String, onOpenReaderSettings: () -> Unit) {
    val displayName = stringResource(R.string.products_reader_display_name, name)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(R.drawable.device),
                contentDescription = displayName,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.products_reader_model_label), color = ProductsSubText, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = displayName,
                    color = ProductsInk,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            ProductsCircleButton(size = 32.dp, onClick = onOpenReaderSettings) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = stringResource(R.string.products_cd_more),
                    tint = ProductsInk,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Text(text = stringResource(R.string.products_reader_registration_date_label), color = ProductsSubText, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = "Mar 20, 2025  ·  v0.0.0.1", color = ProductsInk, fontSize = 15.sp)

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable { /* TODO: 리더 재등록 플로우 */ },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.products_button_re_register),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RegisterReaderCard(onAddNew: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(R.drawable.device),
                contentDescription = stringResource(R.string.products_register_reader_title),
                modifier = Modifier.size(64.dp),
                alpha = 0.5f,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.products_register_reader_title),
                    color = ProductsInk,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ProductsTabTrack)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(text = stringResource(R.string.products_no_device_found), color = ProductsSubText, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable(onClick = onAddNew),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.products_button_add_new),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ReaderLinksCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White),
    ) {
        ProductsLinkRow(stringResource(R.string.products_link_guides)) { /* TODO: 가이드 */ }
        ProductsRowDivider()
        ProductsLinkRow(stringResource(R.string.products_link_usage_history)) { /* TODO: 사용 기록 */ }
        ProductsRowDivider()
        ProductsLinkRow(stringResource(R.string.products_link_firmware_update)) { /* TODO: 펌웨어 업데이트 */ }
    }
}

@Composable
private fun ProductsLinkRow(title: String, onClick: () -> Unit) {
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
            color = ProductsInk,
            fontSize = 16.sp,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = ProductsChevron,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun ProductsRowDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 18.dp)
            .height(1.dp)
            .background(ProductsDivider),
    )
}

@Composable
private fun KitCard(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(R.drawable.kits),
                contentDescription = stringResource(R.string.products_cd_cosmas_kit),
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.products_kit_serial_label), color = ProductsSubText, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(text = "Cosmas", color = ProductsInk, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
            Text(text = "#43", color = ProductsLinkBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(20.dp))
        KitInfoField(label = stringResource(R.string.products_kit_info_test_date_label), value = "Jan 1, 2025, 4:55 PM")
        Spacer(Modifier.height(16.dp))
        KitInfoField(label = stringResource(R.string.products_kit_info_kit_type_label), value = "5-in-1 Infectious Disease Kit")
        Spacer(Modifier.height(16.dp))

        Text(text = stringResource(R.string.products_test_items_label), color = ProductsSubText, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("코로나19", "인플루엔자A", "인플루엔자B", "RSV").forEach { item ->
                KitItemChip(item)
            }
        }
    }
}

@Composable
private fun KitInfoField(label: String, value: String) {
    Column {
        Text(text = label, color = ProductsSubText, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = value, color = ProductsInk, fontSize = 15.sp)
    }
}

@Composable
private fun KitItemChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ProductsTabTrack)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(text = text, color = ProductsInk, fontSize = 12.sp)
    }
}

@Composable
private fun KitUsageHistoryCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White),
    ) {
        KitHistoryRow(date = "Jan 23, 2025, 4:55 PM", title = "5-in-1 Infectious Disease Test")
        ProductsRowDivider()
        KitHistoryRow(date = "Jan 23, 2025, 4:55 PM", title = "5-Panel Drug Test")
        ProductsRowDivider()
        KitHistoryRow(date = "Jan 23, 2025, 4:55 PM", title = "5-in-1 Chronic Disease Test")
    }
}

@Composable
private fun KitHistoryRow(date: String, title: String, onClick: () -> Unit = { /* TODO: 검사 상세 */ }) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = date, color = ProductsSubText, fontSize = 13.sp)
            Spacer(Modifier.height(3.dp))
            Text(text = title, color = ProductsInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = ProductsChevron,
            modifier = Modifier.size(22.dp),
        )
    }
}
