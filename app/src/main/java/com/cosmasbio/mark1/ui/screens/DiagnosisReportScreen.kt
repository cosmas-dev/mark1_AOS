package com.cosmasbio.mark1.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
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
import java.time.temporal.WeekFields
import java.util.Locale

private val ReportTop = Color(0xFFEFF3F6)
private val ReportBottom = Color(0xFFD5DDE4)
private val ReportInk = Color(0xFF16181A)
private val ReportSubText = Color(0xFF868D93)
private val ReportTabTrack = Color(0xFFE3E7EB)

private enum class ReportPeriod { Day, Week, Month, Year }

private enum class ReportSearchFilter {
    All,
    Name,
    DiagnosisType,
    Organization,
}

@Composable
private fun reportPeriodLabel(period: ReportPeriod): String = when (period) {
    ReportPeriod.Day -> stringResource(R.string.diag_report_period_day)
    ReportPeriod.Week -> stringResource(R.string.diag_report_period_week)
    ReportPeriod.Month -> stringResource(R.string.diag_report_period_month)
    ReportPeriod.Year -> stringResource(R.string.diag_report_period_year)
}

@Composable
private fun reportSearchFilterLabel(filter: ReportSearchFilter): String = when (filter) {
    ReportSearchFilter.All -> stringResource(R.string.diag_report_filter_all)
    ReportSearchFilter.Name -> stringResource(R.string.diag_report_filter_name)
    ReportSearchFilter.DiagnosisType -> stringResource(R.string.diag_report_filter_diagnosis_type)
    ReportSearchFilter.Organization -> stringResource(R.string.diag_report_filter_organization)
}

@Composable
fun DiagnosisReportScreen(
    examHistory: List<ExamHistoryRow>,
    onMenuClick: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (ExamHistoryRow) -> Unit,
    onMoreClick: (ExamHistoryRow) -> Unit,
    onHomeClick: () -> Unit,
    onChecklistClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.Day) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var didAutoJumpToLatest by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFilter by remember { mutableStateOf(ReportSearchFilter.All) }

    // 검색 모드에서는 기기 뒤로가기로 검색을 닫고 평소 헤더로 돌아간다.
    BackHandler(enabled = isSearchMode) {
        isSearchMode = false
        searchQuery = ""
        searchFilter = ReportSearchFilter.All
    }

    // 오늘 날짜에 기록이 없는 경우가 많으므로, 데이터가 처음 로드되면
    // 가장 최근 검사가 있는 날짜로 한 번만 자동 이동한다(이후 수동 이동은 그대로 존중).
    LaunchedEffect(examHistory) {
        if (!didAutoJumpToLatest && examHistory.isNotEmpty()) {
            selectedDate = examHistory.maxBy { it.capturedAt }.localDate()
            didAutoJumpToLatest = true
        }
    }

    val periodFilteredRows = remember(examHistory, selectedPeriod, selectedDate) {
        examHistory.filter { matchesPeriod(it, selectedDate, selectedPeriod) }
    }
    val visibleRows = remember(periodFilteredRows, searchQuery, searchFilter) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            periodFilteredRows
        } else {
            periodFilteredRows.filter { row -> row.matchesSearch(query, searchFilter) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(ReportTop, ReportBottom)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        if (isSearchMode) {
            ReportSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                filter = searchFilter,
                onFilterChange = { searchFilter = it },
            )
        } else {
            ReportHeader(onMenuClick = onMenuClick, onSearchClick = { isSearchMode = true })
        }

        Spacer(Modifier.height(6.dp))

        ReportDateNavigator(
            date = selectedDate,
            onPrev = { selectedDate = shiftDate(selectedDate, selectedPeriod, -1) },
            onNext = { selectedDate = shiftDate(selectedDate, selectedPeriod, 1) },
        )

        Spacer(Modifier.height(14.dp))

        ReportPeriodSwitcher(
            selected = selectedPeriod,
            onSelect = { selectedPeriod = it },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (visibleRows.isEmpty()) {
            if (isSearchMode && searchQuery.isNotBlank()) {
                ReportSearchEmptyState(modifier = Modifier.weight(1f), query = searchQuery)
            } else {
                ReportEmptyState(
                    modifier = Modifier.weight(1f),
                    onAddClick = onAddClick,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(18.dp))
                visibleRows.forEach { row ->
                    ReportCard(row = row, onClick = { onItemClick(row) }, onMoreClick = { onMoreClick(row) })
                    Spacer(Modifier.height(14.dp))
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(14.dp))
        Box(Modifier.padding(horizontal = 18.dp)) {
            BottomNavigation(
                onHomeClick = onHomeClick,
                onChecklistClick = onChecklistClick,
                onDocumentClick = {},
                onSettingsClick = onSettingsClick,
                selected = BottomNavKey.Document,
            )
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun ReportHeader(onMenuClick: () -> Unit, onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 18.dp),
    ) {
        GlassCircleIconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onMenuClick,
            iconRes = R.drawable.ic_menu,
            contentDescription = stringResource(R.string.diag_report_menu_content_description),
        )

        Text(
            text = stringResource(R.string.diag_report_header_title),
            modifier = Modifier.align(Alignment.Center),
            color = ReportInk,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        GlassCircleIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            onClick = onSearchClick,
            icon = Icons.Rounded.Search,
            contentDescription = stringResource(R.string.diag_report_search_content_description),
        )
    }
}

@Composable
private fun ReportSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: ReportSearchFilter,
    onFilterChange: (ReportSearchFilter) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    var isFilterMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .height(64.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .clickable { isFilterMenuOpen = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = reportSearchFilterLabel(filter), color = ReportInk, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = ReportInk,
                    modifier = Modifier.size(18.dp),
                )
            }

            DropdownMenu(
                expanded = isFilterMenuOpen,
                onDismissRequest = { isFilterMenuOpen = false },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
            ) {
                ReportSearchFilter.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = reportSearchFilterLabel(option),
                                color = ReportInk,
                                fontSize = 14.sp,
                                fontWeight = if (option == filter) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                        onClick = {
                            onFilterChange(option)
                            isFilterMenuOpen = false
                        },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = ReportSubText,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(text = stringResource(R.string.diag_report_search_placeholder), color = ReportSubText, fontSize = 14.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(color = ReportInk, fontSize = 14.sp),
                    singleLine = true,
                    cursorBrush = SolidColor(ReportInk),
                )
            }
        }
    }
}

