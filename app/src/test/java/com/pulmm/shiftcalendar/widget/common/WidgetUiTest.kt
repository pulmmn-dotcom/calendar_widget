package com.pulmm.shiftcalendar.widget.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class WidgetUiTest {

    @Test
    fun `모든 주는 정확히 7일로 구성된다`() {
        val months = listOf(YearMonth.of(2025, 3), YearMonth.of(2025, 6), YearMonth.of(2026, 2))
        for (month in months) {
            for (weekStartMonday in listOf(false, true)) {
                val weeks = buildMonthGrid(month, weekStartMonday)
                weeks.forEach { week -> assertEquals(7, week.size) }
            }
        }
    }

    @Test
    fun `그리드의 날짜는 하루씩 끊김없이 이어진다`() {
        val months = listOf(YearMonth.of(2025, 3), YearMonth.of(2025, 6), YearMonth.of(2026, 2))
        for (month in months) {
            for (weekStartMonday in listOf(false, true)) {
                val flat = buildMonthGrid(month, weekStartMonday).flatten()
                for (i in 1 until flat.size) {
                    assertEquals(flat[i - 1].plusDays(1), flat[i])
                }
            }
        }
    }

    @Test
    fun `일요일에 시작하는 달은 일요일 시작 주에서 앞쪽 채움이 없다`() {
        // 2025-06-01은 일요일(java.time.DayOfWeek.SUNDAY)이다.
        val month = YearMonth.of(2025, 6)
        assertEquals(LocalDate.of(2025, 6, 1), month.atDay(1))
        assertEquals(java.time.DayOfWeek.SUNDAY, month.atDay(1).dayOfWeek)

        val weeks = buildMonthGrid(month, weekStartMonday = false)
        assertEquals(LocalDate.of(2025, 6, 1), weeks.first().first())
    }

    @Test
    fun `토요일에 시작하는 달은 일요일 시작 주에서 앞쪽 채움이 6일이다`() {
        // 2025-03-01은 토요일이다.
        val month = YearMonth.of(2025, 3)
        assertEquals(LocalDate.of(2025, 3, 1), month.atDay(1))
        assertEquals(java.time.DayOfWeek.SATURDAY, month.atDay(1).dayOfWeek)

        val weeks = buildMonthGrid(month, weekStartMonday = false)
        val firstWeek = weeks.first()
        // 앞쪽 6일은 채움(2월), 마지막 1일(토요일)이 3월 1일이어야 한다.
        assertEquals(LocalDate.of(2025, 2, 23), firstWeek[0])
        assertEquals(LocalDate.of(2025, 2, 28), firstWeek[5])
        assertEquals(LocalDate.of(2025, 3, 1), firstWeek[6])
    }

    @Test
    fun `달이 중간에 시작하는 경우 첫 행의 선행 채움 날짜는 이전 달에 속한다`() {
        val month = YearMonth.of(2025, 3)
        val weeks = buildMonthGrid(month, weekStartMonday = false)
        val firstWeek = weeks.first()

        // 3월 1일(토요일) 이전의 채움 셀들은 모두 2월(이전 달)이어야 한다.
        for (i in 0 until 6) {
            assertEquals(2, firstWeek[i].monthValue)
            assertTrue(YearMonth.from(firstWeek[i]) != month)
        }
        // 마지막 칸만 실제 3월 1일이어야 한다.
        assertEquals(3, firstWeek[6].monthValue)
        assertTrue(YearMonth.from(firstWeek[6]) == month)
    }
}
