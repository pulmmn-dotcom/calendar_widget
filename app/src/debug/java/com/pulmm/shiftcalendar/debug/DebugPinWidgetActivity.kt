package com.pulmm.shiftcalendar.debug

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log

/** 디버그 빌드 전용: 홈화면에 위젯 추가 요청. adb shell am start -n com.pulmm.shiftcalendar/.debug.DebugPinWidgetActivity --es type month */
class DebugPinWidgetActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val receiver = when (intent.getStringExtra("type")) {
            "day" -> "com.pulmm.shiftcalendar.widget.day.DayWidgetReceiver"
            "week" -> "com.pulmm.shiftcalendar.widget.week.WeekWidgetReceiver"
            "small" -> "com.pulmm.shiftcalendar.widget.monthsmall.SmallMonthWidgetReceiver"
            else -> "com.pulmm.shiftcalendar.widget.month.MonthWidgetReceiver"
        }
        val manager = AppWidgetManager.getInstance(this)
        val ok = manager.isRequestPinAppWidgetSupported &&
            manager.requestPinAppWidget(ComponentName(packageName, receiver), null, null)
        Log.i("DebugPin", "requestPin=$ok type=${intent.getStringExtra("type")}")
        finish()
    }
}
