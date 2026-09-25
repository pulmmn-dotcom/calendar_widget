package com.pulmm.shiftcalendar

import android.app.Application
import com.pulmm.shiftcalendar.data.AppDatabase
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.widget.common.WidgetAlarmScheduler

class ShiftCalendarApp : Application() {
    val repository: ShiftRepository by lazy {
        // Task 19에서 두 번째 인자로 실제 위젯 새로고침 콜백을 연결한다
        ShiftRepository(AppDatabase.getInstance(this))
    }

    override fun onCreate() {
        super.onCreate()
        WidgetAlarmScheduler.scheduleNextMidnight(this)
    }
}
