package com.pulmm.shiftcalendar.logic

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * 한국 공휴일을 인터넷 없이 앱 안에서 계산한다. (관공서의 공휴일에 관한 규정 기준)
 * 양력 고정 공휴일, 음력 공휴일(설날·부처님오신날·추석), 대체공휴일은 규칙으로 계산하고,
 * 규칙이 없는 선거일·임시공휴일은 [EXTRA_HOLIDAYS]에 직접 적어 둔다.
 */
object KoreanHolidays {
    private const val SUBSTITUTE_NAME = "대체공휴일"

    /** 규칙으로 계산할 수 없는 날. 새 임시공휴일이 발표되면 여기에 추가한다. */
    private val EXTRA_HOLIDAYS: Map<LocalDate, String> = mapOf(
        LocalDate.of(2024, 4, 10) to "국회의원선거",
        LocalDate.of(2024, 10, 1) to "임시공휴일",
        LocalDate.of(2025, 1, 27) to "임시공휴일",
        LocalDate.of(2025, 6, 3) to "대통령선거",
        LocalDate.of(2026, 6, 3) to "지방선거",
        LocalDate.of(2028, 4, 12) to "국회의원선거"
    )

    private val cache = ConcurrentHashMap<Int, Map<LocalDate, String>>()

    /** 그 날이 공휴일이면 이름, 아니면 null. */
    fun nameOf(date: LocalDate): String? = holidaysOf(date.year)[date]

    fun isHoliday(date: LocalDate): Boolean = nameOf(date) != null

    /** 한 해의 공휴일 전체(날짜 → 이름). 연도별로 한 번만 계산해 둔다. */
    fun holidaysOf(year: Int): Map<LocalDate, String> = cache.getOrPut(year) { compute(year) }

    /**
     * 대체공휴일 규칙
     * - [block]: 설날·추석 3일 연휴. 일요일이나 다른 공휴일과 겹치면 대체공휴일 1일.
     * - [satToo]: 토요일도 대체 대상인지 (설날·추석은 일요일만).
     */
    private class Base(val dates: List<LocalDate>, val name: String, val substitutable: Boolean, val satToo: Boolean)

    private fun compute(year: Int): Map<LocalDate, String> {
        val bases = mutableListOf<Base>()
        fun fixed(month: Int, day: Int, name: String, substituteFrom: Int?) {
            val sub = substituteFrom != null && year >= substituteFrom
            bases += Base(listOf(LocalDate.of(year, month, day)), name, sub, satToo = true)
        }

        fixed(1, 1, "신정", null)
        fixed(3, 1, "삼일절", 2022)
        if (year >= 2026) fixed(5, 1, "노동절", null)
        fixed(5, 5, "어린이날", 2014)
        fixed(6, 6, "현충일", null)
        if (year <= 2007 || year >= 2026) fixed(7, 17, "제헌절", 2026)
        fixed(8, 15, "광복절", 2021)
        fixed(10, 3, "개천절", 2021)
        fixed(10, 9, "한글날", 2021)
        fixed(12, 25, "성탄절", 2023)

        LunarConverter.toSolar(year, 1, 1)?.let { seollal ->
            bases += Base(listOf(seollal.minusDays(1), seollal, seollal.plusDays(1)), "설날", year >= 2014, satToo = false)
        }
        LunarConverter.toSolar(year, 4, 8)?.let {
            bases += Base(listOf(it), "부처님오신날", year >= 2023, satToo = true)
        }
        LunarConverter.toSolar(year, 8, 15)?.let { chuseok ->
            bases += Base(listOf(chuseok.minusDays(1), chuseok, chuseok.plusDays(1)), "추석", year >= 2014, satToo = false)
        }

        val result = sortedMapOf<LocalDate, String>()
        // 같은 날 공휴일이 겹치면 이름을 함께 보여준다. (예: 어린이날·부처님오신날)
        val namesByDate = mutableMapOf<LocalDate, MutableList<String>>()
        bases.forEach { b -> b.dates.forEach { namesByDate.getOrPut(it) { mutableListOf() } += b.name } }
        EXTRA_HOLIDAYS.filterKeys { it.year == year }.forEach { (d, n) -> namesByDate.getOrPut(d) { mutableListOf() } += n }
        namesByDate.forEach { (d, names) -> if (d.year == year) result[d] = names.distinct().joinToString("·") }

        // 대체공휴일: 날짜 순서대로, 겹친 날 하나당 한 번만 만든다.
        val handled = mutableSetOf<LocalDate>()
        val allDates = namesByDate.keys
        for (b in bases.filter { it.substitutable }.sortedBy { it.dates.first() }) {
            val triggered = b.dates.any { d ->
                d !in handled && (
                    d.dayOfWeek == DayOfWeek.SUNDAY ||
                        (b.satToo && d.dayOfWeek == DayOfWeek.SATURDAY) ||
                        namesByDate.getValue(d).size > 1
                    )
            }
            handled += b.dates
            if (!triggered) continue
            var candidate = b.dates.last().plusDays(1)
            while (candidate.dayOfWeek == DayOfWeek.SATURDAY || candidate.dayOfWeek == DayOfWeek.SUNDAY ||
                candidate in allDates || candidate in result
            ) {
                candidate = candidate.plusDays(1)
            }
            result[candidate] = SUBSTITUTE_NAME
        }
        return result.filterKeys { it.year == year }
    }
}
