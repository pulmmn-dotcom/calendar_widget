package com.pulmm.shiftcalendar.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PatternCalculatorTest {

    @Test
    fun `패턴이 없으면 null을 반환한다`() {
        val result = PatternCalculator.resolveShiftTypeId(emptyList(), targetEpochDay = 100L)
        assertNull(result)
    }

    @Test
    fun `시작일부터 순서대로 근무를 반복한다`() {
        val pattern = PatternDefinition(startEpochDay = 100L, shiftTypeIdsInOrder = listOf(1L, 2L, 3L))
        assertEquals(1L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 100L))
        assertEquals(2L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 101L))
        assertEquals(3L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 102L))
        assertEquals(1L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 103L))
        assertEquals(3L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 111L))
    }

    @Test
    fun `시작일 이전 날짜는 null을 반환한다`() {
        val pattern = PatternDefinition(startEpochDay = 100L, shiftTypeIdsInOrder = listOf(1L, 2L))
        assertNull(PatternCalculator.resolveShiftTypeId(listOf(pattern), 99L))
    }

    @Test
    fun `가장 최근에 시작한 패턴이 우선 적용된다`() {
        val oldPattern = PatternDefinition(startEpochDay = 100L, shiftTypeIdsInOrder = listOf(1L, 2L))
        val newPattern = PatternDefinition(startEpochDay = 200L, shiftTypeIdsInOrder = listOf(9L))
        val patterns = listOf(oldPattern, newPattern)
        assertEquals(2L, PatternCalculator.resolveShiftTypeId(patterns, 199L))
        assertEquals(9L, PatternCalculator.resolveShiftTypeId(patterns, 200L))
        assertEquals(9L, PatternCalculator.resolveShiftTypeId(patterns, 250L))
    }
}
