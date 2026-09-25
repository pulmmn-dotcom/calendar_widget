package com.pulmm.shiftcalendar.widget.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MidnightRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WidgetRefresher.refreshAll(context)
            } finally {
                WidgetAlarmScheduler.scheduleNextMidnight(context)
                pendingResult.finish()
            }
        }
    }
}
