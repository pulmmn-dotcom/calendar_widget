package com.pulmm.shiftcalendar.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import com.pulmm.shiftcalendar.logic.KoreanHolidays
import com.pulmm.shiftcalendar.ui.rememberRepository
import com.pulmm.shiftcalendar.ui.theme.CalendarColors
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val MinCellHeight = 44.dp

@Composable
fun CalendarScreen(startMonth: YearMonth? = null) {
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(rememberRepository(), startMonth))
    val month by viewModel.currentMonth.collectAsState()
    val dayInfos by viewModel.monthDayInfos.collectAsState()
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val dragAccumulator = remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { dragAccumulator.value = 0f },
                    onDragCancel = { dragAccumulator.value = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulator.value += dragAmount
                        when {
                            dragAccumulator.value > 120f -> {
                                viewModel.goToPreviousMonth()
                                dragAccumulator.value = 0f
                            }
                            dragAccumulator.value < -120f -> {
                                viewModel.goToNextMonth()
                                dragAccumulator.value = 0f
                            }
                        }
                    }
                )
            }
    ) {
        CalendarHeader(
            month = month,
            onPrevious = viewModel::goToPreviousMonth,
            onNext = viewModel::goToNextMonth,
            onTitleClick = { showMonthPicker = true }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(CalendarColors.surfaceCard)
        ) {
            HorizontalDivider(color = CalendarColors.gridLine)
            WeekdayHeaderRow()
            HorizontalDivider(color = CalendarColors.gridLine)
            MonthGrid(
                month = month,
                dayInfoByEpochDay = remember(dayInfos) { dayInfos.associateBy { it.epochDay } },
                shiftTypeById = shiftTypeById,
                onDayClick = { date -> selectedDate = date },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialYearMonth = month,
            onDismiss = { showMonthPicker = false },
            onConfirm = { picked ->
                viewModel.goToMonth(picked)
                showMonthPicker = false
            }
        )
    }

    selectedDate?.let { date ->
        val dayInfo = dayInfos.find { it.epochDay == date.toEpochDay() }
        DayEditSheet(
            date = date,
            dayInfo = dayInfo,
            shiftTypes = shiftTypes,
            onDismiss = { selectedDate = null },
            onSelectShiftType = { shiftTypeId -> viewModel.setOverride(date, shiftTypeId) },
            onClearShiftType = { viewModel.setOverride(date, null) },
            onSaveMemo = { text -> viewModel.setMemo(date, text) }
        )
    }
}

@Composable
private fun CalendarHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "이전 달")
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onTitleClick)
                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${month.year}년 ${month.monthValue}월",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "다음 달")
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val labels = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        labels.forEachIndexed { index, label ->
            val color = when (index) {
                0 -> CalendarColors.sunday
                6 -> CalendarColors.saturday
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                modifier = Modifier.weight(1f, fill = true),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    dayInfoByEpochDay: Map<Long, DayInfo>,
    shiftTypeById: Map<Long, ShiftType>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val weeks = remember(month) { buildMonthGrid(month, weekStartMonday = false) }
    val today = LocalDate.now()

    // 주 행들이 남는 높이를 똑같이 나눠 갖고, 칸 최소 높이(44dp)에 못 미치면 스크롤한다.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val rowHeight: Dp = maxOf(MinCellHeight, maxHeight / weeks.size)
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            weeks.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth().height(rowHeight)) {
                    week.forEach { date ->
                        val inMonth = YearMonth.from(date) == month
                        val dayInfo = dayInfoByEpochDay[date.toEpochDay()]
                        val shiftId = dayInfo?.shiftTypeId
                        DayCell(
                            date = date,
                            inMonth = inMonth,
                            isToday = inMonth && date == today,
                            shiftType = shiftId?.let { shiftTypeById[it] },
                            isShiftDeleted = shiftId != null && shiftTypeById[shiftId] == null,
                            holidayName = KoreanHolidays.nameOf(date),
                            memo = dayInfo?.memoText?.takeIf { it.isNotBlank() },
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    isToday: Boolean,
    shiftType: ShiftType?,
    isShiftDeleted: Boolean,
    holidayName: String?,
    memo: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberColor = when {
        !inMonth -> CalendarColors.otherMonthText
        date.dayOfWeek == DayOfWeek.SUNDAY || holidayName != null -> CalendarColors.sunday
        date.dayOfWeek == DayOfWeek.SATURDAY -> CalendarColors.saturday
        else -> MaterialTheme.colorScheme.onSurface
    }
    val todayShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .border(0.5.dp, CalendarColors.gridLine)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
                .then(
                    if (isToday) {
                        Modifier
                            .background(CalendarColors.todayBg, todayShape)
                            .border(BorderStroke(1.5.dp, CalendarColors.todayRing), todayShape)
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(top = 3.dp, start = 2.dp, end = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${date.dayOfMonth}",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = numberColor
                )
                if (holidayName != null) {
                    Text(
                        holidayName,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CalendarColors.sunday,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
                Spacer(modifier = Modifier.height(1.dp))
                if (shiftType != null) {
                    ShiftBadge(name = shiftType.name, color = Color(shiftType.colorArgb))
                } else if (isShiftDeleted) {
                    DeletedShiftBadge()
                }
                if (memo != null) {
                    // 메모는 남은 높이만큼 앞부분을 보여주고, 넘치는 뒷부분은 잘라낸다.
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        memo,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(3.dp))
                            .background(CalendarColors.memoDot.copy(alpha = 0.12f))
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthYearPickerDialog(
    initialYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYearMonth.year) }
    var selectedMonth by remember { mutableStateOf(initialYearMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = CalendarColors.surfaceSheet,
        title = { Text("연월 이동", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear -= 1 }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "이전 연도")
                    }
                    Text(
                        "${selectedYear}년",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(onClick = { selectedYear += 1 }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "다음 연도")
                    }
                }
                (1..12).chunked(3).forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMonths.forEach { monthValue ->
                            MonthPill(
                                label = "${monthValue}월",
                                selected = monthValue == selectedMonth,
                                onClick = { selectedMonth = monthValue },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonth)) }) {
                Text("이동")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun MonthPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CircleShape
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.primary else CalendarColors.surfaceBg)
            .then(if (selected) Modifier else Modifier.border(1.dp, CalendarColors.borderSubtle, shape))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
