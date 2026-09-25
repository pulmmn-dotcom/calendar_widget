package com.pulmm.shiftcalendar.widget.monthsmall

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.widget.common.MonthGridView
import com.pulmm.shiftcalendar.widget.common.WeekdayHeaderRow
import com.pulmm.shiftcalendar.widget.common.WidgetFit
import com.pulmm.shiftcalendar.widget.common.WidgetHeaderButtons
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import com.pulmm.shiftcalendar.widget.common.monthGridRange
import com.pulmm.shiftcalendar.widget.common.openCalendarAction
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import com.pulmm.shiftcalendar.widget.common.widgetPalette
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class SmallMonthWidget : GlanceAppWidget() {
    // 실제 위젯 높이를 알아야 6주짜리 달에서도 잘리지 않게 촘촘한 배치로 바꿀 수 있다.
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val initialMonth = YearMonth.now()
        // 첫 화면이 비어 보이지 않도록 처음 한 번만 미리 읽어 둔다.
        val initialDayInfos = repository
            .observeDayInfoRange(monthGridRange(initialMonth).first, monthGridRange(initialMonth).second).first()
        val initialShiftTypes = repository.getShiftTypesOnce()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        provideContent {
            // 위젯 세션이 살아 있는 동안 update()는 provideGlance를 다시 부르지 않고 다시 그리기만 하므로,
            // 메모/근무/스타일 변경이 바로 보이도록 상태와 데이터를 컴포지션 안에서 읽는다.
            val rawStyle = currentState<Preferences>().toWidgetStyle()
            val month = YearMonth.now()
            val dayInfos by remember(month) {
                repository.observeDayInfoRange(monthGridRange(month).first, monthGridRange(month).second)
            }.collectAsState(if (month == initialMonth) initialDayInfos else emptyList())
            val shiftTypes by remember { repository.observeShiftTypes() }.collectAsState(initialShiftTypes)
            val dayInfoByEpochDay = remember(dayInfos) { dayInfos.associateBy { it.epochDay } }
            val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
            val weeks = remember(month, rawStyle.weekStartMonday) { buildMonthGrid(month, rawStyle.weekStartMonday) }

            // 한 주 줄에 쓸 수 있는 높이를 계산해, 넉넉하면 기본 배치, 빠듯하면 촘촘한 배치(작은 머리줄/여백)를 쓴다.
            val size = LocalSize.current
            val height = size.height
            val roomyRowHeight = (height - 20.dp - ROOMY_HEADER - ROOMY_WEEKDAY) / weeks.size
            val compact = roomyRowHeight < ROOMY_ROW_MIN
            val rootPadding = if (compact) 6.dp else 10.dp
            val buttonBox = if (compact) 22.dp else 26.dp
            val buttonIcon = if (compact) 14.dp else 16.dp
            val headerHeight = buttonBox + if (compact) 1.dp else 4.dp
            val weekdayHeight = if (compact) 15.dp else ROOMY_WEEKDAY
            val rowHeight = (height - rootPadding * 2 - headerHeight - weekdayHeight) / weeks.size
            // 위젯이 좁은데 날짜 배율이 크면 숫자 원이 칸보다 커지므로, 이 폭에 들어가는 배율로 줄여서 그린다.
            val style = WidgetFit.smallMonth(rawStyle, size.width.value, rootPadding.value, rowHeight.value)
            val palette = widgetPalette(style)

            WidgetRoot(style, padding = rootPadding) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(bottom = if (compact) 1.dp else 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${month.year}. ${"%02d".format(month.monthValue)}",
                            maxLines = 1,
                            modifier = GlanceModifier.clickable(openCalendarAction(month)),
                            style = TextStyle(
                                fontSize = if (compact) 14.sp else 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(palette.textPrimary)
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        WidgetHeaderButtons(
                            palette, appWidgetId, SmallMonthWidgetConfigActivity::class.java,
                            boxSize = buttonBox, iconSize = buttonIcon
                        )
                    }
                    WeekdayHeaderRow(
                        style.weekStartMonday,
                        if (compact) style.dateFontScale * 0.85f else style.dateFontScale,
                        palette
                    )
                    MonthGridView(
                        month, weeks, dayInfoByEpochDay, shiftTypeById, style, showShiftName = false,
                        modifier = GlanceModifier.defaultWeight(),
                        cellHeight = rowHeight
                    )
                }
            }
        }
    }
}

// 기본 배치의 머리줄(버튼 26 + 여백 4), 요일줄 높이, 그리고 이보다 한 주 줄이 낮으면 촘촘한 배치로 바꾸는 기준.
private val ROOMY_HEADER = 30.dp
private val ROOMY_WEEKDAY = 17.dp
private val ROOMY_ROW_MIN = 30.dp
