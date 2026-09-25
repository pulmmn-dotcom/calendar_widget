package com.pulmm.shiftcalendar.widget.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulmm.shiftcalendar.R
import com.pulmm.shiftcalendar.logic.KoreanHolidays
import com.pulmm.shiftcalendar.ui.theme.OnSurface
import com.pulmm.shiftcalendar.ui.theme.SurfaceContainerHigh
import com.pulmm.shiftcalendar.widget.common.WidgetFit
import com.pulmm.shiftcalendar.widget.common.WidgetPalette
import com.pulmm.shiftcalendar.widget.common.WidgetStyle
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import com.pulmm.shiftcalendar.widget.common.widgetPalette
import com.pulmm.shiftcalendar.widget.week.weekStartOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/**
 * 실제 위젯(Glance)을 Compose로 흉내 낸 미리보기 4종.
 * 색, 알약/막대/오늘 원/메모 점 모양, 배치, 글자 크기(스타일의 배율)를 실제 위젯과 같게 맞췄다.
 * 눌러도 아무 일도 없고, DB도 읽지 않는다. 그릴 데이터는 [PreviewData]로 받는다.
 */
object PreviewSizes {
    val Day = DpSize(110.dp, 110.dp)
    val Week = DpSize(250.dp, 110.dp)
    val SmallMonth = DpSize(180.dp, 140.dp)
    val Month = DpSize(250.dp, 250.dp)
}

private const val NO_SHIFT_COLOR = 0xFF64748B.toInt()
private val COMPACT_HEIGHT = 125.dp
private const val TIGHT_CELL_DP = 26f
private val WEEKDAY_LABELS = mapOf(
    DayOfWeek.SUNDAY to "일", DayOfWeek.MONDAY to "월", DayOfWeek.TUESDAY to "화",
    DayOfWeek.WEDNESDAY to "수", DayOfWeek.THURSDAY to "목", DayOfWeek.FRIDAY to "금",
    DayOfWeek.SATURDAY to "토"
)

/**
 * 위젯 미리보기를 올려 놓는 배경판. 옅은 푸른 회색 바탕에 점무늬가 있어서,
 * 배경 투명도를 낮춘 위젯이 어떻게 비쳐 보이는지 확인하기 좋다.
 */
@Composable
fun WidgetPreviewBackdrop(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerHigh.copy(alpha = 0.6f))
            .drawBehind {
                val step = 16.dp.toPx()
                val radius = 1.dp.toPx()
                val dot = OnSurface.copy(alpha = 0.10f)
                var y = step / 2
                while (y < size.height) {
                    var x = step / 2
                    while (x < size.width) {
                        drawCircle(dot, radius, Offset(x, y))
                        x += step
                    }
                    y += step
                }
            }
            .padding(padding),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun DayWidgetPreview(
    rawStyle: WidgetStyle,
    data: PreviewData,
    modifier: Modifier = Modifier,
    size: DpSize = PreviewSizes.Day
) {
    val today = data.today
    val day = data.dayAt(today)
    val shift = day?.shift
    val memo = day?.memoText?.takeIf { it.isNotBlank() }
    val compact = size.height < COMPACT_HEIGHT
    val weekday = today.dayOfWeek.getDisplayName(JavaTextStyle.FULL, Locale.KOREAN)
    val lunar = if (rawStyle.showLunar) day?.lunarText else null
    val holiday = KoreanHolidays.nameOf(today)
    val weekdayLine = weekday + (holiday?.let { " · $it" } ?: "") + (lunar?.let { " (음 $it)" } ?: "")
    // 실제 위젯과 같은 규칙으로, 이 크기에 안 들어가는 글자 배율은 줄여서 그린다.
    val style = WidgetFit.day(
        rawStyle, size.width.value, size.height.value, compact, weekdayLine,
        "${today.monthValue}.${today.dayOfMonth}",
        shift?.name ?: "근무 없음",
        if (shift != null) (if (compact) 14f else 18f) else (if (compact) 12f else 14f),
        memo
    )
    val palette = widgetPalette(style)
    val weekdayColor = if (holiday != null) palette.sunday else palette.textSecondary

    PreviewCard(style, size, modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    if (compact) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PText("오늘", 10f * style.dateFontScale, palette.sunday, FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            PText(weekdayLine, 10f * style.dateFontScale, weekdayColor)
                        }
                    } else {
                        PText("오늘", 10f * style.dateFontScale, palette.sunday, FontWeight.Bold)
                    }
                    PText(
                        "${today.monthValue}.${today.dayOfMonth}",
                        28f * style.dateFontScale, palette.textPrimary, FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PreviewIconButton(R.drawable.ic_widget_refresh, palette, 22.dp, 14.dp)
                    Spacer(Modifier.height(4.dp))
                    PreviewIconButton(R.drawable.ic_widget_settings, palette, 22.dp, 14.dp)
                }
            }
            if (!compact) PText(weekdayLine, 11f * style.dateFontScale, weekdayColor)
            Spacer(Modifier.weight(1f))
            if (shift != null) {
                PreviewPill(
                    shift.name, shift.colorArgb, style, palette, solid = true,
                    baseFontSp = if (compact) 14f else 18f, cornerDp = 10f, padH = 10f,
                    padV = if (compact) 1f else 2f
                )
            } else {
                PreviewPill(
                    "근무 없음", NO_SHIFT_COLOR, style, palette, solid = false,
                    baseFontSp = if (compact) 12f else 14f, cornerDp = 10f, padH = 10f,
                    padV = if (compact) 1f else 2f
                )
            }
            Spacer(Modifier.weight(1f))
            if (memo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.buttonBackground)
                        .padding(horizontal = 6.dp, vertical = if (compact) 1.dp else 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_widget_note),
                        contentDescription = null,
                        tint = palette.memoDot,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    PText(
                        memo, 10f * style.memoFontScale, palette.textPrimary,
                        maxLines = if (compact) 1 else 2
                    )
                }
            }
        }
    }
}