@Composable
private fun ReportSearchEmptyState(modifier: Modifier = Modifier, query: String) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.diag_report_search_empty, query),
            textAlign = TextAlign.Center,
            color = ReportSubText,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun ReportCircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
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
private fun ReportDateNavigator(date: LocalDate, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.ChevronLeft,
            contentDescription = stringResource(R.string.diag_report_prev_content_description),
            tint = ReportInk,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onPrev),
        )
        Text(
            text = date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)),
            color = ReportInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = stringResource(R.string.diag_report_next_content_description),
            tint = ReportInk,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onNext),
        )
    }
}

@Composable
private fun ReportPeriodSwitcher(
    selected: ReportPeriod,
    onSelect: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ReportTabTrack)
            .padding(4.dp),
    ) {
        ReportPeriod.entries.forEach { period ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (period == selected) Color.White else Color.Transparent)
                    .clickable { onSelect(period) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = reportPeriodLabel(period),
                    color = if (period == selected) ReportInk else ReportSubText,
                    fontSize = 14.sp,
                    fontWeight = if (period == selected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ReportEmptyState(modifier: Modifier = Modifier, onAddClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.diag_report_empty_message),
            textAlign = TextAlign.Center,
            color = ReportInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 21.sp,
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .clickable(onClick = onAddClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.diag_report_add_content_description),
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun ReportCard(row: ExamHistoryRow, onClick: () -> Unit, onMoreClick: () -> Unit) {
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
                    Text(text = formatCardDate(row.capturedAt), color = ReportSubText, fontSize = 15.sp)
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
                                text = "N",
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
                    text = row.personName.ifBlank { stringResource(R.string.diag_report_no_name) },
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
                    contentDescription = stringResource(R.string.diag_report_more_content_description),
                    tint = Color.Black,
                    modifier = Modifier.size(27.dp),
                )
            }
        }

        Spacer(Modifier.height(13.dp))
        ReportDetailRow(label = stringResource(R.string.diag_report_type_label), value = row.diagnosisType.ifBlank { "-" })
        Spacer(Modifier.height(8.dp))
        ReportDetailRow(label = stringResource(R.string.diag_report_items_label), value = row.diagnosisItems.ifBlank { "-" })
    }
}

@Composable
private fun ReportDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = ReportInk, fontSize = 17.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            color = ReportSubText,
            fontSize = 17.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
        )
    }
}

private fun ExamHistoryRow.localDate(): LocalDate =
    Instant.ofEpochMilli(capturedAt).atZone(ZoneId.systemDefault()).toLocalDate()

private fun matchesPeriod(row: ExamHistoryRow, date: LocalDate, period: ReportPeriod): Boolean {
    val rowDate = row.localDate()
    return when (period) {
        ReportPeriod.Day -> rowDate == date
        ReportPeriod.Week -> {
            val field = WeekFields.of(Locale.getDefault())
            rowDate.get(field.weekOfWeekBasedYear()) == date.get(field.weekOfWeekBasedYear()) &&
                rowDate.get(field.weekBasedYear()) == date.get(field.weekBasedYear())
        }
        ReportPeriod.Month -> rowDate.year == date.year && rowDate.month == date.month
        ReportPeriod.Year -> rowDate.year == date.year
    }
}

private fun ExamHistoryRow.matchesSearch(query: String, filter: ReportSearchFilter): Boolean =
    when (filter) {
        ReportSearchFilter.All ->
            personName.contains(query, ignoreCase = true) ||
                diagnosisType.contains(query, ignoreCase = true) ||
                diagnosisItems.contains(query, ignoreCase = true) ||
                organization.contains(query, ignoreCase = true)
        ReportSearchFilter.Name -> personName.contains(query, ignoreCase = true)
        ReportSearchFilter.DiagnosisType -> diagnosisType.contains(query, ignoreCase = true)
        ReportSearchFilter.Organization -> organization.contains(query, ignoreCase = true)
    }

private fun shiftDate(date: LocalDate, period: ReportPeriod, amount: Long): LocalDate =
    when (period) {
        ReportPeriod.Day -> date.plusDays(amount)
        ReportPeriod.Week -> date.plusWeeks(amount)
        ReportPeriod.Month -> date.plusMonths(amount)
        ReportPeriod.Year -> date.plusYears(amount)
    }

private fun formatCardDate(capturedAt: Long): String =
    Instant.ofEpochMilli(capturedAt).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH))
