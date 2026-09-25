package com.pulmm.shiftcalendar.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class KoreanHolidaysTest {

    private fun holidays(year: Int) = KoreanHolidays.holidaysOf(year).mapKeys { "${it.key.monthValue}/${it.key.dayOfMonth}" }

    @Test
    fun `2026년 공휴일 전체가 정부 발표와 같다`() {
        val expected = mapOf(
            "1/1" to "신정",
            "2/16" to "설날", "2/17" to "설날", "2/18" to "설날",
            "3/1" to "삼일절", "3/2" to "대체공휴일",
            "5/1" to "노동절",
            "5/5" to "어린이날",
            "5/24" to "부처님오신날", "5/25" to "대체공휴일",
            "6/3" to "지방선거",
            "6/6" to "현충일",
            "7/17" to "제헌절",
            "8/15" to "광복절", "8/17" to "대체공휴일",
            "9/24" to "추석", "9/25" to "추석", "9/26" to "추석",
            "10/3" to "개천절", "10/5" to "대체공휴일",
            "10/9" to "한글날",
            "12/25" to "성탄절"
        )
        assertEquals(expected, holidays(2026))
    }

    @Test
    fun `같은 날 겹친 공휴일은 이름을 함께 쓰고 대체공휴일은 하루만 생긴다`() {
        val h = holidays(2025)
        assertEquals("어린이날·부처님오신날", h["5/5"])
        assertEquals("대체공휴일", h["5/6"])
        assertNull(h["5/7"])
    }

    @Test
    fun `추석 연휴가 일요일과 겹치면 연휴 다음 평일이 대체공휴일이다`() {
        val h = holidays(2025)
        assertEquals("추석", h["10/5"])
        assertEquals("대체공휴일", h["10/8"])
        assertEquals("한글날", h["10/9"])
    }

    @Test
    fun `설날 연휴가 일요일과 겹치면 대체공휴일이 생긴다`() {
        val h = holidays(2024)
        assertEquals("설날", h["2/11"])
        assertEquals("대체공휴일", h["2/12"])
        assertEquals("대체공휴일", h["5/6"])
    }

    @Test
    fun `추석과 개천절이 겹치면 연휴 뒤 하루만 대체공휴일이다`() {
        val h = holidays(2028)
        assertEquals("개천절·추석", h["10/3"])
        assertEquals("대체공휴일", h["10/5"])
        assertNull(h["10/6"])
    }

    @Test
    fun `제헌절은 2008년부터 2025년까지는 공휴일이 아니다`() {
        assertNull(KoreanHolidays.nameOf(LocalDate.of(2025, 7, 17)))
        assertNull(KoreanHolidays.nameOf(LocalDate.of(2025, 5, 1)))
    }
}
