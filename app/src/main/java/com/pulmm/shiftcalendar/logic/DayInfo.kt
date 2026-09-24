package com.pulmm.shiftcalendar.logic

data class DayInfo(
    val epochDay: Long,
    val shiftTypeId: Long?,
    val memoText: String?
)
