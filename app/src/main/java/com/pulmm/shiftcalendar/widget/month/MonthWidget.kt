package com.pulmm.shiftcalendar.widget.month

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.widget.common.MonthGridView
import com.pulmm.shiftcalendar.widget.common.WeekdayHeaderRow
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.WidgetSettingsKeys
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class MonthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val style = prefs.toWidgetStyle()
        val monthOffset = prefs[WidgetSettingsKeys.MONTH_OFFSET] ?: 0
        val month = YearMonth.now().plusMonths(monthOffset.toLong())

        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        val dayInfos = repository.observeDayInfoRange(start, end).first()
        val shiftTypes = repository.getShiftTypesOnce()
        val dayInfoByEpochDay = dayInfos.associateBy { it.epochDay }
        val shiftTypeById = shiftTypes.associateBy { it.id }
        val weeks = buildMonthGrid(month, style.weekStartMonday)

        provideContent {
            WidgetRoot(style) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            "◀",
                            modifier = GlanceModifier.clickable(
                                actionRunCallback<MonthOffsetAction>(actionParametersOf(MonthOffsetAction.DELTA_KEY to -1))
                            ),
                            style = TextStyle(color = ColorProvider(Color(0xFFEEEEEE)))
                        )
                        Text(
                            "${month.year}년 ${month.monthValue}월",
                            modifier = GlanceModifier.defaultWeight(),
                            style = TextStyle(fontSize = (12 * style.dateFontScale).sp, color = ColorProvider(Color(0xFFEEEEEE)))
                        )
                        Text(
                            "▶",
                            modifier = GlanceModifier.clickable(
                                actionRunCallback<MonthOffsetAction>(actionParametersOf(MonthOffsetAction.DELTA_KEY to 1))
                            ),
                            style = TextStyle(color = ColorProvider(Color(0xFFEEEEEE)))
                        )
                    }
                    WeekdayHeaderRow(style.weekStartMonday, style.dateFontScale)
                    MonthGridView(month, weeks, dayInfoByEpochDay, shiftTypeById, style, showShiftName = true)
                }
            }
        }
    }
}
