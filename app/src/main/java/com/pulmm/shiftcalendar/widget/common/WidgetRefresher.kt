package com.pulmm.shiftcalendar.widget.common

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.pulmm.shiftcalendar.widget.day.DayWidget
import com.pulmm.shiftcalendar.widget.month.MonthWidget
import com.pulmm.shiftcalendar.widget.monthsmall.SmallMonthWidget
import com.pulmm.shiftcalendar.widget.week.WeekWidget

object WidgetRefresher {
    suspend fun refreshAll(context: Context) {
        DayWidget().updateAll(context)
        WeekWidget().updateAll(context)
        SmallMonthWidget().updateAll(context)
        MonthWidget().updateAll(context)
    }
}
