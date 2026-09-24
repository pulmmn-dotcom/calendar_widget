package com.pulmm.shiftcalendar.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulmm.shiftcalendar.data.entity.Memo
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeRange(startEpochDay: Long, endEpochDay: Long): Flow<List<Memo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(memo: Memo)

    @Query("DELETE FROM memos WHERE epochDay = :epochDay")
    suspend fun delete(epochDay: Long)
}
