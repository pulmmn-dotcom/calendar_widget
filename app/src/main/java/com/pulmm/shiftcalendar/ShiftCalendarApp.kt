package com.pulmm.shiftcalendar

import android.app.Application
import com.pulmm.shiftcalendar.data.AppDatabase
import com.pulmm.shiftcalendar.data.ShiftRepository

class ShiftCalendarApp : Application() {
    val repository: ShiftRepository by lazy {
        ShiftRepository(AppDatabase.getInstance(this))
    }
}
