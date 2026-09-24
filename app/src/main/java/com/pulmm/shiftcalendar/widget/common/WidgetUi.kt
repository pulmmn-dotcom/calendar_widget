package com.pulmm.shiftcalendar.widget.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun WidgetRoot(style: WidgetStyle, content: @Composable () -> Unit) {
    val bg = Color(style.bgColorArgb).copy(alpha = style.opacity)
    Box(modifier = GlanceModifier.fillMaxSize().background(ColorProvider(bg)).padding(6.dp)) {
        content()
    }
}

@Composable
fun WeekdayHeaderRow(weekStartMonday: Boolean, fontScale: Float) {
    val labels = if (weekStartMonday) listOf("월", "화", "수", "목", "금", "토", "일")
    else listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            val isSunday = if (weekStartMonday) index == 6 else index == 0
            val isSaturday = if (weekStartMonday) index == 5 else index == 6
            val color = when {
                isSunday -> Color(0xFFE74C3C)
                isSaturday -> Color(0xFF3498DB)
                else -> Color(0xFFAAAAAA)
            }
            Box(modifier = GlanceModifier.defaultWeight()) {
                Text(label, style = TextStyle(color = ColorProvider(color), fontSize = (10 * fontScale).sp))
            }
        }
    }
}

// 이전/다음 달의 채움 날짜도 실제 LocalDate로 반환한다(null이 아님). 디자인 스펙 4.1: "이전/다음 달의
// 날짜(앞뒤 채움용)는 흐린 회색, 근무 정보 없음" — 화면에는 실제 날짜 숫자를 흐리게 표시해야 하므로
// 호출부에서 어느 달에 속하는지 판단할 수 있도록 null 대신 실제 날짜를 내려준다.
fun buildMonthGrid(month: YearMonth, weekStartMonday: Boolean): List<List<LocalDate>> {
    val firstDay = month.atDay(1)
    val firstDayIndex = if (weekStartMonday) (firstDay.dayOfWeek.value + 6) % 7 else firstDay.dayOfWeek.value % 7
    val totalDays = month.lengthOfMonth()
    val gridStart = firstDay.minusDays(firstDayIndex.toLong())
    val totalCells = firstDayIndex + totalDays
    val paddedSize = ((totalCells + 6) / 7) * 7
    return (0 until paddedSize).map { gridStart.plusDays(it.toLong()) }.chunked(7)
}

@Composable
fun MonthGridView(
    month: YearMonth,
    weeks: List<List<LocalDate>>,
    dayInfoByEpochDay: Map<Long, DayInfo>,
    shiftTypeById: Map<Long, ShiftType>,
    style: WidgetStyle,
    showShiftName: Boolean
) {
    val today = LocalDate.now()
    Column {
        weeks.forEach { week ->
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(modifier = GlanceModifier.defaultWeight()) {
                        val isInCurrentMonth = YearMonth.from(date) == month
                        MonthDayCell(
                            date,
                            dayInfoByEpochDay[date.toEpochDay()],
                            shiftTypeById,
                            style,
                            showShiftName,
                            date == today,
                            isInCurrentMonth
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    date: LocalDate,
    dayInfo: DayInfo?,
    shiftTypeById: Map<Long, ShiftType>,
    style: WidgetStyle,
    showShiftName: Boolean,
    isToday: Boolean,
    isInCurrentMonth: Boolean
) {
    val shiftType = if (isInCurrentMonth) dayInfo?.shiftTypeId?.let { shiftTypeById[it] } else null
    Column(
        modifier = GlanceModifier
            .padding(1.dp)
            .clickable(actionStartActivity<MemoEditActivity>(
                actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to date.toEpochDay())
            ))
    ) {
        Text(
            "${date.dayOfMonth}",
            style = TextStyle(
                fontSize = (10 * style.dateFontScale).sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = ColorProvider(if (isInCurrentMonth) Color(0xFFEEEEEE) else Color(0xFF555555))
            )
        )
        if (isInCurrentMonth) {
            if (showShiftName && shiftType != null) {
                Text(
                    shiftType.name,
                    style = TextStyle(fontSize = (8 * style.shiftFontScale).sp, color = ColorProvider(Color(shiftType.colorArgb)))
                )
            }
            if (shiftType != null) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(2.dp).background(ColorProvider(Color(shiftType.colorArgb)))) {}
            }
            if (dayInfo?.memoText?.isNotBlank() == true) {
                Box(modifier = GlanceModifier.height(3.dp).background(ColorProvider(Color(0xFFF1C40F)))) {}
            }
        }
    }
}
