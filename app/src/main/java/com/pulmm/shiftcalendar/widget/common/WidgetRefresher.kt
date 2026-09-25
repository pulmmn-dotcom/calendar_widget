package com.pulmm.shiftcalendar.widget.common

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.pulmm.shiftcalendar.widget.day.DayWidget
import com.pulmm.shiftcalendar.widget.month.MonthWidget
import com.pulmm.shiftcalendar.widget.monthsmall.SmallMonthWidget
import com.pulmm.shiftcalendar.widget.week.WeekWidget

object WidgetRefresher {
    suspend fun refreshAll(context: Context) {
        refreshOne(context, "DayWidget") { DayWidget().updateAll(context) }
        refreshOne(context, "WeekWidget") { WeekWidget().updateAll(context) }
        refreshOne(context, "SmallMonthWidget") { SmallMonthWidget().updateAll(context) }
        refreshOne(context, "MonthWidget") { MonthWidget().updateAll(context) }
    }

    private suspend fun refreshOne(context: Context, name: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            Log.e("WidgetRefresher", "$name 새로고침 실패", e)
        }
    }
}
