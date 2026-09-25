package com.pulmm.shiftcalendar.logic

import com.github.usingsky.calendar.KoreanLunarCalendar
import java.time.LocalDate

data class LunarDate(val year: Int, val month: Int, val day: Int, val isLeapMonth: Boolean) {
    fun toShortDisplay(): String {
        val leapMark = if (isLeapMonth) "윤" else ""
        return "음 $leapMark$month.$day"
    }
}

/**
 * 이 오브젝트를 거쳐서만 [KoreanLunarCalendar]를 사용해야 한다.
 * getInstance()가 반환하는 인스턴스는 JVM 전역에서 공유되는 가변 싱글턴이라,
 * 다른 곳에서 직접 호출하면 이 클래스의 @Synchronized 보호가 무력화된다.
 */
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

    /** 음력(평달) 날짜를 양력으로 바꾼다. 라이브러리 지원 범위를 벗어나면 null. */
    @Synchronized
    fun toSolar(lunarYear: Int, lunarMonth: Int, lunarDay: Int): LocalDate? {
        val calendar = KoreanLunarCalendar.getInstance()
        val ok = calendar.setLunarDate(lunarYear, lunarMonth, lunarDay, false)
        if (!ok) return null
        return LocalDate.of(calendar.solarYear, calendar.solarMonth, calendar.solarDay)
    }
}
