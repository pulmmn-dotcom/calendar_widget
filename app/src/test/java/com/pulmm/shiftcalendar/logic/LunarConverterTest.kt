package com.pulmm.shiftcalendar.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class LunarConverterTest {

    @Test
    fun `양력 날짜를 음력으로 변환한다`() {
        val result = LunarConverter.toLunar(LocalDate.of(1956, 3, 3))
        assertEquals(LunarDate(1956, 1, 21, false), result)
    }

    @Test
    fun `윤달인 경우를 구분한다`() {
        val result = LunarConverter.toLunar(LocalDate.of(2017, 6, 24))
        assertEquals(LunarDate(2017, 5, 1, true), result)
    }

    @Test
    fun `지원 범위를 벗어나면 null을 반환한다`() {
        val result = LunarConverter.toLunar(LocalDate.of(2051, 1, 1))
        assertNull(result)
    }

    @Test
    fun `지원 범위의 최댓값 경계를 처리한다`() {
        val result = LunarConverter.toLunar(LocalDate.of(2050, 12, 31))
        assertEquals(LunarDate(2050, 11, 18, false), result)
    }
}
