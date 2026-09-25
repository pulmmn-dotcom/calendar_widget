package com.pulmm.shiftcalendar.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.ui.theme.ShiftPalette
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 디버그 빌드 전용: 화면 확인용 샘플 데이터를 넣는다. adb shell am broadcast -a com.pulmm.shiftcalendar.DEBUG_SEED */
class DebugSeedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (repository.getShiftTypesOnce().isEmpty()) {
                    val names = listOf("주간", "오후", "야간", "휴무")
                    val colors = listOf(0, 1, 2, 3).map { ShiftPalette.colors[it] }
                    val ids = names.indices.map { repository.addShiftType(names[it], colors[it], it) }
                    val today = LocalDate.now()
                    val start = today.withDayOfMonth(1).minusDays(4)
                    repository.savePattern("4조 2교대", start.toEpochDay(), listOf(ids[0], ids[0], ids[2], ids[2], ids[3], ids[3]))
                    repository.setMemo(today.toEpochDay(), "정기검진")
                    repository.setMemo(today.plusDays(3).toEpochDay(), "회식")
                    repository.setOverride(today.plusDays(5).toEpochDay(), ids[1])
                }
            } finally {
                pending.finish()
            }
        }
    }
}