@Composable
fun WeekWidgetPreview(
    rawStyle: WidgetStyle,
    data: PreviewData,
    modifier: Modifier = Modifier,
    size: DpSize = PreviewSizes.Week
) {
    val today = data.today
    val weekStart = weekStartOf(today, rawStyle.weekStartMonday)
    val weekDates = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
    val weekOfMonth = remember(today, rawStyle.weekStartMonday) {
        buildMonthGrid(YearMonth.from(today), rawStyle.weekStartMonday).indexOfFirst { today in it } + 1
    }
    val compact = size.height < COMPACT_HEIGHT
    // 실제 위젯과 같은 규칙으로, 이 크기에 안 들어가는 글자 배율은 줄여서 그린다.
    val style = WidgetFit.week(
        rawStyle, size.width.value, size.height.value, compact,
        weekDates.map { date ->
            val info = data.dayAt(date)
            WidgetFit.WeekDay(
                isToday = date == today,
                shiftName = info?.shift?.name,
                shiftDeleted = false,
                memo = info?.memoText?.takeIf { it.isNotBlank() },
                hasLunar = rawStyle.showLunar && info?.lunarText != null
            )
        }
    )
    val palette = widgetPalette(style)
    val rangeText = "${weekDates.first().monthValue}.${weekDates.first().dayOfMonth} ~ " +
        "${weekDates.last().monthValue}.${weekDates.last().dayOfMonth}"

    PreviewCard(style, size, modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = if (compact) 1.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_widget_calendar),
                    contentDescription = null,
                    tint = palette.todayLabel,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                PText(
                    "${today.monthValue}월 ${weekOfMonth}주차 ($rangeText)",
                    13f, palette.textPrimary, FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                PreviewHeaderButtons(
                    palette,
                    boxSize = if (compact) 20.dp else 24.dp,
                    iconSize = if (compact) 13.dp else 15.dp
                )
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                weekDates.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        WeekDayColumn(
                            date = date,
                            isToday = date == today,
                            info = data.dayAt(date),
                            style = style,
                            palette = palette,
                            compact = compact
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekDayColumn(
    date: LocalDate,
    isToday: Boolean,
    info: PreviewDay?,
    style: WidgetStyle,
    palette: WidgetPalette,
    compact: Boolean
) {
    val dow = date.dayOfWeek
    val isHoliday = KoreanHolidays.isHoliday(date)
    val weekdayColor = when {
        isHoliday || dow == DayOfWeek.SUNDAY -> palette.sunday
        dow == DayOfWeek.SATURDAY -> palette.saturday
        else -> palette.textSecondary
    }
    val dateColor = when {
        isHoliday || dow == DayOfWeek.SUNDAY -> palette.sunday
        dow == DayOfWeek.SATURDAY -> palette.saturday
        else -> palette.textPrimary
    }
    val shift = info?.shift
    val memo = info?.memoText?.takeIf { it.isNotBlank() }
    val lunar = if (style.showLunar) info?.lunarText else null

    // 오늘 강조: 파란 테두리(바깥 파란 상자) + 안쪽 연한 파란 채움.
    val outer = if (isToday) {
        Modifier.fillMaxWidth().padding(horizontal = 1.dp)
            .background(palette.todayRing, RoundedCornerShape(10.dp)).padding(1.5.dp)
    } else Modifier.fillMaxWidth().padding(horizontal = 1.dp)
    Box(modifier = outer) {
        val inner = if (isToday) {
            Modifier.fillMaxWidth().background(palette.todayFill, RoundedCornerShape(8.5.dp))
        } else Modifier.fillMaxWidth()
        Box(modifier = inner) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = if (compact) 1.dp else 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PText(
                    if (isToday) "오늘" else (WEEKDAY_LABELS[dow] ?: ""),
                    10f * style.dateFontScale,
                    if (isToday) palette.todayLabel else weekdayColor,
                    if (isToday) FontWeight.Bold else FontWeight.Medium
                )
                PText("${date.dayOfMonth}", 14f * style.dateFontScale, dateColor, FontWeight.Bold)
                Spacer(Modifier.height(if (compact) 1.dp else 2.dp))
                if (shift != null) {
                    PreviewPill(
                        shift.name, shift.colorArgb, style, palette,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        solid = true, baseFontSp = 10f, cornerDp = 5f, padH = 1f, padV = 2f
                    )
                } else {
                    Spacer(Modifier.height(15.dp))
                }
                if (memo != null) {
                    PText(memo.take(3), 9f * style.memoFontScale, palette.memoDot, FontWeight.Bold)
                }
                if (lunar != null) {
                    PText(lunar, 8f, palette.textSecondary)
                }
            }
        }
    }
}

@Composable
fun SmallMonthWidgetPreview(
    rawStyle: WidgetStyle,
    data: PreviewData,
    modifier: Modifier = Modifier,
    size: DpSize = PreviewSizes.SmallMonth
) {
    val month = YearMonth.from(data.today)
    val weeks = remember(month, rawStyle.weekStartMonday) { buildMonthGrid(month, rawStyle.weekStartMonday) }

    // 실제 SmallMonthWidget과 같은 계산: 한 주 줄 높이가 빠듯하면 촘촘한 배치.
    val height = size.height
    val roomyRowHeight = (height - 20.dp - 30.dp - 17.dp) / weeks.size
    val compact = roomyRowHeight < 30.dp
    val rootPadding = if (compact) 6.dp else 10.dp
    val buttonBox = if (compact) 22.dp else 26.dp
    val buttonIcon = if (compact) 14.dp else 16.dp
    val headerHeight = buttonBox + if (compact) 1.dp else 4.dp
    val weekdayHeight = if (compact) 15.dp else 17.dp
    val rowHeight = (height - rootPadding * 2 - headerHeight - weekdayHeight) / weeks.size
    // 실제 위젯과 같은 규칙으로, 이 폭에 안 들어가는 날짜 배율은 줄여서 그린다.
    val style = WidgetFit.smallMonth(rawStyle, size.width.value, rootPadding.value, rowHeight.value)
    val palette = widgetPalette(style)

    PreviewCard(style, size, modifier, padding = rootPadding) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = if (compact) 1.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PText(
                    "${month.year}. ${"%02d".format(month.monthValue)}",
                    if (compact) 14f else 17f, palette.textPrimary, FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                PreviewHeaderButtons(palette, boxSize = buttonBox, iconSize = buttonIcon)
            }
            PreviewWeekdayHeader(
                style.weekStartMonday,
                if (compact) style.dateFontScale * 0.85f else style.dateFontScale,
                palette
            )
            MonthGrid(
                month, weeks, data, style, palette, showShiftName = false,
                modifier = Modifier.weight(1f), cellHeight = rowHeight
            )
        }
    }
}

