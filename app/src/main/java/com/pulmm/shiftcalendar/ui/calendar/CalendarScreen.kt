package com.pulmm.shiftcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import com.pulmm.shiftcalendar.ui.rememberRepository
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(rememberRepository()))
    val month by viewModel.currentMonth.collectAsState()
    val dayInfos by viewModel.monthDayInfos.collectAsState()
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("${month.year}년 ${month.monthValue}월") },
            navigationIcon = {
                IconButton(onClick = viewModel::goToPreviousMonth) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "이전 달")
                }
            },
            actions = {
                IconButton(onClick = viewModel::goToNextMonth) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "다음 달")
                }
            }
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            WeekdayHeaderRow()
            MonthGrid(
                month = month,
                dayInfoByEpochDay = remember(dayInfos) { dayInfos.associateBy { it.epochDay } },
                shiftTypeById = shiftTypeById,
                onDayClick = { date -> selectedDate = date }
            )
        }
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
private fun WeekdayHeaderRow() {
    val labels = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        labels.forEachIndexed { index, label ->
            val color = when (index) { 0 -> Color.Red; 6 -> Color.Blue; else -> Color.Gray }
            Text(label, color = color, modifier = Modifier.weight(1f, fill = true), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    dayInfoByEpochDay: Map<Long, DayInfo>,
    shiftTypeById: Map<Long, ShiftType>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    val leadingBlanks = firstDayOfMonth.dayOfWeek.value % 7
    val totalDays = month.lengthOfMonth()
    val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..totalDays).map { month.atDay(it) }
    val paddedCells = cells + List((7 - cells.size % 7) % 7) { null }
    val weeks = paddedCells.chunked(7)
    val today = LocalDate.now()

    Column {
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .then(
                                if (date == today) Modifier.background(Color(0xFFFFF3CD), RoundedCornerShape(6.dp))
                                else Modifier
                            )
                            .clickable(enabled = date != null) { date?.let(onDayClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val dayInfo = dayInfoByEpochDay[date.toEpochDay()]
                            val shiftType = dayInfo?.shiftTypeId?.let { shiftTypeById[it] }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${date.dayOfMonth}", fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal)
                                if (shiftType != null) {
                                    Text(shiftType.name, style = MaterialTheme.typography.labelSmall, color = Color(shiftType.colorArgb))
                                } else if (dayInfo?.shiftTypeId != null) {
                                    Text("삭제됨", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                                if (dayInfo?.memoText?.isNotBlank() == true) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFF1C40F)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
