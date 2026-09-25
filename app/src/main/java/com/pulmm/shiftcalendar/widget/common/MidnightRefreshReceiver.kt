package com.pulmm.shiftcalendar.widget.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MidnightRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                try {
                    WidgetRefresher.refreshAll(context)
                } catch (e: Exception) {
                    Log.e("MidnightRefreshReceiver", "위젯 새로고침 실패", e)
                }
            } finally {
                WidgetAlarmScheduler.scheduleNextMidnight(context)
                pendingResult.finish()
            }
        }
    }
}
