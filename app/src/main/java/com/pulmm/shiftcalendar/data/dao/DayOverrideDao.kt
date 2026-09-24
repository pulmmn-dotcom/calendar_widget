package com.pulmm.shiftcalendar.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulmm.shiftcalendar.data.entity.DayOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface DayOverrideDao {
    @Query("SELECT * FROM day_overrides WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DayOverride>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: DayOverride)

    @Query("DELETE FROM day_overrides WHERE epochDay = :epochDay")
    suspend fun delete(epochDay: Long)
}
