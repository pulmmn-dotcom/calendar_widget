package com.pulmm.shiftcalendar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.pulmm.shiftcalendar.data.entity.ShiftPattern
import com.pulmm.shiftcalendar.data.entity.ShiftPatternItem
import kotlinx.coroutines.flow.Flow

data class PatternWithItems(
    @Embedded val pattern: ShiftPattern,
    @Relation(parentColumn = "id", entityColumn = "patternId")
    val items: List<ShiftPatternItem>
)

@Dao
interface ShiftPatternDao {
    @Transaction
    @Query("SELECT * FROM shift_patterns ORDER BY startEpochDay ASC")
    fun observeAllWithItems(): Flow<List<PatternWithItems>>

    @Insert
    suspend fun insertPattern(pattern: ShiftPattern): Long

    @Insert
    suspend fun insertItems(items: List<ShiftPatternItem>)

    @Delete
    suspend fun deletePattern(pattern: ShiftPattern)
}