@Composable
fun MonthWidgetPreview(
    rawStyle: WidgetStyle,
    data: PreviewData,
    modifier: Modifier = Modifier,
    size: DpSize = PreviewSizes.Month
) {
    val month = YearMonth.from(data.today)
    val weeks = remember(month, rawStyle.weekStartMonday) { buildMonthGrid(month, rawStyle.weekStartMonday) }
    // 실제 위젯과 같은 규칙으로, 이 크기에 안 들어가는 글자 배율은 줄여서 그린다.
    val monthShifts = weeks.flatten().filter { YearMonth.from(it) == month }.mapNotNull { date ->
        data.dayAt(date)?.shift?.let { it.name to false }
    }
    val style = WidgetFit.month(
        rawStyle, size.width.value, size.height.value, weeks.size,
        monthShifts.distinct(), monthShifts.isNotEmpty()
    )
    val palette = widgetPalette(style)

    PreviewCard(style, size, modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PreviewIconButton(
                    R.drawable.ic_widget_chevron_left, palette, 28.dp, 22.dp,
                    tonal = false, iconColor = palette.textSecondary
                )
                PText(
                    "${month.year}. ${"%02d".format(month.monthValue)}",
                    17f, palette.textPrimary, FontWeight.Bold
                )
                PreviewIconButton(
                    R.drawable.ic_widget_chevron_right, palette, 28.dp, 22.dp,
                    tonal = false, iconColor = palette.textSecondary
                )
                Spacer(Modifier.weight(1f))
                PreviewHeaderButtons(palette, boxSize = 26.dp, iconSize = 16.dp)
            }
            PreviewWeekdayHeader(style.weekStartMonday, style.dateFontScale, palette)
            MonthGrid(
                month, weeks, data, style, palette, showShiftName = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ---- 공통 조각 ----

/** 위젯 카드: 둥근 20dp, 배경색 * 투명도. */
@Composable
private fun PreviewCard(
    style: WidgetStyle,
    size: DpSize,
    modifier: Modifier,
    padding: Dp = 10.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(style.bgColorArgb).copy(alpha = style.opacity))
            .padding(padding)
    ) {
        content()
    }
}

