package com.pulmm.shiftcalendar.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShiftResolverTest {

    private val pattern = PatternDefinition(startEpochDay = 100L, shiftTypeIdsInOrder = listOf(1L, 2L))

    @Test
    fun `오버라이드가 없으면 패턴 계산값을 그대로 쓴다`() {
        val result = ShiftResolver.resolveShiftTypeId(listOf(pattern), emptyMap(), 100L)
        assertEquals(1L, result)
    }

    @Test
    fun `오버라이드가 있으면 패턴보다 오버라이드가 우선한다`() {
        val overrides = mapOf(100L to 99L)
        val result = ShiftResolver.resolveShiftTypeId(listOf(pattern), overrides, 100L)
        assertEquals(99L, result)
    }

    @Test
    fun `패턴도 오버라이드도 없으면 null이다`() {
        val result = ShiftResolver.resolveShiftTypeId(emptyList(), emptyMap(), 100L)
        assertNull(result)
    }
}
