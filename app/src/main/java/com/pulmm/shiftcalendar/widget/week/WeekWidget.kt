package com.pulmm.shiftcalendar.widget.week

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
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
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.R
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.logic.LunarConverter
import com.pulmm.shiftcalendar.logic.LunarDate
import com.pulmm.shiftcalendar.widget.common.MemoEditActivity
import com.pulmm.shiftcalendar.widget.common.WidgetHeaderButtons
import com.pulmm.shiftcalendar.widget.common.WidgetPalette
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.WidgetShiftPill
import com.pulmm.shiftcalendar.widget.common.WidgetStyle
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import com.pulmm.shiftcalendar.widget.common.widgetPalette
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private const val DELETED_SHIFT_COLOR = 0xFF94A3B8.toInt()
private val WEEKDAY_LABELS = mapOf(
    DayOfWeek.SUNDAY to "일", DayOfWeek.MONDAY to "월", DayOfWeek.TUESDAY to "화",
    DayOfWeek.WEDNESDAY to "수", DayOfWeek.THURSDAY to "목", DayOfWeek.FRIDAY to "금",
    DayOfWeek.SATURDAY to "토"
)

internal fun weekStartOf(today: LocalDate, weekStartMonday: Boolean): LocalDate =
    if (weekStartMonday) today.minusDays(((today.dayOfWeek.value + 6) % 7).toLong())
    else today.minusDays((today.dayOfWeek.value % 7).toLong())

/** 위젯 안의 작은 음력 표기: 음력 9월 2일 -> "9.2" (윤달은 "윤9.2"). */
private fun LunarDate.compact(): String = "${if (isLeapMonth) "윤" else ""}$month.$day"