/** 글자. 실제 위젯의 TextView처럼 줄 높이는 글자 크기의 약 1.2배로 고정한다. */
@Composable
private fun PText(
    text: String,
    sizeSp: Float,
    color: Color,
    weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = sizeSp.sp,
        lineHeight = (sizeSp * 1.2f).sp,
        letterSpacing = 0.sp,
        fontWeight = weight,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        textAlign = textAlign
    )
}

/**
 * 근무 알약 (WidgetShiftPill과 같은 규칙).
 * solid=false: 근무색 15% 배경 + 어둡게(또는 밝게) 만든 근무색 글자. solid=true: 근무색 배경 + 흰 글자.
 */
@Composable
private fun PreviewPill(
    name: String,
    colorArgb: Int,
    style: WidgetStyle,
    palette: WidgetPalette,
    modifier: Modifier = Modifier,
    solid: Boolean = false,
    baseFontSp: Float = 8f,
    cornerDp: Float = 6f,
    padH: Float = 3f,
    padV: Float = 2f
) {
    val shift = Color(colorArgb)
    val bg = if (solid) shift else shift.copy(alpha = 0.15f)
    val fg = if (solid) Color.White else palette.pillText(colorArgb)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerDp.dp))
            .background(bg)
            .padding(horizontal = padH.dp, vertical = padV.dp),
        contentAlignment = Alignment.Center
    ) {
        PText(name, baseFontSp * style.shiftFontScale, fg, FontWeight.Bold)
    }
}

@Composable
private fun PreviewIconButton(
    iconRes: Int,
    palette: WidgetPalette,
    boxSize: Dp,
    iconSize: Dp,
    tonal: Boolean = true,
    iconColor: Color = palette.buttonIcon
) {
    var mod = Modifier.size(boxSize).clip(CircleShape)
    if (tonal) mod = mod.background(palette.buttonBackground)
    Box(modifier = mod, contentAlignment = Alignment.Center) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** 헤더 오른쪽의 새로고침 + 설정 버튼(가로). */
@Composable
private fun PreviewHeaderButtons(palette: WidgetPalette, boxSize: Dp, iconSize: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        PreviewIconButton(R.drawable.ic_widget_refresh, palette, boxSize, iconSize)
        Spacer(Modifier.width(6.dp))
        PreviewIconButton(R.drawable.ic_widget_settings, palette, boxSize, iconSize)
    }
}

@Composable
private fun PreviewWeekdayHeader(weekStartMonday: Boolean, fontScale: Float, palette: WidgetPalette) {
    val labels = if (weekStartMonday) listOf("월", "화", "수", "목", "금", "토", "일")
    else listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        labels.forEachIndexed { index, label ->
            val isSunday = if (weekStartMonday) index == 6 else index == 0
            val isSaturday = if (weekStartMonday) index == 5 else index == 6
            val color = when {
                isSunday -> palette.sunday
                isSaturday -> palette.saturday
                else -> palette.textSecondary
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PText(label, 11f * fontScale, color, FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    weeks: List<List<LocalDate>>,
    data: PreviewData,
    style: WidgetStyle,
    palette: WidgetPalette,
    showShiftName: Boolean,
    modifier: Modifier = Modifier,
    cellHeight: Dp = Dp.Unspecified
) {
    Column(modifier = modifier.fillMaxWidth()) {
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        val inMonth = YearMonth.from(date) == month
                        val info = if (inMonth) data.dayAt(date) else null
                        if (showShiftName) {
                            MonthDayCell(date, info, style, palette, date == data.today, inMonth)
                        } else {
                            SmallMonthDayCell(date, info, style, palette, date == data.today, inMonth, cellHeight)
                        }
                    }
                }
            }
        }
    }
}

