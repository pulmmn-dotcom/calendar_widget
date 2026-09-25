package com.pulmm.shiftcalendar.widget.monthsmall

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.widget.common.MonthGridView
import com.pulmm.shiftcalendar.widget.common.WeekdayHeaderRow
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import com.pulmm.shiftcalendar.widget.common.widgetPalette
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class SmallMonthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val style = getAppWidgetState(context, PreferencesGlanceStateDefinition, id).toWidgetStyle()

        val month = YearMonth.now()
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
                    WeekdayHeaderRow(style.weekStartMonday, style.dateFontScale, widgetPalette(style))
                    MonthGridView(
                        month, weeks, dayInfoByEpochDay, shiftTypeById, style, showShiftName = false,
                        modifier = GlanceModifier.defaultWeight()
                    )
                }
            }
        }
    }
}
