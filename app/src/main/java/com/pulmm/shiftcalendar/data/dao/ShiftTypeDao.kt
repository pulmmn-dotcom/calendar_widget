package com.pulmm.shiftcalendar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pulmm.shiftcalendar.data.entity.ShiftType
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftTypeDao {
    @Query("SELECT * FROM shift_types ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<ShiftType>>

    @Insert
    suspend fun insert(shiftType: ShiftType): Long

    @Update
    suspend fun update(shiftType: ShiftType)

    @Delete
    suspend fun delete(shiftType: ShiftType)
}
