package com.pulmm.shiftcalendar

import android.app.Application
import com.pulmm.shiftcalendar.data.AppDatabase
import com.pulmm.shiftcalendar.data.DataChangeListener
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.widget.common.WidgetAlarmScheduler
import com.pulmm.shiftcalendar.widget.common.WidgetRefresher

class ShiftCalendarApp : Application() {
    val repository: ShiftRepository by lazy {
        ShiftRepository(
            db = AppDatabase.getInstance(this),
            onDataChanged = DataChangeListener { WidgetRefresher.refreshAll(this) }
        )
    }

    override fun onCreate() {
        super.onCreate()
        WidgetAlarmScheduler.scheduleNextMidnight(this)
    }
}
