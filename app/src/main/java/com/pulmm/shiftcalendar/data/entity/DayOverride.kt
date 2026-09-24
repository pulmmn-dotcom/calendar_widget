package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity

@Entity(tableName = "day_overrides", primaryKeys = ["epochDay"])
data class DayOverride(
    val epochDay: Long,
    val shiftTypeId: Long
)
