package com.pulmm.shiftcalendar.logic

import com.github.usingsky.calendar.KoreanLunarCalendar
import java.time.LocalDate

data class LunarDate(val year: Int, val month: Int, val day: Int, val isLeapMonth: Boolean) {
    fun toShortDisplay(): String {
        val leapMark = if (isLeapMonth) "윤" else ""
        return "음 $leapMark$month.$day"
    }
}

object LunarConverter {
    @Synchronized
    fun toLunar(date: LocalDate): LunarDate? {
        val calendar = KoreanLunarCalendar.getInstance()
        val ok = calendar.setSolarDate(date.year, date.monthValue, date.dayOfMonth)
        if (!ok) return null
        return LunarDate(
            year = calendar.lunarYear,
            month = calendar.lunarMonth,
            day = calendar.lunarDay,
            isLeapMonth = calendar.isIntercalation
        )
    }
}
