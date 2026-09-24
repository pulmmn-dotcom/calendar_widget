package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shift_pattern_items",
    foreignKeys = [
        ForeignKey(entity = ShiftPattern::class, parentColumns = ["id"], childColumns = ["patternId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("patternId"), Index("shiftTypeId")]
)
data class ShiftPatternItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patternId: Long,
    val shiftTypeId: Long,
    val orderIndex: Int
)