private fun dateColorOf(date: LocalDate, isToday: Boolean, inMonth: Boolean, palette: WidgetPalette): Color = when {
    isToday -> palette.todayText
    !inMonth -> palette.textDimmed
    date.dayOfWeek == DayOfWeek.SUNDAY -> palette.sunday
    KoreanHolidays.isHoliday(date) -> palette.sunday
    date.dayOfWeek == DayOfWeek.SATURDAY -> palette.saturday
    else -> palette.textPrimary
}

@Composable
private fun MonthDayCell(
    date: LocalDate,
    info: PreviewDay?,
    style: WidgetStyle,
    palette: WidgetPalette,
    isToday: Boolean,
    inMonth: Boolean
) {
    val shift = info?.shift
    val hasMemo = inMonth && info?.memoText?.isNotBlank() == true
    val circleSize = (20 * style.dateFontScale).dp

    Box(
        modifier = Modifier.fillMaxSize().padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    var numberModifier = Modifier.size(circleSize)
                    if (isToday) numberModifier = numberModifier.clip(CircleShape).background(palette.todayCircle)
                    Box(modifier = numberModifier, contentAlignment = Alignment.Center) {
                        PText(
                            "${date.dayOfMonth}", 12f * style.dateFontScale,
                            dateColorOf(date, isToday, inMonth, palette),
                            if (isToday) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
                if (hasMemo) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 1.dp, end = 2.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Box(Modifier.size(5.dp).clip(CircleShape).background(palette.memoDot))
                    }
                }
            }
            if (inMonth && shift != null) {
                PreviewPill(
                    shift.name, shift.colorArgb, style, palette,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp),
                    baseFontSp = 9f
                )
            }
        }
    }
}

@Composable
private fun SmallMonthDayCell(
    date: LocalDate,
    info: PreviewDay?,
    style: WidgetStyle,
    palette: WidgetPalette,
    isToday: Boolean,
    inMonth: Boolean,
    cellHeight: Dp
) {
    val shift = info?.shift
    val hasMemo = inMonth && info?.memoText?.isNotBlank() == true
    val hasBar = inMonth && shift != null
    val barColor = Color(shift?.colorArgb ?: NO_SHIFT_COLOR)

    val scale = style.dateFontScale
    val heightKnown = cellHeight != Dp.Unspecified
    val tight = heightKnown && cellHeight.value < TIGHT_CELL_DP
    val barHeight = if (tight) 2.dp else 3.dp
    var circle = 20f * scale
    if (heightKnown) {
        val room = if (tight) cellHeight.value - 2f else cellHeight.value - 2f - barHeight.value - 1f
        circle = minOf(circle, maxOf(room, 12f))
    }
    val circleSize = circle.dp
    val fontSp = minOf(12f * scale, circle * 0.68f)
    val barWidth = (circle * 0.85f).dp

    val number: @Composable () -> Unit = {
        var numberModifier = Modifier.size(circleSize)
        if (isToday) numberModifier = numberModifier.clip(CircleShape).background(palette.todayCircle)
        Box(modifier = Modifier.width(circleSize + 10.dp).height(circleSize)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(modifier = numberModifier, contentAlignment = Alignment.Center) {
                    PText(
                        "${date.dayOfMonth}", fontSp,
                        dateColorOf(date, isToday, inMonth, palette),
                        if (isToday) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
            if (hasMemo) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(palette.memoDot))
                }
            }
        }
    }
    val bar: @Composable () -> Unit = {
        Box(
            Modifier.width(barWidth).height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2)).background(barColor)
        )
    }

    Box(modifier = Modifier.fillMaxSize().padding(1.dp), contentAlignment = Alignment.Center) {
        if (tight) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { number() }
            if (hasBar) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) { bar() }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                number()
                if (hasBar) {
                    Spacer(Modifier.height(1.dp))
                    bar()
                }
            }
        }
    }
}
