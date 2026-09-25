package com.pulmm.shiftcalendar.widget.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.ZoneId

object WidgetAlarmScheduler {
    fun scheduleNextMidnight(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextMidnight = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault())
        val triggerAtMillis = nextMidnight.toInstant().toEpochMilli()

        val intent = Intent(context, MidnightRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
}
