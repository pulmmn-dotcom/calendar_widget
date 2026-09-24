package com.pulmm.shiftcalendar.logic

object ShiftResolver {
    fun resolveShiftTypeId(
        patterns: List<PatternDefinition>,
        overrides: Map<Long, Long>,
        targetEpochDay: Long
    ): Long? {
        overrides[targetEpochDay]?.let { return it }
        return PatternCalculator.resolveShiftTypeId(patterns, targetEpochDay)
    }
}
