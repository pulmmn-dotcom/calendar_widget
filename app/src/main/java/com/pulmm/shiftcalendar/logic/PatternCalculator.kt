package com.pulmm.shiftcalendar.logic

data class PatternDefinition(
    val startEpochDay: Long,
    val shiftTypeIdsInOrder: List<Long>
)

object PatternCalculator {
    fun resolveShiftTypeId(patterns: List<PatternDefinition>, targetEpochDay: Long): Long? {
        val applicable = patterns
            .filter { it.startEpochDay <= targetEpochDay && it.shiftTypeIdsInOrder.isNotEmpty() }
            .maxByOrNull { it.startEpochDay }
            ?: return null
        val size = applicable.shiftTypeIdsInOrder.size
        val dayOffset = targetEpochDay - applicable.startEpochDay
        val index = ((dayOffset % size) + size) % size
        return applicable.shiftTypeIdsInOrder[index.toInt()]
    }
}
