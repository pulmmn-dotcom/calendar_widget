package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity

@Entity(tableName = "memos", primaryKeys = ["epochDay"])
data class Memo(
    val epochDay: Long,
    val text: String
)