class WeekWidget : GlanceAppWidget() {
    // 실제 위젯 크기를 알아야 낮은 칸(높이 110dp 안팎)에서 촘촘한 배치로 바꿀 수 있다.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val initialPrefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val initialToday = LocalDate.now()
        val initialWeekStart = weekStartOf(initialToday, initialPrefs.toWidgetStyle().weekStartMonday)
        // 첫 화면이 비어 보이지 않도록 처음 한 번만 미리 읽어 둔다.
        val initialDayInfos = repository
            .observeDayInfoRange(initialWeekStart.toEpochDay(), initialWeekStart.plusDays(6).toEpochDay()).first()
        val initialShiftTypes = repository.getShiftTypesOnce()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        provideContent {
            // 위젯 세션이 살아 있는 동안 update()는 provideGlance를 다시 부르지 않고 다시 그리기만 하므로,
            // 메모/근무/스타일 변경이 바로 보이도록 상태와 데이터를 컴포지션 안에서 읽는다.
            val style = currentState<Preferences>().toWidgetStyle()
            val today = LocalDate.now()
            val weekStart = weekStartOf(today, style.weekStartMonday)
            val weekDates = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
            val dayInfos by remember(weekStart) {
                repository.observeDayInfoRange(weekStart.toEpochDay(), weekStart.plusDays(6).toEpochDay())
            }.collectAsState(if (weekStart == initialWeekStart) initialDayInfos else emptyList())
            val shiftTypes by remember { repository.observeShiftTypes() }.collectAsState(initialShiftTypes)
            val dayInfoByEpochDay = remember(dayInfos) { dayInfos.associateBy { it.epochDay } }
            val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
            // LunarConverter는 동기화된 싱글턴이라 순서대로 하나씩 부른다.
            val lunarByEpochDay = remember(weekDates, style.showLunar) {
                if (style.showLunar) {
                    weekDates.mapNotNull { d -> LunarConverter.toLunar(d)?.let { d.toEpochDay() to it.compact() } }.toMap()
                } else emptyMap()
            }
            val weekOfMonth = remember(today, style.weekStartMonday) {
                buildMonthGrid(YearMonth.from(today), style.weekStartMonday)
                    .indexOfFirst { week -> today in week } + 1
            }
            val palette = widgetPalette(style)
            val compact = LocalSize.current.height < 125.dp
            val rangeText = "${weekDates.first().monthValue}.${weekDates.first().dayOfMonth} ~ " +
                "${weekDates.last().monthValue}.${weekDates.last().dayOfMonth}"

            WidgetRoot(style) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(bottom = if (compact) 1.dp else 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_calendar),
                            contentDescription = null,
                            modifier = GlanceModifier.size(16.dp),
                            colorFilter = ColorFilter.tint(ColorProvider(palette.todayLabel))
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            "${today.monthValue}월 ${weekOfMonth}주차 ($rangeText)",
                            maxLines = 1,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(palette.textPrimary)
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        WidgetHeaderButtons(
                            palette, appWidgetId, WeekWidgetConfigActivity::class.java,
                            boxSize = if (compact) 20.dp else 24.dp, iconSize = if (compact) 13.dp else 15.dp
                        )
                    }
                    Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                        weekDates.forEach { date ->
                            val dayInfo = dayInfoByEpochDay[date.toEpochDay()]
                            val shiftTypeId = dayInfo?.shiftTypeId
                            val shiftType = shiftTypeId?.let { shiftTypeById[it] }
                            Box(modifier = GlanceModifier.defaultWeight()) {
                                WeekDayColumn(
                                    date = date,
                                    isToday = date == today,
                                    shiftName = shiftType?.name ?: if (shiftTypeId != null) "삭제됨" else null,
                                    shiftColorArgb = shiftType?.colorArgb ?: DELETED_SHIFT_COLOR,
                                    isDeletedShift = shiftTypeId != null && shiftType == null,
                                    memo = dayInfo?.memoText?.takeIf { it.isNotBlank() },
                                    lunarText = lunarByEpochDay[date.toEpochDay()],
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
    }
}

@androidx.compose.runtime.Composable
private fun WeekDayColumn(
    date: LocalDate,
    isToday: Boolean,
    shiftName: String?,
    shiftColorArgb: Int,
    isDeletedShift: Boolean,
    memo: String?,
    lunarText: String?,
    style: WidgetStyle,
    palette: WidgetPalette,
    compact: Boolean
) {
    val dow = date.dayOfWeek
    val weekdayColor = when (dow) {
        DayOfWeek.SUNDAY -> palette.sunday
        DayOfWeek.SATURDAY -> palette.saturday
        else -> palette.textSecondary
    }
    val dateColor = when (dow) {
        DayOfWeek.SUNDAY -> palette.sunday
        DayOfWeek.SATURDAY -> palette.saturday
        else -> palette.textPrimary
    }
    val click = GlanceModifier.clickable(
        actionStartActivity<MemoEditActivity>(
            actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to date.toEpochDay())
        )
    )
    // 오늘 강조: 파란 테두리(바깥 파란 상자) + 안쪽 연한 파란 채움. Glance에는 테두리가 없어 상자 두 겹으로 만든다.
    val ringModifier = if (isToday) {
        GlanceModifier.fillMaxWidth().padding(horizontal = 1.dp)
            .background(ColorProvider(palette.todayRing)).cornerRadius(10.dp).padding(1.5.dp)
    } else GlanceModifier.fillMaxWidth().padding(horizontal = 1.dp)
    Box(modifier = ringModifier) {
        val innerModifier = if (isToday) {
            GlanceModifier.fillMaxWidth().background(ColorProvider(palette.todayFill)).cornerRadius(8.5.dp)
        } else GlanceModifier.fillMaxWidth()
        Box(modifier = innerModifier.then(click)) {
            Column(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = if (compact) 1.dp else 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (isToday) "오늘" else (WEEKDAY_LABELS[dow] ?: ""),
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = (10 * style.dateFontScale).sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = ColorProvider(if (isToday) palette.todayLabel else weekdayColor)
                    )
                )
                Text(
                    "${date.dayOfMonth}",
                    maxLines = 1,
                    style = TextStyle(
                        fontSize = (14 * style.dateFontScale).sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(dateColor)
                    )
                )
                Spacer(modifier = GlanceModifier.height(if (compact) 1.dp else 2.dp))
                if (shiftName != null) {
                    WidgetShiftPill(
                        name = shiftName,
                        colorArgb = shiftColorArgb,
                        style = style,
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 2.dp),
                        solid = true,
                        baseFontSp = if (isDeletedShift) 8f else 10f,
                        cornerDp = 5f,
                        padHorizontalDp = 1f,
                        padVerticalDp = 2f
                    )
                } else {
                    Spacer(modifier = GlanceModifier.height(15.dp))
                }
                if (memo != null) {
                    Text(
                        memo.take(3),
                        maxLines = 1,
                        style = TextStyle(
                            fontSize = (9 * style.memoFontScale).sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(palette.memoDot)
                        )
                    )
                }
                if (lunarText != null) {
                    Text(
                        lunarText,
                        maxLines = 1,
                        style = TextStyle(fontSize = 8.sp, color = ColorProvider(palette.textSecondary))
                    )
                }
            }
        }
    }
}
