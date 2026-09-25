package com.pulmm.shiftcalendar.widget.month

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.currentState
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
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
import com.pulmm.shiftcalendar.R
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.widget.common.MonthGridView
import com.pulmm.shiftcalendar.widget.common.WeekdayHeaderRow
import com.pulmm.shiftcalendar.widget.common.WidgetHeaderButtons
import com.pulmm.shiftcalendar.widget.common.WidgetIconButton
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.WidgetSettingsKeys
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import com.pulmm.shiftcalendar.widget.common.widgetPalette
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class MonthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val initialPrefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val initialMonth = YearMonth.now().plusMonths((initialPrefs[WidgetSettingsKeys.MONTH_OFFSET] ?: 0).toLong())
        // 첫 화면이 비어 보이지 않도록 처음 한 번만 미리 읽어 둔다.
        val initialDayInfos = repository
            .observeDayInfoRange(initialMonth.atDay(1).toEpochDay(), initialMonth.atEndOfMonth().toEpochDay()).first()
        val initialShiftTypes = repository.getShiftTypesOnce()
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)

        provideContent {
            // 위젯 세션이 살아 있는 동안 update()는 provideGlance를 다시 부르지 않고 다시 그리기만 하므로,
            // 달 이동/스타일 변경이 바로 보이도록 상태와 데이터를 컴포지션 안에서 읽는다.
            val prefs = currentState<Preferences>()
            val style = prefs.toWidgetStyle()
            val month = YearMonth.now().plusMonths((prefs[WidgetSettingsKeys.MONTH_OFFSET] ?: 0).toLong())
            val dayInfos by remember(month) {
                repository.observeDayInfoRange(month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay())
            }.collectAsState(if (month == initialMonth) initialDayInfos else emptyList())
            val shiftTypes by remember { repository.observeShiftTypes() }.collectAsState(initialShiftTypes)
            val dayInfoByEpochDay = remember(dayInfos) { dayInfos.associateBy { it.epochDay } }
            val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
            val weeks = remember(month, style.weekStartMonday) { buildMonthGrid(month, style.weekStartMonday) }
            val palette = widgetPalette(style)
            WidgetRoot(style) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WidgetIconButton(
                            iconRes = R.drawable.ic_widget_chevron_left,
                            description = "이전 달",
                            palette = palette,
                            action = actionRunCallback<MonthOffsetAction>(actionParametersOf(MonthOffsetAction.DELTA_KEY to -1)),
                            tonal = false,
                            boxSize = 28.dp,
                            iconSize = 22.dp,
                            iconColor = palette.textSecondary
                        )
                        Text(
                            "${month.year}. ${"%02d".format(month.monthValue)}",
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(palette.textPrimary)
                            )
                        )
                        WidgetIconButton(
                            iconRes = R.drawable.ic_widget_chevron_right,
                            description = "다음 달",
                            palette = palette,
                            action = actionRunCallback<MonthOffsetAction>(actionParametersOf(MonthOffsetAction.DELTA_KEY to 1)),
                            tonal = false,
                            boxSize = 28.dp,
                            iconSize = 22.dp,
                            iconColor = palette.textSecondary
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        WidgetHeaderButtons(palette, appWidgetId, MonthWidgetConfigActivity::class.java)
                    }
                    WeekdayHeaderRow(style.weekStartMonday, style.dateFontScale, palette)
                    MonthGridView(
                        month, weeks, dayInfoByEpochDay, shiftTypeById, style, showShiftName = true,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }
}
