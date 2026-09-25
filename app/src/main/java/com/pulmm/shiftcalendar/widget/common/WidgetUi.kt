package com.pulmm.shiftcalendar.widget.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun WidgetRoot(style: WidgetStyle, content: @Composable () -> Unit) {
    val bg = Color(style.bgColorArgb).copy(alpha = style.opacity)
    // cornerRadius는 API 31+에서만 적용된다(그 미만은 각진 카드 그대로).
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bg))
            .cornerRadius(20.dp)
            .padding(10.dp)
    ) {
        content()
    }
}

@Composable
fun WeekdayHeaderRow(weekStartMonday: Boolean, fontScale: Float, palette: WidgetPalette) {
    val labels = if (weekStartMonday) listOf("월", "화", "수", "목", "금", "토", "일")
    else listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp)) {
        labels.forEachIndexed { index, label ->
            val isSunday = if (weekStartMonday) index == 6 else index == 0
            val isSaturday = if (weekStartMonday) index == 5 else index == 6
            val color = when {
                isSunday -> palette.sunday
                isSaturday -> palette.saturday
                else -> palette.textSecondary
            }
            Box(modifier = GlanceModifier.defaultWeight(), contentAlignment = Alignment.Center) {
                Text(
                    label,
                    style = TextStyle(
                        color = ColorProvider(color),
                        fontSize = (11 * fontScale).sp,
                        fontWeight = FontWeight.Medium
                    )
                )
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
    showShiftName: Boolean,
    modifier: GlanceModifier = GlanceModifier
) {
    val today = LocalDate.now()
    val palette = widgetPalette(style)
    Column(modifier = modifier.fillMaxWidth()) {
        weeks.forEach { week ->
            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                week.forEach { date ->
                    Box(
                        modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val isInCurrentMonth = YearMonth.from(date) == month
                        MonthDayCell(
                            date,
                            dayInfoByEpochDay[date.toEpochDay()],
                            shiftTypeById,
                            style,
                            palette,
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

private val DELETED_SHIFT_COLOR = 0xFF94A3B8.toInt()

@Composable
private fun MonthDayCell(
    date: LocalDate,
    dayInfo: DayInfo?,
    shiftTypeById: Map<Long, ShiftType>,
    style: WidgetStyle,
    palette: WidgetPalette,
    showShiftName: Boolean,
    isToday: Boolean,
    isInCurrentMonth: Boolean
) {
    val shiftTypeId = if (isInCurrentMonth) dayInfo?.shiftTypeId else null
    val shiftType = shiftTypeId?.let { shiftTypeById[it] }
    // 근무 종류가 지정돼 있는데 목록에 없으면 "삭제된 근무".
    val isDeletedShift = shiftTypeId != null && shiftType == null
    val hasMemo = isInCurrentMonth && dayInfo?.memoText?.isNotBlank() == true

    val dateColor = when {
        isToday -> palette.todayText
        !isInCurrentMonth -> palette.textDimmed
        date.dayOfWeek == DayOfWeek.SUNDAY -> palette.sunday
        date.dayOfWeek == DayOfWeek.SATURDAY -> palette.saturday
        else -> palette.textPrimary
    }
    val circleSize = (20 * style.dateFontScale).dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(1.dp)
            .clickable(actionStartActivity<MemoEditActivity>(
                actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to date.toEpochDay())
            )),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = GlanceModifier.fillMaxWidth()) {
            var numberModifier = GlanceModifier.size(circleSize)
            if (isToday) {
                numberModifier = numberModifier
                    .background(ColorProvider(palette.todayCircle))
                    .cornerRadius(circleSize / 2)
            }
            Box(modifier = GlanceModifier.fillMaxWidth()) {
                Box(modifier = GlanceModifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = numberModifier, contentAlignment = Alignment.Center) {
                        Text(
                            "${date.dayOfMonth}",
                            style = TextStyle(
                                fontSize = (12 * style.dateFontScale).sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                color = ColorProvider(dateColor)
                            )
                        )
                    }
                }
                if (hasMemo) {
                    // 메모 표시: 날짜 줄 오른쪽 위의 작은 호박색 점
                    Box(
                        modifier = GlanceModifier.fillMaxWidth().padding(top = 1.dp, end = 2.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(5.dp)
                                .background(ColorProvider(palette.memoDot))
                                .cornerRadius(2.5.dp)
                        ) {}
                    }
                }
            }
            if (isInCurrentMonth && (shiftType != null || isDeletedShift)) {
                val pillColor = shiftType?.colorArgb ?: DELETED_SHIFT_COLOR
                if (showShiftName) {
                    WidgetShiftPill(
                        name = shiftType?.name ?: "삭제됨",
                        colorArgb = pillColor,
                        style = style,
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 1.dp),
                        baseFontSp = if (isDeletedShift) 8f else 9f
                    )
                } else {
                    // 작은한달 위젯(D9에서 다시 디자인): 이름 없이 가는 색 막대만 보여준다.
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(horizontal = 3.dp)
                            .height(3.dp)
                            .background(ColorProvider(Color(pillColor)))
                            .cornerRadius(1.5.dp)
                    ) {}
                }
            }
        }
    }
}
