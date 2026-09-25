package com.pulmm.shiftcalendar.widget.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import com.pulmm.shiftcalendar.logic.KoreanHolidays
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun WidgetRoot(style: WidgetStyle, padding: Dp = 10.dp, content: @Composable () -> Unit) {
    val bg = Color(style.bgColorArgb).copy(alpha = style.opacity)
    // cornerRadius는 API 31+에서만 적용된다(그 미만은 각진 카드 그대로).
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(bg))
            .cornerRadius(20.dp)
            .padding(padding)
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

/** 달력 칸 전체(앞뒤 채움 날짜 포함)를 덮는 기간. 채움은 한쪽에 최대 6일이라 7일씩 넓혀 읽는다. */
fun monthGridRange(month: YearMonth): Pair<Long, Long> =
    month.atDay(1).minusDays(7).toEpochDay() to month.atEndOfMonth().plusDays(7).toEpochDay()

// 이전/다음 달의 채움 날짜도 실제 LocalDate로 반환한다(null이 아님). 채움 날짜는 숫자만 흐리게 하고
// 근무·메모는 이번 달과 똑같이 보여주므로, 호출부에서 어느 달에 속하는지 판단할 수 있도록 실제 날짜를 내려준다.
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
    modifier: GlanceModifier = GlanceModifier,
    // 작은 위젯처럼 높이가 빠듯할 때만 넘긴다(한 주 줄의 높이). 넘기면 칸 안 요소가 그 높이에 맞춰 줄어든다.
    cellHeight: Dp = Dp.Unspecified
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
                            isInCurrentMonth,
                            cellHeight
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
    isInCurrentMonth: Boolean,
    cellHeight: Dp = Dp.Unspecified
) {
    if (!showShiftName) {
        SmallMonthDayCell(date, dayInfo, shiftTypeById, style, palette, isToday, isInCurrentMonth, cellHeight)
        return
    }
    val shiftTypeId = dayInfo?.shiftTypeId
    val shiftType = shiftTypeId?.let { shiftTypeById[it] }
    // 근무 종류가 지정돼 있는데 목록에 없으면 "삭제된 근무".
    val isDeletedShift = shiftTypeId != null && shiftType == null
    val hasMemo = dayInfo?.memoText?.isNotBlank() == true

    val dateColor = when {
        isToday -> palette.todayText
        !isInCurrentMonth -> palette.textDimmed
        date.dayOfWeek == DayOfWeek.SUNDAY -> palette.sunday
        KoreanHolidays.isHoliday(date) -> palette.sunday
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
            if (shiftType != null || isDeletedShift) {
                WidgetShiftPill(
                    name = shiftType?.name ?: "삭제됨",
                    colorArgb = shiftType?.colorArgb ?: DELETED_SHIFT_COLOR,
                    style = style,
                    modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 1.dp),
                    baseFontSp = if (isDeletedShift) 8f else 9f
                )
            }
        }
    }
}

// 이 높이(dp)보다 칸이 낮으면 색 막대를 숫자 아래에 쌓지 않고 칸 맨 아래에 겹쳐 그린다.
private const val TIGHT_CELL_DP = 26f

/**
 * 작은 한달 위젯의 날짜 칸: 날짜 숫자(오늘은 파란 원) + 아래의 가는 근무색 막대(이름 없음).
 * 삭제된 근무는 회색 막대, 근무가 없으면 막대 없음, 이전/다음 달 채움 날짜는 흐린 숫자만.
 * cellHeight를 알면 그 높이에 맞춰 숫자 원/글자/막대를 줄여 잘리지 않게 한다.
 */
@Composable
private fun SmallMonthDayCell(
    date: LocalDate,
    dayInfo: DayInfo?,
    shiftTypeById: Map<Long, ShiftType>,
    style: WidgetStyle,
    palette: WidgetPalette,
    isToday: Boolean,
    isInCurrentMonth: Boolean,
    cellHeight: Dp
) {
    val shiftTypeId = dayInfo?.shiftTypeId
    val shiftType = shiftTypeId?.let { shiftTypeById[it] }
    // 근무 종류가 지정돼 있는데 목록에 없으면 "삭제된 근무".
    val isDeletedShift = shiftTypeId != null && shiftType == null
    val hasMemo = dayInfo?.memoText?.isNotBlank() == true
    val hasBar = shiftType != null || isDeletedShift
    val barColor = Color(shiftType?.colorArgb ?: DELETED_SHIFT_COLOR)

    val dateColor = when {
        isToday -> palette.todayText
        !isInCurrentMonth -> palette.textDimmed
        date.dayOfWeek == DayOfWeek.SUNDAY -> palette.sunday
        KoreanHolidays.isHoliday(date) -> palette.sunday
        date.dayOfWeek == DayOfWeek.SATURDAY -> palette.saturday
        else -> palette.textPrimary
    }

    val scale = style.dateFontScale
    val heightKnown = cellHeight != Dp.Unspecified
    val tight = heightKnown && cellHeight.value < TIGHT_CELL_DP
    val barHeight = if (tight) 2.dp else 3.dp
    var circle = 20f * scale
    if (heightKnown) {
        // 칸 안쪽 여백(위아래 1dp씩)과 막대 자리를 뺀 나머지가 숫자 원이 쓸 수 있는 높이.
        val room = if (tight) cellHeight.value - 2f else cellHeight.value - 2f - barHeight.value - 1f
        circle = minOf(circle, maxOf(room, 12f))
    }
    val circleSize = circle.dp
    val fontSp = minOf(12f * scale, circle * 0.68f)
    val barWidth = (circle * 0.85f).dp

    // 숫자 원 + 오른쪽 위에 겹친 메모 점(칸이 넓어도 점이 숫자 바로 옆에 붙도록 숫자 폭 + 여유만큼의 상자 안에 둔다).
    val number: @Composable () -> Unit = {
        var numberModifier = GlanceModifier.size(circleSize)
        if (isToday) {
            numberModifier = numberModifier
                .background(ColorProvider(palette.todayCircle))
                .cornerRadius(circleSize / 2)
        }
        Box(modifier = GlanceModifier.width(circleSize + 10.dp).height(circleSize)) {
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(modifier = numberModifier, contentAlignment = Alignment.Center) {
                    Text(
                        "${date.dayOfMonth}",
                        style = TextStyle(
                            fontSize = fontSp.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = ColorProvider(dateColor)
                        )
                    )
                }
            }
            if (hasMemo) {
                // 메모 표시: 숫자 오른쪽 위의 작은 호박색 점
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = GlanceModifier
                            .size(5.dp)
                            .background(ColorProvider(palette.memoDot))
                            .cornerRadius(2.5.dp)
                    ) {}
                }
            }
        }
    }
    val bar: @Composable () -> Unit = {
        Box(
            modifier = GlanceModifier
                .width(barWidth)
                .height(barHeight)
                .background(ColorProvider(barColor))
                .cornerRadius(barHeight / 2)
        ) {}
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(1.dp)
            .clickable(actionStartActivity<MemoEditActivity>(
                actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to date.toEpochDay())
            )),
        contentAlignment = Alignment.Center
    ) {
        if (tight) {
            // 빠듯한 높이: 숫자는 가운데, 막대는 칸 맨 아래에 겹쳐 그린다.
            Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) { number() }
            if (hasBar) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { bar() }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = GlanceModifier.fillMaxWidth()) {
                number()
                if (hasBar) {
                    Spacer(modifier = GlanceModifier.height(1.dp))
                    bar()
                }
            }
        }
    }
}
